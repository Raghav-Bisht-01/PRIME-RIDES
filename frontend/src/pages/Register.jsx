import { useState } from "react";
import { API_URL } from "../config";

function Register() {
  const [form, setForm] = useState({
    name: "",
    email: "",
    phone: "",
    password: "",
  });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  function update(event) {
    setForm({ ...form, [event.target.name]: event.target.value });
  }

  async function handleRegister(event) {
    event.preventDefault();
    setError("");

    if (!form.name.trim()) {
      setError("Please enter your full name.");
      return;
    }

    setLoading(true);

    try {
      const response = await fetch(
        `${API_URL}/api/auth/register`,
        {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({
            ...form,
            name: form.name.trim(),
            email: form.email.trim(),
            phone: form.phone.trim(),
          }),
        }
      );

      const data = await response.json();

      if (!response.ok) {
        setError(data.error || "Registration failed");
        return;
      }

      alert("Account created successfully! Please log in.");
      window.location.href = "/login";
    } catch {
      setError("Cannot connect to the server.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="auth-page">
      <form className="auth-card" onSubmit={handleRegister}>
        <a className="brand" href="/">
          <span className="brand-icon">P</span>
          <span>PRIME <strong>RIDES</strong></span>
        </a>

        <h1>Create your account</h1>
        <p>Your next journey starts here.</p>

        <label>
          Full name
          <input
            name="name"
            value={form.name}
            onChange={update}
            placeholder="Your full name"
            required
          />
        </label>

        <label>
          Email address
          <input
            type="email"
            name="email"
            value={form.email}
            onChange={update}
            placeholder="you@example.com"
            required
          />
        </label>

        <label>
          Phone number
          <input
            type="tel"
            name="phone"
            value={form.phone}
            onChange={update}
            placeholder="10-digit phone number"
            pattern="[0-9]{10}"
            required
          />
        </label>

        <label>
          Password
          <input
            type="password"
            name="password"
            value={form.password}
            onChange={update}
            placeholder="At least 6 characters"
            minLength={6}
            required
          />
        </label>

        {error && <p className="auth-error">{error}</p>}

        <button className="signup-button auth-submit" disabled={loading}>
          {loading ? "Creating account..." : "Create account"}
        </button>

        <p className="auth-switch">
          Already registered? <a href="/login">Log in</a>
        </p>
      </form>
    </div>
  );
}

export default Register;
