from flask import Blueprint, request, jsonify
from sqlalchemy import text
try:
    from ..db import db
except ImportError:
    from db import db
from datetime import datetime, timezone

booking_bp = Blueprint("bookings", __name__, url_prefix="/api/bookings")


@booking_bp.route("/", methods=["POST"])
def create_booking():
    data = request.get_json(silent=True) or {}

    if not isinstance(data, dict):
        return jsonify({"error": "Request body must be a JSON object"}), 400

    user_id = data.get("user_id")
    vehicle_id = data.get("vehicle_id")
    start = data.get("start_datetime")
    end = data.get("end_datetime")
    booking_type = data.get("booking_type", "daily")

    if not all([user_id, vehicle_id, start, end]):
        return jsonify({"error": "All fields are required"}), 400

    try:
        user_id = int(user_id)
        vehicle_id = int(vehicle_id)
        if user_id < 1 or vehicle_id < 1:
            raise ValueError
    except (TypeError, ValueError):
        return jsonify({"error": "Invalid user or vehicle"}), 400

    if not isinstance(start, str) or not isinstance(end, str):
        return jsonify({"error": "Invalid date format"}), 400

    if booking_type not in ("hourly", "daily"):
        return jsonify({"error": "Invalid booking type"}), 400

    try:
        start_date = datetime.fromisoformat(start)
        end_date = datetime.fromisoformat(end)

        # PostgreSQL columns are TIMESTAMP WITHOUT TIME ZONE. Normalize
        # explicit offsets to UTC before storing; datetime-local values remain
        # local-naive for compatibility with the current form.
        if start_date.tzinfo is not None:
            start_date = start_date.astimezone(timezone.utc).replace(tzinfo=None)
        if end_date.tzinfo is not None:
            end_date = end_date.astimezone(timezone.utc).replace(tzinfo=None)

        if end_date <= start_date:
            return jsonify({"error": "Return time must be after pickup time"}), 400

        vehicle = db.session.execute(
            text("""
                SELECT id, price_per_hour, price_per_day
                FROM vehicles
                WHERE id = :vehicle_id
                  AND availability = 'available'
            """),
            {"vehicle_id": vehicle_id}
        ).mappings().first()

        if not vehicle:
            return jsonify({"error": "Vehicle is unavailable"}), 404

        user = db.session.execute(
            text("SELECT id FROM users WHERE id = :user_id"),
            {"user_id": user_id}
        ).first()

        if not user:
            return jsonify({"error": "Please log in before booking"}), 401

        seconds = (end_date - start_date).total_seconds()

        if booking_type == "hourly":
            units = max(1, int((seconds + 3599) // 3600))
            total = units * float(vehicle["price_per_hour"])
        else:
            units = max(1, int((seconds + 86399) // 86400))
            total = units * float(vehicle["price_per_day"])

        result = db.session.execute(
            text("""
                INSERT INTO bookings
                (user_id, vehicle_id, start_datetime, end_datetime,
                 booking_type, total_amount, status)
                VALUES
                (:user_id, :vehicle_id, :start, :end,
                 :booking_type, :total, 'pending')
                RETURNING id
            """),
            {
                "user_id": user_id,
                "vehicle_id": vehicle_id,
                "start": start_date,
                "end": end_date,
                "booking_type": booking_type,
                "total": total
            }
        )

        booking_id = result.scalar_one()
        db.session.commit()

        return jsonify({
            "message": "Booking created successfully",
            "booking_id": booking_id,
            "total_amount": total,
            "status": "pending"
        }), 201

    except ValueError:
        return jsonify({"error": "Invalid date format"}), 400

    except Exception:
        db.session.rollback()
        return jsonify({"error": "Unable to create booking"}), 500
