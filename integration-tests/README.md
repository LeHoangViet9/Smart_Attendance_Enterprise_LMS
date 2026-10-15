# Backend integration checks

These checks use real HTTP requests, PostgreSQL, Redis and MinIO. They create test users, a course, a class, enrollment, an assignment, submissions, quiz attempts, attendance and notifications. They do not test browser rendering, Gemini responses or WebSocket delivery. Real face recognition can also be tested with the optional commands below.

Requirements: JDK 21, Node.js, Docker Compose services running from the repository root. The test database and bucket are intentionally retained for investigation. Do not point this suite at production.

## Prepare and build (PowerShell, repository root)

Create `lms_integration_test` once:

```powershell
docker compose up -d
docker exec lms_postgres createdb -U postgres lms_integration_test
New-Item -ItemType Directory -Force build/tmp | Out-Null
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-21'
$env:JAVA_TOOL_OPTIONS = "-Djdk.net.unixdomain.tmpdir=$PWD\build\tmp"
$env:DB_URL = 'jdbc:postgresql://localhost:5434/lms_integration_test'
.\gradlew.bat build --no-daemon --max-workers=2
```

Adjust `JAVA_HOME` to the installed JDK 21. The socket setting avoids the Windows JDK loopback failure when the system temporary directory is unsuitable.

## Start the dedicated server

Run in a separate terminal, from the repository root:

```powershell
& "$env:JAVA_HOME\bin\java.exe" "-Djdk.net.unixdomain.tmpdir=$PWD\build\tmp" -jar build/libs/Smart_Attendance_Enterprise_LMS-0.0.1-SNAPSHOT.jar --server.port=18080 --spring.datasource.url=jdbc:postgresql://localhost:5434/lms_integration_test --spring.data.redis.database=15 --aws.s3.bucket-name=lms-integration-test --spring.jpa.show-sql=false
```

Reserve Redis DB 15 and the `lms-integration-test` bucket for this suite. Wait for startup and sample data seeding to finish.

## Run

```powershell
node integration-tests/backend.mjs
```

To include real AI enrollment and face matching, start the AI container and download/test its reference images first:

```powershell
docker compose up -d --build ai-service
python integration-tests/face-recognition.py
node integration-tests/backend.mjs --faces
```

Exit code is nonzero on failure. Results are written to `build/integration-results.json` without authentication tokens. Each run creates unique fixtures; the suite can be rerun without deleting previous test data. Stop the dedicated server after testing.

Class/enrollment fixtures are inserted directly into the test database; the remaining flows use application APIs. This does not validate a UI or API workflow for creating classes/enrollment.
