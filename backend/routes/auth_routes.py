from flask import Blueprint, request, jsonify
from werkzeug.security import generate_password_hash, check_password_hash
from sqlalchemy import text
try:
    from ..db import db
except ImportError:
    from db import db

auth_bp = Blueprint("auth", __name__, url_prefix="/api/auth")


@auth_bp.route("/register", methods=["POST"])
def register():
    data = request.get_json() or {}

    if not isinstance(data, dict):
        return jsonify({"error": "Request body must be a JSON object"}), 400

    if any(not isinstance(data.get(field, ""), str) for field in ("name", "email", "phone", "password")):
        return jsonify({"error": "All fields must be text"}), 400

    name = data.get("name", "").strip()
    email = data.get("email", "").strip().lower()
    phone = data.get("phone", "").strip()
    password = data.get("password", "")

    if not name or not email or not phone or not password:
        return jsonify({"error": "All fields are required"}), 400

    if len(password) < 6:
        return jsonify({"error": "Password must be at least 6 characters"}), 400

    existing = db.session.execute(
        text("SELECT id FROM users WHERE email = :email OR phone = :phone"),
        {"email": email, "phone": phone}
    ).fetchone()

    if existing:
        return jsonify({"error": "Email or phone already registered"}), 409

    try:
        db.session.execute(
            text("""
                INSERT INTO users (full_name, email, phone, password_hash, role)
                VALUES (:name, :email, :phone, :password_hash, 'user')
            """),
            {
                "name": name,
                "email": email,
                "phone": phone,
                "password_hash": generate_password_hash(password)
            }
        )
        db.session.commit()

        return jsonify({"message": "Registration successful"}), 201

    except Exception:
        db.session.rollback()
        return jsonify({"error": "Registration failed"}), 500


@auth_bp.route("/login", methods=["POST"])
def login():
    data = request.get_json() or {}

    if not isinstance(data, dict):
        return jsonify({"error": "Request body must be a JSON object"}), 400

    if any(not isinstance(data.get(field, ""), str) for field in ("email", "password")):
        return jsonify({"error": "Email and password must be text"}), 400

    email = data.get("email", "").strip().lower()
    password = data.get("password", "")

    if not email or not password:
        return jsonify({"error": "Email and password are required"}), 400

    user = db.session.execute(
        text("""
            SELECT id, full_name, email, phone, password_hash, role
            FROM users
            WHERE email = :email
        """),
        {"email": email}
    ).mappings().first()

    if not user or not check_password_hash(
        user["password_hash"], password
    ):
        return jsonify({"error": "Invalid email or password"}), 401

    return jsonify({
        "message": "Login successful",
        "user": {
            "id": user["id"],
            "name": user["full_name"],
            "email": user["email"],
            "phone": user["phone"],
            "role": user["role"]
        }
    }), 200
