async function signup() {

    const email = document.getElementById("signupEmail").value;
    const password = document.getElementById("signupPassword").value;

    try {

        const response = await fetch(
            "http://localhost:8080/api/auth/register",
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    email: email,
                    password: password
                })
            }
        );

        if (response.ok) {

            alert("Registration successful! Please sign in.");

            window.location.href = "index.html";

        } else {

            alert("Registration failed. Email may already exist.");

        }

    } catch (error) {

        alert("Cannot connect to backend server.");

    }
}


async function login() {

    const email = document.getElementById("loginEmail").value;
    const password = document.getElementById("loginPassword").value;

    try {

        const response = await fetch(
            "http://localhost:8080/api/auth/login",
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    email: email,
                    password: password
                })
            }
        );

        if (response.ok) {

            const data = await response.json();

            localStorage.setItem("token", data.token);
            localStorage.setItem("email", data.email);

            alert("Login successful!");

            window.location.href = "dashboard.html";

        } else {

            alert("Invalid email or password.");

        }

    } catch (error) {

        alert("Cannot connect to backend server.");

    }
}