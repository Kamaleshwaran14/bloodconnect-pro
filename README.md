# BloodConnect Pro

## GitHub Description

BloodConnect Pro is a secure role-based blood coordination and emergency response platform connecting donors, hospitals, and blood banks. Built with React, Spring Boot, JWT authentication, email notifications, inventory management, and real-time emergency request handling.

---

# BloodConnect Pro

BloodConnect Pro is a secure, role-based blood coordination and emergency response platform designed to connect verified donors, hospitals, and blood banks through a centralized digital infrastructure.

The platform enables emergency blood request handling, inventory coordination, donor management, institutional verification, and secure communication workflows.

---

# Features

## Authentication & Security
- JWT-based authentication
- Role-based authorization
- Protected routes
- Secure API access
- Admin approval workflow

---

## User Roles

### Donor
- Register as donor
- Maintain availability status
- Receive emergency alerts
- Respond to blood requests

### Hospital
- Raise emergency blood requests
- Search donors and blood banks
- Manage blood inventory
- Emergency coordination system

### Blood Bank
- Manage blood stock
- Respond to hospital requests
- Maintain inventory records

### Admin
- Verify institutions and donors
- Approve/reject registrations
- Block/unblock users
- Monitor platform activity

---

# Tech Stack

## Frontend
- React.js
- React Router
- Tailwind CSS
- Axios

## Backend
- Spring Boot
- Spring Security
- JWT Authentication
- Spring Data JPA
- Java Mail Sender

## Database
- MySQL

---

# System Architecture

Frontend (React) communicates with Spring Boot REST APIs.

Authentication is handled using JWT tokens.

All platform activities are role-based and secured through protected endpoints.

---

# Folder Structure

## Frontend

```bash
frontend/
├── src/
├── components/
├── pages/
├── services/
└── App.jsx
```

## Backend

```bash
backend/
├── controller/
├── service/
├── repository/
├── model/
├── dto/
└── security/
```

---

# Installation & Setup

## 1. Clone Repository

```bash
git clone https://github.com/your-username/bloodconnect-pro.git
```

---

# Backend Setup (Spring Boot)

## Navigate to backend folder

```bash
cd backend
```

## Configure MySQL Database

Update `application.properties`

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/bloodconnect
spring.datasource.username=root
spring.datasource.password=your_password

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

---

## Configure Email

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your_email@gmail.com
spring.mail.password=your_app_password

spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

---

## Run Backend

```bash
mvn spring-boot:run
```

Backend runs on:

```bash
http://localhost:8080
```

---

# Frontend Setup (React)

## Navigate to frontend folder

```bash
cd frontend
```

## Install Dependencies

```bash
npm install
```

## Run Frontend

```bash
npm run dev
```

Frontend runs on:

```bash
http://localhost:5173
```

---

# API Base URL

```javascript
http://localhost:8080/api
```

---

# Main Functionalities

- Emergency blood request handling
- Smart donor matching
- Blood inventory management
- Role-based dashboards
- Email notifications
- File/document verification
- Admin approval system
- Secure institutional coordination

---

# Security Features

- JWT token validation
- Route protection
- Role-based access control
- Secure document handling
- Admin verification workflow

---

# Future Enhancements

- Real-time notifications
- Predictive shortage analytics
- AI-based donor matching
- Mobile application support
- Multi-city deployment
- Government health integration

---

# Contact

📧 bloodconnectpro@gmail.com

---

# License

This project is developed for educational and healthcare coordination purposes.