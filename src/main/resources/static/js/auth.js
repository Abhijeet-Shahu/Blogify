const BASE_URL = "http://localhost:8080/api/auth"; // adjust to your backend

// REGISTER
async function registerUser(event) {
    event.preventDefault();

    const email = document.getElementById("registerEmail").value;
    const username = document.getElementById("registerUsername").value;
    const password = document.getElementById("registerPassword").value;
    const confirmPassword = document.getElementById("registerConfirmPassword").value;

    if (password !== confirmPassword) {
        alert("Passwords do not match!");
        return;
    }

    try {
        const res = await fetch(`${BASE_URL}/register`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email, username, password })
        });

        if (res.ok) {
            alert("Registration successful! Please login.");
            window.location.href = "login.html";
        } else {
            const err = await res.json();
            alert("Error: " + err.message);
        }
    } catch (e) {
        console.error(e);
        alert("Something went wrong!");
    }
}

// LOGIN
async function loginUser(event) {
    event.preventDefault();

    const email = document.getElementById("loginEmail").value;
    const password = document.getElementById("loginPassword").value;

    try {
        const res = await fetch(`${BASE_URL}/login`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email, password })
        });
        console.log(res);

        if (res.ok) {
            const data = await res.json();
            localStorage.setItem("token", data.token);
            localStorage.setItem("user", JSON.stringify(data.user));
            window.location.href = "index.html";
        } else {
            const err = await res.json();
            alert("Login failed: " + err.message);
        }
    } catch (e) {
        console.error(e);
        alert("Something went wrong!");
    }
}

// SHOW DASHBOARD IF LOGGED IN
function checkLogin() {
    const user = localStorage.getItem("user");
    const dashboardLink = document.getElementById("dashboard-link");
    const loginButtons = document.getElementById("auth-buttons");

    if (user && dashboardLink) {
        dashboardLink.style.display = "block";
        if (loginButtons) loginButtons.style.display = "none";
    }
}

// LOGOUT
function logoutUser() {
    localStorage.clear();
    window.location.href = "index.html";
}

window.onload = checkLogin;
