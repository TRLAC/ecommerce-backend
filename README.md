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
* SQL Server
* Maven
* JWT (JSON Web Token)

---

## 📁 Project Structure

src/main/java/com/ecommerce
* config
* controller
* dto/filter
* dto/request
* dto/response
* entity
* enums
* exception
* mapper
* repository
* security
* service

---

## ⚙️ Setup & Run

### 1. Clone project

git clone https://github.com/TRLAC/ecommerce-backend.git
cd ecommerce-backend

### 2. Configure database

Update in `application.properties`:

spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=shop;encrypt=true;trustServerCertificate=true
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
