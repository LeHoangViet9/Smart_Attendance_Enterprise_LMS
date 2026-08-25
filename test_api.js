async function testApi() {
    try {
        const response = await fetch('http://localhost:8080/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email: 'sinhvien@edu.vn', password: '123456' })
        });
        const loginData = await response.json();

        if (!loginData.data) {
            console.log("LOGIN FAIL STATUS:", response.status);
            console.log("LOGIN FAIL:", loginData);
            return;
        }

        const token = loginData.data.accessToken;
        console.log("LOGIN SUCCESS! Token fetched.");

        const quizResponse = await fetch('http://localhost:8080/api/v1/student/quizzes', {
            headers: { Authorization: `Bearer ${token}` }
        });

        console.log("QUIZ API HTTP STATUS:", quizResponse.status);
        const text = await quizResponse.text();
        console.log("QUIZ API BODY:", text);
    } catch (e) {
        console.error("ERROR:", e);
    }
}

testApi();
