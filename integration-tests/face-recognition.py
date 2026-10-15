"""Exercise the real AI service with public reference photos; no user photos are uploaded.

Downloads the face_recognition project's own example images into ignored build/.
Run from the repository root after `docker compose up -d --build ai-service`.
"""
import json
import http.client
import math
import struct
import time
import urllib.error
import urllib.request
import uuid
import zlib
from pathlib import Path

BASE = 'http://127.0.0.1:8000'
FIXTURES = Path('build/face-fixtures')
FIXTURES.mkdir(parents=True, exist_ok=True)
results = []


def post(path, image, fields=None):
    boundary = uuid.uuid4().hex
    body = bytearray()
    for name, value in (fields or {}).items():
        body.extend(f'--{boundary}\r\nContent-Disposition: form-data; name="{name}"\r\n\r\n{value}\r\n'.encode())
    body.extend(f'--{boundary}\r\nContent-Disposition: form-data; name="file"; filename="face.jpg"\r\nContent-Type: image/jpeg\r\n\r\n'.encode())
    body.extend(image)
    body.extend(f'\r\n--{boundary}--\r\n'.encode())
    request = urllib.request.Request(BASE + path, data=bytes(body), headers={
        'Content-Type': f'multipart/form-data; boundary={boundary}'})
    try:
        with urllib.request.urlopen(request, timeout=60) as response:
            return response.status, json.load(response)
    except urllib.error.HTTPError as error:
        return error.code, json.load(error)


def check(name, action):
    try:
        action()
        results.append({'name': name, 'passed': True})
        print('PASS', name, flush=True)
    except Exception as error:
        results.append({'name': name, 'passed': False, 'error': str(error)})
        print('FAIL', name, str(error), flush=True)


def require(condition, message):
    if not condition:
        raise AssertionError(message)


for retry in range(30):
    try:
        with urllib.request.urlopen(BASE + '/health', timeout=5) as response:
            require(json.load(response)['status'] == 'ok', 'AI service is unhealthy')
        break
    except (urllib.error.URLError, TimeoutError, ConnectionError, http.client.HTTPException):
        if retry == 29:
            raise
        time.sleep(2)

for name in ('obama.jpg', 'obama2.jpg', 'biden.jpg'):
    target = FIXTURES / name
    if not target.exists():
        url = 'https://raw.githubusercontent.com/ageitgey/face_recognition/master/examples/' + name
        with urllib.request.urlopen(url, timeout=60) as response:
            target.write_bytes(response.read())

reference = (FIXTURES / 'obama.jpg').read_bytes()
other_view = (FIXTURES / 'obama2.jpg').read_bytes()
different_person = (FIXTURES / 'biden.jpg').read_bytes()
status, extracted = post('/api/v1/ai/extract-vector', reference)
descriptor = extracted.get('descriptor', [])
check('Real image produces a finite 128-dimensional face descriptor', lambda: require(
    status == 200 and extracted.get('success') and len(descriptor) == 128
    and all(math.isfinite(value) for value in descriptor), 'Face descriptor extraction failed'))
registered = {'registered_descriptors': json.dumps([{'studentId': 101, 'descriptor': descriptor}])}


def verify(image, expected):
    status, body = post('/api/v1/ai/verify-face', image, registered)
    require(status == 200 and body.get('present_student_ids') == expected, f'Unexpected match result: {status} {body}')


check('Another photo of the same person matches', lambda: verify(other_view, [101]))
check('A different person does not match', lambda: verify(different_person, []))
check('Invalid image is rejected with HTTP 400', lambda: require(
    post('/api/v1/ai/extract-vector', b'not an image')[0] == 400, 'Expected HTTP 400'))
check('Malformed descriptor JSON is rejected with HTTP 400', lambda: require(
    post('/api/v1/ai/verify-face', reference, {'registered_descriptors': '{bad json'})[0] == 400, 'Expected HTTP 400'))


def chunk(kind, value):
    return struct.pack('!I', len(value)) + kind + value + struct.pack('!I', zlib.crc32(kind + value) & 0xffffffff)


blank = b'\x89PNG\r\n\x1a\n' + chunk(b'IHDR', struct.pack('!2I5B', 64, 64, 8, 2, 0, 0, 0))
blank += chunk(b'IDAT', zlib.compress((b'\0' + b'\xff' * 64 * 3) * 64)) + chunk(b'IEND', b'')
check('An image without a face is not enrolled', lambda: require(
    post('/api/v1/ai/extract-vector', blank)[1].get('success') is False, 'Blank image must not produce an enrollment'))
Path('build/face-recognition-results.json').write_text(json.dumps({'results': results}, indent=2))
print(f'{sum(result["passed"] for result in results)}/{len(results)} passed', flush=True)
raise SystemExit(1 if any(not result['passed'] for result in results) else 0)
