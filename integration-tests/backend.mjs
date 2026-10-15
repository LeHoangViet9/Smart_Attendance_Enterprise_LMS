import assert from 'node:assert/strict';
import {execFileSync} from 'node:child_process';
import {randomUUID} from 'node:crypto';
import {mkdirSync, writeFileSync, readFileSync} from 'node:fs';

// Run only against the dedicated test server/database described in README.md.
const base = 'http://127.0.0.1:18080/api';
const run = Date.now().toString();
const results = [];
for (let retry = 0; ; retry++) {
  try {
    await fetch(base + '/v1/courses', {signal: AbortSignal.timeout(2000)});
    break;
  } catch (error) {
    if (retry >= 29) throw new Error('Test server at port 18080 did not become ready within 60 seconds', {cause: error});
    await new Promise(resolve => setTimeout(resolve, 2000));
  }
}
function sql(query) {
  return execFileSync('docker', ['exec', 'lms_postgres', 'psql', '-U', 'postgres',
    '-d', 'lms_integration_test', '-v', 'ON_ERROR_STOP=1', '-tAc', query], {encoding: 'utf8'}).trim();
}
assert.equal(sql('SELECT current_database()'), 'lms_integration_test');
async function request(path, {method = 'GET', token, body, status = 200} = {}) {
  const res = await fetch(base + path, {method, headers: {
    ...(token ? {Authorization: `Bearer ${token}`} : {}),
    ...(body !== undefined ? {'Content-Type': 'application/json'} : {})
  }, body: body === undefined ? undefined : JSON.stringify(body), signal: AbortSignal.timeout(20000)});
  const data = await res.json();
  assert.equal(res.status, status, `${method} ${path}: ${res.status} ${data.message ?? ''}`);
  return data.data;
}
async function check(name, test) {
  try { await test(); results.push({name, passed: true}); console.log(`PASS ${name}`); }
  catch (e) { results.push({name, passed: false, error: e.message}); console.log(`FAIL ${name}: ${e.message}`); }
}
const admin = await request('/auth/login', {method: 'POST', body: {email: 'admin@edu.vn', password: '123456'}});
async function user(role, suffix) {
  const email = `integration${run}${suffix}@example.com`;
  const u = await request('/auth/register', {method: 'POST', token: admin.accessToken,
    body: {email, code: `IT${run}${suffix}`, fullName: `Integration ${role}`, role}});
  const login = await request('/auth/login', {method: 'POST', body: {email, password: email}});
  assert.equal(sql(`SELECT user_id FROM users WHERE email='${email}'`), String(u.userId), 'Server must use the test database');
  return {...login, userId: u.userId};
}
const student = await user('STUDENT', 'S');
const other = await user('STUDENT', 'O');
const lecturer = await user('LECTURER', 'L');
const at = admin.accessToken, st = student.accessToken, lt = lecturer.accessToken;
await check('Anonymous requests require authentication', () => request('/v1/courses', {status: 401}));
await check('Invalid credentials are rejected', () => request('/auth/login', {method: 'POST', body: {email: student.email, password: 'wrong'}, status: 401}));
await check('Students cannot create courses', () => request('/v1/courses', {method: 'POST', token: st, body: {title: 'Forbidden'}, status: 403}));
await check('Public password reset token issuance is blocked', () => request('/auth/forgot-password', {method: 'POST', body: {email: student.email}, status: 401}));
await check('Students cannot issue password reset tokens', () => request('/auth/forgot-password', {method: 'POST', token: st, body: {email: student.email}, status: 403}));
await check('Course title validation', () => request('/v1/courses', {method: 'POST', token: lt, body: {title: ''}, status: 400}));
let course, classId, assignment, quiz, question, attempt, fileUrl, submission;
await check('Lecturer creates course and student enrollment is persisted', async () => {
  course = await request('/v1/courses', {method: 'POST', token: lt, body: {title: `Integration ${run}`, isPublished: true}, status: 201});
  classId = randomUUID();
  sql(`INSERT INTO school_classes(id,class_name,entry_year,lecturer_id,course_id) VALUES ('${classId}','IT${run}',2026,${lecturer.userId},'${course.id}'); INSERT INTO class_enrollments(id,class_id,student_user_id,enrolled_at,is_active) VALUES ('${randomUUID()}','${classId}',${student.userId},CURRENT_TIMESTAMP,true)`);
  const courses = await request('/v1/courses', {token: st});
  assert(courses.content.some(c => c.id === course.id));
});
await check('MinIO signed upload and download preserve file contents', async () => {
  const upload = await request('/v1/assignments/upload-url', {method: 'POST', token: st, body: {fileName: 'integration.txt', contentType: 'text/plain'}});
  const content = `integration-${run}`;
  const put = await fetch(upload.uploadUrl, {method: 'PUT', body: content});
  assert.equal(put.status, 200);
  const get = await fetch(upload.fileUrl);
  assert.equal(get.status, 200);
  assert.equal(await get.text(), content);
  fileUrl = upload.fileUrl;
});
if (process.argv.includes('--faces')) {
  async function uploadFace(path, token, filename) {
    const form = new FormData();
    form.append('file', new Blob([readFileSync(`build/face-fixtures/${filename}`)], {type: 'image/jpeg'}), filename);
    const response = await fetch(base + path, {method: 'POST', headers: {Authorization: `Bearer ${token}`}, body: form, signal: AbortSignal.timeout(60000)});
    const body = await response.json();
    assert.equal(response.status, 200, `${path}: ${response.status} ${body.message ?? ''}`);
    return body;
  }
  await check('Real face enrollment stores a descriptor in PostgreSQL', async () => {
    const enrolled = await uploadFace('/student/onboarding-face', st, 'obama.jpg');
    assert.equal(enrolled.vectorGenerated, true);
    const stored = JSON.parse(sql(`SELECT face_embedding FROM users WHERE user_id=${student.userId}`));
    assert.equal(stored.length, 128);
  });
  await check('Backend and AI recognize an enrolled student from another photo', async () => {
    const match = await uploadFace(`/v1/attendance/class/${classId}/verify-faces`, lt, 'obama2.jpg');
    assert.deepEqual(match.data, [student.userId]);
  });
  await check('Backend and AI reject another person', async () => {
    const mismatch = await uploadFace(`/v1/attendance/class/${classId}/verify-faces`, lt, 'biden.jpg');
    assert.deepEqual(mismatch.data, []);
  });
}
await check('Assignment creation, student submission and lecturer grading', async () => {
  assert(classId && fileUrl);
  assignment = await request('/v1/assignments', {method: 'POST', token: lt, status: 201,
    body: {classIds: [classId], title: `Assignment ${run}`, dueDate: '2099-01-01T00:00:00', maxScore: 10, isPublished: true}});
  submission = await request(`/v1/assignments/${assignment.id}/submit`, {method: 'POST', token: st, body: {fileUrl}, status: 201});
  await request(`/v1/submissions/${submission.id}/grade`, {method: 'POST', token: lt, body: {score: 8, feedback: 'Integration verified'}});
  const graded = await request(`/v1/assignments/${assignment.id}/submission`, {token: st});
  assert.equal(graded.score, 8);
});
await check('Unenrolled student cannot submit assignment', async () => {
  assert(assignment);
  await request(`/v1/assignments/${assignment.id}/submit`, {method: 'POST', token: other.accessToken, body: {fileUrl}, status: 400});
});
await check('Quiz creation and student answers do not expose correctness', async () => {
  quiz = await request('/v1/quizzes', {method: 'POST', token: lt, status: 201,
    body: {title: `Quiz ${run}`, timeLimitMinutes: 30, classIds: [classId], accessCode: 'integration-code'}});
  question = await request(`/v1/quizzes/${quiz.id}/questions`, {method: 'POST', token: lt, status: 201,
    body: {content: '2 + 2?', points: 10, questionType: 'SINGLE_CHOICE', options: [{content: '4', isCorrect: true}, {content: '5', isCorrect: false}]}});
  const details = await request(`/v1/student/quizzes/${quiz.id}/details`, {token: st});
  assert.equal(details.accessCode, null);
  assert(details.questions.every(q => q.options.every(o => o.isCorrect == null)));
});
await check('Quiz access code is enforced', () => request(`/v1/student/quizzes/${quiz.id}/attempts`, {method: 'POST', token: st, body: {accessCode: 'wrong'}, status: 409}));
await check('Quiz autosave is restored from Redis', async () => {
  attempt = await request(`/v1/student/quizzes/${quiz.id}/attempts`, {method: 'POST', token: st, status: 201, body: {accessCode: 'integration-code'}});
  const answers = [{questionId: question.id, selectedOptionId: question.options.find(o => o.content === '4').id}];
  await request(`/v1/student/quizzes/attempts/${attempt.id}/autosave`, {method: 'POST', token: st, body: {answers}});
  const resumed = await request(`/v1/student/quizzes/${quiz.id}/attempts`, {method: 'POST', token: st, status: 201, body: {accessCode: 'integration-code'}});
  assert.deepEqual(resumed.cachedAnswers.map(a => ({questionId: a.questionId, selectedOptionId: a.selectedOptionId})), answers);
});
await check('Another student cannot submit someone else’s quiz attempt', async () => {
  assert(attempt);
  await request(`/v1/student/quizzes/attempts/${attempt.id}/submit`, {method: 'POST', token: other.accessToken, body: {answers: []}, status: 403});
});
await check('Quiz submission is scored and appears in history', async () => {
  const answers = [{questionId: question.id, selectedOptionId: question.options.find(o => o.content === '4').id}];
  const completed = await request(`/v1/student/quizzes/attempts/${attempt.id}/submit`, {method: 'POST', token: st, body: {answers}});
  assert.equal(completed.status, 'COMPLETED');
  assert.equal(completed.score, 10);
  const history = await request('/v1/student/quizzes/attempts/history', {token: st});
  assert(history.content.some(a => a.id === attempt.id));
});
await check('Student receives assignment notifications', async () => {
  const notifications = await request('/v1/notifications', {token: st});
  assert(notifications.content.length > 0);
});
await check('Attendance is persisted and visible to the student', async () => {
  await request('/v1/attendance/submit-attendance', {method: 'POST', token: lt, body: {classId, presentStudentIds: [student.userId]}});
  const history = await request('/v1/attendance/my-history', {token: st});
  assert(history.content.some(r => r.classId === classId && r.status === 'PRESENT'));
});
await check('Duplicate daily attendance is rejected', () => request('/v1/attendance/submit-attendance', {method: 'POST', token: lt,
  body: {classId, presentStudentIds: [student.userId]}, status: 409}));
