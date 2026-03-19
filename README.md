# 🛒 E-commerce Backend API

A RESTful backend system for an e-commerce application built with **Spring Boot**.
This project provides APIs for managing users, products, carts, and orders.

---

## 🚀 Features

* 🔐 JWT Authentication & Authorization
* 👤 User management
* 📦 Product CRUD (create, update, delete, search)
* 🛒 Shopping cart
* 📑 Order management
* 📄 Pagination & filtering

---

## 🛠️ Tech Stack

* Java 17
* Spring Boot
* Spring Security
* Spring Data JPA (Hibernate)
* MySQL
* Maven
* JWT (JSON Web Token)

---

## 📁 Project Structure

src/main/java/com/ecommerce
├── controller
├── service
├── repository
├── entity
├── dto
├── mapper
└── config

---

## ⚙️ Setup & Run

### 1. Clone project

git clone https://github.com/your-username/ecommerce-backend.git
cd ecommerce-backend

### 2. Configure database

Update in `application.properties`:

spring.datasource.url=jdbc:mysql://localhost:3306/ecommerce
spring.datasource.username=root
spring.datasource.password=your_password

### 3. Run project

mvn spring-boot:run

---

## 🔑 API Endpoints

### Auth

* POST /api/auth/login
* POST /api/auth/register

### Product

* GET /api/products
* GET /api/products/{id}
* POST /api/products
* PUT /api/products/{id}
* DELETE /api/products/{id}

### Cart

* POST /api/cart
* GET /api/cart

### Order

* POST /api/orders
* GET /api/orders

---

## 🔐 Authentication

Use JWT token in header:

Authorization: Bearer your_token_here

---

## 📌 Future Improvements

* Payment integration
* Admin dashboard
* Docker deployment
* Unit testing

---

## 👨‍💻 Author

* Your Name

---

## 📄 License

MIT License
