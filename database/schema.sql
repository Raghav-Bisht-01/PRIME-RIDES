-- PRIME RIDES DATABASE SCHEMA
-- Select the prime_rides database in MySQL Workbench before running this file.


-- 1. STATES

CREATE TABLE states (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);


-- 2. CITIES

CREATE TABLE cities (
    id INT AUTO_INCREMENT PRIMARY KEY,
    state_id INT NOT NULL,
    name VARCHAR(100) NOT NULL,
    FOREIGN KEY (state_id) REFERENCES states(id)
        ON DELETE CASCADE,
    UNIQUE (state_id, name)
);


-- 3. USERS

CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    phone VARCHAR(15) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'user',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


-- 4. DEALERS

CREATE TABLE dealers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    dealer_name VARCHAR(150) NOT NULL,
    phone VARCHAR(15) NOT NULL,
    email VARCHAR(150),
    address VARCHAR(255),
    city_id INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE,

    FOREIGN KEY (city_id) REFERENCES cities(id)
        ON DELETE RESTRICT
);


-- 5. VEHICLES

CREATE TABLE vehicles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    dealer_id INT NOT NULL,

    vehicle_name VARCHAR(100) NOT NULL,
    brand VARCHAR(100) NOT NULL,
    model VARCHAR(100),
    model_year INT,

    vehicle_type VARCHAR(20) NOT NULL,
    fuel_type VARCHAR(20) NOT NULL,

    seats INT DEFAULT 2,

    registration_number VARCHAR(30) NOT NULL UNIQUE,

    price_per_hour DECIMAL(10,2) DEFAULT 0.00,
    price_per_day DECIMAL(10,2) DEFAULT 0.00,
    security_deposit DECIMAL(10,2) NOT NULL DEFAULT 0.00,

    image VARCHAR(255),

    availability VARCHAR(20) DEFAULT 'available',

    description TEXT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (dealer_id) REFERENCES dealers(id)
        ON DELETE CASCADE
);


-- 6. BOOKINGS

CREATE TABLE bookings (
    id INT AUTO_INCREMENT PRIMARY KEY,

    user_id INT NOT NULL,
    vehicle_id INT NOT NULL,

    start_datetime DATETIME NOT NULL,
    end_datetime DATETIME NOT NULL,

    booking_type VARCHAR(20) NOT NULL,

    total_amount DECIMAL(10,2) NOT NULL,

    status VARCHAR(20) DEFAULT 'pending',

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE,

    FOREIGN KEY (vehicle_id) REFERENCES vehicles(id)
        ON DELETE CASCADE
);


-- 7. PAYMENTS

CREATE TABLE payments (
    id INT AUTO_INCREMENT PRIMARY KEY,

    booking_id INT NOT NULL,

    amount DECIMAL(10,2) NOT NULL,

    payment_method VARCHAR(30) NOT NULL,

    transaction_id VARCHAR(100) UNIQUE,

    payment_status VARCHAR(20) DEFAULT 'pending',

    paid_at TIMESTAMP NULL,

    FOREIGN KEY (booking_id) REFERENCES bookings(id)
        ON DELETE CASCADE
);


-- 8. REVIEWS

CREATE TABLE reviews (
    id INT AUTO_INCREMENT PRIMARY KEY,

    user_id INT NOT NULL,

    vehicle_id INT NOT NULL,

    rating INT NOT NULL,

    comment TEXT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE,

    FOREIGN KEY (vehicle_id) REFERENCES vehicles(id)
        ON DELETE CASCADE
);


-- 9. CONTACT MESSAGES

CREATE TABLE contact_messages (
    id INT AUTO_INCREMENT PRIMARY KEY,

    name VARCHAR(100) NOT NULL,

    email VARCHAR(150) NOT NULL,

    phone VARCHAR(15),

    subject VARCHAR(200),

    message TEXT NOT NULL,

    status VARCHAR(20) DEFAULT 'new',

    created_at TIMESTAMP DEFAUlT CURRENT_TIMESTAMP
);


-- 10. VEHICLE DOCUMENTS

CREATE TABLE vehicle_documents (
    id INT AUTO_INCREMENT PRIMARY KEY,

    vehicle_id INT NOT NULL,

    document_type VARCHAR(30) NOT NULL,

    document_number VARCHAR(100),

    document_file VARCHAR(255),

    verified BOOLEAN DEFAULT FALSE,

    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (vehicle_id) REFERENCES vehicles(id)
        ON DELETE CASCADE
);


-- 11. FAVORITES

CREATE TABLE favorites (
    id INT AUTO_INCREMENT PRIMARY KEY,

    user_id INT NOT NULL,

    vehicle_id INT NOT NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE,

    FOREIGN KEY (vehicle_id) REFERENCES vehicles(id)
        ON DELETE CASCADE,

    UNIQUE (user_id, vehicle_id)
);
