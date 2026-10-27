import os
from pathlib import Path
from flask import Flask, abort, jsonify, send_from_directory
from werkzeug.exceptions import NotFound
from flask_cors import CORS
from sqlalchemy import text
try:  # Package imports for Vercel's configured backend.app entrypoint.
    from .config import Config
    from .db import db
    from .routes.auth_routes import auth_bp
    from .routes.vehicle_routes import vehicle_bp
    from .routes.booking_routes import booking_bp
except ImportError:  # Preserve `python backend/app.py` local startup.
    from config import Config
    from db import db
    from routes.auth_routes import auth_bp
    from routes.vehicle_routes import vehicle_bp
    from routes.booking_routes import booking_bp

PROJECT_ROOT = Path(__file__).resolve().parent.parent
FRONTEND_DIST = PROJECT_ROOT / "public"

def create_app():
    app = Flask(__name__)
    app.config.from_object(Config)

    CORS(app, resources={r"/api/*": {"origins": "*"}})

    db.init_app(app)
    app.register_blueprint(auth_bp)
    app.register_blueprint(vehicle_bp)
    app.register_blueprint(booking_bp)

    @app.get("/")
    def home():
        should_serve_frontend = (
            os.getenv("SERVE_FRONTEND", "false").lower() == "true"
            or os.getenv("VERCEL") == "1"
        )
        if should_serve_frontend and (FRONTEND_DIST / "index.html").is_file():
            return send_from_directory(str(FRONTEND_DIST), "index.html")
        return jsonify({
            "message": "PRIME RIDES API is running!",
            "status": "success"
        })

    @app.get("/<path:requested_path>")
    def frontend_routes(requested_path):
        should_serve_frontend = (
            os.getenv("SERVE_FRONTEND", "false").lower() == "true"
            or os.getenv("VERCEL") == "1"
        )
        if not should_serve_frontend or not (FRONTEND_DIST / "index.html").is_file():
            abort(404)

        if requested_path == "api" or requested_path.startswith("api/"):
            abort(404)

        try:
            return send_from_directory(str(FRONTEND_DIST), requested_path)
        except NotFound:
            # Vite's client router handles extensionless routes such as /login.
            if Path(requested_path).suffix:
                abort(404)
            return send_from_directory(str(FRONTEND_DIST), "index.html")

    @app.get("/api/health")
    def health():
        try:
            db.session.execute(text("SELECT 1"))
            required_tables = {
                "states", "cities", "users", "dealers", "vehicles", "bookings",
                "payments", "reviews", "contact_messages", "vehicle_documents", "favorites",
            }
            existing_tables = set(db.session.execute(text("""
                SELECT table_name
                FROM information_schema.tables
                WHERE table_schema = 'public'
            """)).scalars())
            missing_tables = sorted(required_tables - existing_tables)
            booking_columns = set(db.session.execute(text("""
                SELECT column_name
                FROM information_schema.columns
                WHERE table_schema = 'public' AND table_name = 'bookings'
            """)).scalars())
            required_booking_columns = {
                "user_id", "vehicle_id", "start_datetime", "end_datetime",
                "booking_type", "total_amount", "status",
            }
            missing_booking_columns = sorted(required_booking_columns - booking_columns)
            if missing_tables or missing_booking_columns:
                return jsonify({
                    "status": "unhealthy",
                    "database": "connected",
                    "missing_tables": missing_tables,
                    "missing_bookings_columns": missing_booking_columns,
                }), 503

            available_vehicles = db.session.execute(text("""
                SELECT COUNT(*) FROM vehicles WHERE availability = 'available'
            """)).scalar_one()
            return jsonify({
                "status": "healthy",
                "database": "connected",
                "schema": "ready",
                "available_vehicles": available_vehicles,
            }), 200
        except Exception:
            app.logger.exception("Database health check failed")
            db.session.rollback()
            return jsonify({
                "status": "unhealthy",
                "database": "disconnected"
            }), 503

    @app.get("/api/tables")
    def tables():
        try:
            result = db.session.execute(text("""
                SELECT table_name
                FROM information_schema.tables
                WHERE table_schema = 'public'
                ORDER BY table_name
            """))
            return jsonify({
                "tables": [row[0] for row in result]
            }), 200
        except Exception:
            app.logger.exception("Could not retrieve database tables")
            db.session.rollback()
            return jsonify({
                "error": "Could not retrieve tables"
            }), 500

    return app


app = create_app()

if __name__ == "__main__":
    app.run(
        host="0.0.0.0",
        port=int(os.getenv("PORT", 5000)),
        debug=False
    )
