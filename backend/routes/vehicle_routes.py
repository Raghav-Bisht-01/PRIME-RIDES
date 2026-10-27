from flask import Blueprint, jsonify, request
from sqlalchemy import text
try:
    from ..db import db
except ImportError:
    from db import db

vehicle_bp = Blueprint("vehicles", __name__, url_prefix="/api/vehicles")


@vehicle_bp.get("/")
def get_vehicles():
    query = """
        SELECT
            v.id, v.vehicle_name, v.brand, v.model,
            v.vehicle_type, v.fuel_type, v.seats,
            v.price_per_hour, v.price_per_day,
            v.security_deposit, v.image, v.description,
            d.dealer_name, c.name AS city, s.name AS state
        FROM vehicles v
        JOIN dealers d ON d.id = v.dealer_id
        JOIN cities c ON c.id = d.city_id
        JOIN states s ON s.id = c.state_id
        WHERE v.availability = 'available'
    """

    params = {}

    vehicle_type = request.args.get("type")
    fuel_type = request.args.get("fuel")
    state = request.args.get("state")
    city = request.args.get("city")

    if vehicle_type:
        query += " AND LOWER(v.vehicle_type) = LOWER(:vehicle_type)"
        params["vehicle_type"] = vehicle_type

    if fuel_type:
        query += " AND LOWER(v.fuel_type) = LOWER(:fuel_type)"
        params["fuel_type"] = fuel_type

    if state:
        query += " AND LOWER(s.name) = LOWER(:state)"
        params["state"] = state

    if city:
        query += " AND LOWER(c.name) = LOWER(:city)"
        params["city"] = city

    query += " ORDER BY v.id"

    try:
        rows = db.session.execute(text(query), params).mappings().all()
        return jsonify({"vehicles": [dict(row) for row in rows]}), 200

    except Exception:
        db.session.rollback()
        return jsonify({"error": "Unable to load vehicles"}), 500
