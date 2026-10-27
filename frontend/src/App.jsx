import { useEffect, useRef, useState } from "react";
import { API_URL } from "./config";

function App() {
  const [location, setLocation] = useState("All locations");
  const [vehicleType, setVehicleType] = useState("All vehicles");
  const [vehicles, setVehicles] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [selectedVehicle, setSelectedVehicle] = useState(null);
  const [bookingType, setBookingType] = useState("daily");
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");
  const [bookingMessage, setBookingMessage] = useState("");
  const [bookingLoading, setBookingLoading] = useState(false);
  const bookingFormRef = useRef(null);


  useEffect(() => {
    fetch(`${API_URL}/api/vehicles/`)
      .then((response) => {
        if (!response.ok) throw new Error("Could not load vehicles");
        return response.json();
      })
      .then((data) => setVehicles(data.vehicles || []))
      .catch(() => setError("Unable to load vehicles. Please check the backend."))
      .finally(() => setLoading(false));
  }, []);

  const filteredVehicles = vehicles.filter((vehicle) => {
    const matchesType =
      vehicleType === "All vehicles" ||
      vehicle.vehicle_type?.toLowerCase() === vehicleType.toLowerCase();

    const matchesLocation =
      location === "All locations" ||
      vehicle.city?.toLowerCase() === location.toLowerCase() ||
      vehicle.state?.toLowerCase() === location.toLowerCase();

    return matchesType && matchesLocation;
  });

  const formatPrice = (price) =>
    `₹${Number(price || 0).toLocaleString("en-IN")}`;

  const localDateTime = (date) => {
    const local = new Date(date.getTime() - date.getTimezoneOffset() * 60_000);
    return local.toISOString().slice(0, 16);
  };

  const getType = (type) =>
    type?.charAt(0).toUpperCase() + type?.slice(1);

  const getFallbackImage = (vehicle) => {
    if (vehicle.vehicle_type === "car") return "/images/cars/rental-suv.jpg";
    if (vehicle.vehicle_type === "bike") return "/images/bikes/rental-motorcycle.jpg";
    return "/images/scooties/rental-scooter.jpg";
  };

  const getImage = (vehicle) => vehicle.image || getFallbackImage(vehicle);
    useEffect(() => {
    if (selectedVehicle && bookingFormRef.current) {
      bookingFormRef.current.scrollIntoView({
        behavior: "smooth",
        block: "center",
      });
    }
  }, [selectedVehicle]);

    async function handleBooking(event) {
    event.preventDefault();
    setBookingMessage("");

    const user = JSON.parse(localStorage.getItem("user") || "null");

    if (!user) {
      window.location.href = "/login";
      return;
    }

    if (new Date(endDate) <= new Date(startDate)) {
      setBookingMessage("Return date must be after pickup date.");
      return;
    }

    setBookingLoading(true);

    try {
      const response = await fetch(`${API_URL}/api/bookings/`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          user_id: user.id,
          vehicle_id: selectedVehicle.id,
          start_datetime: startDate,
          end_datetime: endDate,
          booking_type: bookingType,
        }),
      });

      const data = await response.json();

      if (!response.ok) {
        setBookingMessage(data.error || "Booking failed.");
        return;
      }

      setBookingMessage(
        `Booking confirmed! Booking ID: ${data.booking_id}. Total: ${formatPrice(data.total_amount)}. Status: ${data.status}.`
      );
    } catch {
      setBookingMessage("Could not connect to the booking server.");
    } finally {
      setBookingLoading(false);
    }
  }


  return (
    <div className="site-wrapper">
      <header className="navbar">
        <a className="brand" href="#home">
          <span className="brand-icon">P</span>
          <span>PRIME <strong>RIDES</strong></span>
        </a>

        <nav className="nav-links">
          <a href="#home">Home</a>
          <a href="#vehicles">Explore Vehicles</a>
          <a href="#why-us">Why Prime Rides?</a>
        </nav>

        <div className="nav-actions">
          <button
            className="login-button"
            onClick={() => (window.location.href = "/login")}
          >
            Log in
          </button>

          <button
            className="signup-button"
            onClick={() => (window.location.href = "/register")}
          >
            Sign up
          </button>
        </div>
      </header>

      <main>
        <section className="hero" id="home">
          <div className="hero-content">
            <span className="eyebrow">YOUR JOURNEY. YOUR RULES.</span>

            <h1>
              Find your next
              <br />
              <span>great ride.</span>
            </h1>

            <p>
              From everyday city rides to unforgettable road trips,
              find the perfect vehicle for every journey.
            </p>

            <a className="primary-button" href="#vehicles">
              Explore vehicles <span>→</span>
            </a>

            <div className="hero-stats">
              <div>
                <strong>Easy</strong>
                <span>Online booking</span>
              </div>
              <div>
                <strong>Your choice</strong>
                <span>Multiple vehicles</span>
              </div>
              <div>
                <strong>Flexible</strong>
                <span>Rental options</span>
              </div>
            </div>
          </div>

          <div className="hero-visual">
            <div className="hero-orbit orbit-one"></div>
            <div className="hero-orbit orbit-two"></div>

            <div className="hero-card">
              <span className="hero-card-label">YOUR NEXT ADVENTURE</span>
              <div className="hero-car-icon">
                <img src="/images/cars/rental-suv.jpg" alt="Dark blue SUV ready for a trip" />
              </div>
              <div className="hero-card-bottom">
                <div>
                  <strong>Ready when you are.</strong>
                  <span>Your journey starts here.</span>
                </div>
                <span className="hero-arrow">↗</span>
              </div>
            </div>

            <div className="floating-badge">
              <span>✦</span>
              Find your freedom
            </div>
          </div>
        </section>

        <section className="search-section">
          <div className="search-heading">
            <span className="section-label">LET'S GET YOU MOVING</span>
            <h2>Find your perfect ride</h2>
          </div>

          <div className="search-controls">
            <label>
              <span>📍 Pickup location</span>
              <select
                value={location}
                onChange={(event) => setLocation(event.target.value)}
              >
                <option>All locations</option>
                <option>Chandigarh</option>
                <option>Ludhiana</option>
                <option>Gurugram</option>
                <option>Faridabad</option>
                <option>New Delhi</option>
                <option>Dwarka</option>
                <option>Jaipur</option>
                <option>Udaipur</option>
                <option>Lucknow</option>
                <option>Noida</option>
                <option>Punjab</option>
                <option>Haryana</option>
                <option>Delhi</option>
                <option>Rajasthan</option>
                <option>Uttar Pradesh</option>
              </select>
            </label>

            <label>
              <span>🚗 Vehicle category</span>
              <select
                value={vehicleType}
                onChange={(event) => setVehicleType(event.target.value)}
              >
                <option>All vehicles</option>
                <option>Car</option>
                <option>Bike</option>
                <option>Scooty</option>
              </select>
            </label>

            <button
              className="search-button"
              onClick={() =>
                document
                  .getElementById("vehicles")
                  ?.scrollIntoView({ behavior: "smooth" })
              }
            >
              Find a ride <span>→</span>
            </button>
          </div>
        </section>

        <section className="vehicles-section" id="vehicles">
          <div className="section-heading-row">
            <div>
              <span className="section-label">PICK YOUR RIDE</span>
              <h2>Explore our vehicles</h2>
              <p>Find a ride that fits your plans and your style.</p>
            </div>

            <span className="vehicle-count">
              {filteredVehicles.length} vehicles
            </span>
          </div>

          {loading && <p>Loading vehicles...</p>}
          {error && <p role="alert">{error}</p>}

          {!loading && !error && filteredVehicles.length === 0 && (
            <p>No vehicles found for this selection.</p>
          )}

          <div className="vehicle-grid">
            {filteredVehicles.map((vehicle) => (
              <article className="vehicle-card" key={vehicle.id}>
                <div className="vehicle-image">
                  <img
                    src={getImage(vehicle)}
                    alt={vehicle.vehicle_name}
                    loading="lazy"
                    decoding="async"
                    onError={(event) => {
                      if (!event.currentTarget.dataset.fallbackTried) {
                        event.currentTarget.dataset.fallbackTried = "true";
                        event.currentTarget.src = getFallbackImage(vehicle);
                      } else {
                        event.currentTarget.style.display = "none";
                        event.currentTarget.nextElementSibling.style.display = "flex";
                      }
                    }}
                  />

                  <div className="vehicle-placeholder">
                    {vehicle.vehicle_type === "car"
                      ? "🚘"
                      : vehicle.vehicle_type === "bike"
                        ? "🏍️"
                        : "🛵"}
                  </div>

                  <span className="vehicle-type">
                    {getType(vehicle.vehicle_type)}
                  </span>
                </div>

                <div className="vehicle-details">
                  <h3>{vehicle.vehicle_name}</h3>
                  <p>{vehicle.description}</p>
                  <p>
                    📍 {vehicle.city}, {vehicle.state}
                  </p>
                  <p>
                    ⛽ {getType(vehicle.fuel_type)} · Deposit:{" "}
                    {formatPrice(vehicle.security_deposit)}
                  </p>

                  <div className="vehicle-footer">
                    <div>
                      <strong>
                        {formatPrice(
                          vehicle.vehicle_type === "car"
                            ? vehicle.price_per_day
                            : vehicle.price_per_day
                        )}
                      </strong>
                      <span> / day</span>
                    </div>

                    <button
                      className="book-button"
                      onClick={() => {
                        setSelectedVehicle(vehicle);
                        setBookingMessage("");
                        setStartDate("");
                        setEndDate("");
                        setBookingType("daily");
                      }}
                    >
                      View ride ↗
                    </button>
                  </div>
                </div>
              </article>
            ))}
          </div>
        </section>
        {selectedVehicle && (
          <div className="auth-page" ref={bookingFormRef}>
            <form className="auth-card" onSubmit={handleBooking}>
              <h2>Book {selectedVehicle.vehicle_name}</h2>

              <p>
                📍 {selectedVehicle.city}, {selectedVehicle.state}
              </p>

              <p>
                Security deposit:{" "}
                {formatPrice(selectedVehicle.security_deposit)}
              </p>

              <label>
                Rental type
                <select
                  value={bookingType}
                  onChange={(event) => setBookingType(event.target.value)}
                >
                  <option value="daily">Daily rental</option>
                  {selectedVehicle.vehicle_type === "car" && (
                    <option value="hourly">Hourly rental</option>
                  )}
                </select>
              </label>

              <label>
                Pickup date and time
                <input
                  type="datetime-local"
                  value={startDate}
                  min={localDateTime(new Date())}
                  onChange={(event) => setStartDate(event.target.value)}
                  required
                />
              </label>

              <label>
                Return date and time
                <input
                  type="datetime-local"
                  value={endDate}
                  min={startDate || localDateTime(new Date())}
                  onChange={(event) => setEndDate(event.target.value)}
                  required
                />
              </label>

              <p>
                Rental rate:{" "}
                {formatPrice(
                  bookingType === "hourly"
                    ? selectedVehicle.price_per_hour
                    : selectedVehicle.price_per_day
                )}
                {bookingType === "hourly" ? " / hour" : " / day"}
              </p>

              {bookingMessage && <p role="status">{bookingMessage}</p>}

              <button
                className="signup-button auth-submit"
                type="submit"
                disabled={bookingLoading}
              >
                {bookingLoading ? "Submitting..." : "Confirm booking"}
              </button>

              <button
                type="button"
                className="login-button auth-submit"
                onClick={() => setSelectedVehicle(null)}
              >
                Cancel
              </button>
            </form>
          </div>
        )}

        <section className="why-section" id="why-us">
          <span className="section-label">THE PRIME DIFFERENCE</span>
          <h2>Your ride, your way.</h2>

          <div className="benefit-grid">
            <article>
              <span>✦</span>
              <h3>Plenty of choices</h3>
              <p>Explore cars, bikes and scooties for different journeys.</p>
            </article>

            <article>
              <span>◷</span>
              <h3>Flexible rentals</h3>
              <p>Find rental options suited to your travel plans.</p>
            </article>

            <article>
              <span>♡</span>
              <h3>Made for you</h3>
              <p>A simple way to discover your next ride.</p>
            </article>
          </div>
        </section>
      </main>

      <footer className="footer">
        <a className="brand footer-brand" href="#home">
          <span className="brand-icon">P</span>
          <span>PRIME <strong>RIDES</strong></span>
        </a>

        <p>Every journey begins somewhere. Start yours with Prime Rides.</p>
        <span>© {new Date().getFullYear()} Prime Rides</span>
      </footer>
    </div>
  );
}

export default App;