await check('Lecturer attendance history and date filters load associated records', async () => {
  const history = await request(`/v1/attendance/class/${classId}/history`, {token: lt});
  assert.equal(history.content.length, 1);
  const date = history.content[0].checkInTime.split('T')[0];
  const filtered = await request(`/v1/attendance/class/${classId}/history?date=${date}`, {token: lt});
  assert.equal(filtered.content[0].studentId, student.userId);
  const personal = await request(`/v1/attendance/my-history?date=${date}`, {token: st});
  assert(personal.content.some(r => r.classId === classId));
});
await check('Gradebook synchronization includes assignment and attendance scores', async () => {
  await request(`/v1/gradebooks/classes/${classId}/sync`, {method: 'POST', token: lt});
  const grades = await request('/v1/gradebooks/my-grades', {token: st});
  const grade = grades.find(g => g.classId === classId);
  assert(grade);
  assert.equal(grade.assignmentScore, 8);
  assert.equal(grade.attendanceScore, 10);
});
await check('Gradebook Excel export returns an XLSX archive', async () => {
  const response = await fetch(base + `/v1/gradebooks/classes/${classId}/export`, {headers: {Authorization: `Bearer ${lt}`}});
  assert.equal(response.status, 200);
  const bytes = Buffer.from(await response.arrayBuffer());
  assert.equal(bytes.subarray(0, 2).toString(), 'PK');
});
await check('Token refresh works with Redis', async () => {
  const refreshed = await request('/auth/refresh-token', {method: 'POST', body: {refreshToken: student.refreshToken}});
  assert(refreshed.accessToken);
  await request('/v1/student/classes', {token: refreshed.accessToken});
});
await check('Allowed frontend origin receives CORS headers', async () => {
  const response = await fetch(base + '/v1/courses', {method: 'OPTIONS', headers: {Origin: 'http://localhost:5173', 'Access-Control-Request-Method': 'GET', 'Access-Control-Request-Headers': 'authorization'}});
  assert.equal(response.status, 200);
  assert.equal(response.headers.get('access-control-allow-origin'), 'http://localhost:5173');
});
mkdirSync('build', {recursive: true});
writeFileSync('build/integration-results.json', JSON.stringify({timestamp: new Date().toISOString(), results}, null, 2));
console.log(`${results.filter(r => r.passed).length}/${results.length} passed`);
if (results.some(r => !r.passed)) process.exitCode = 1;
