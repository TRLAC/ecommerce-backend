# 🛒 E-commerce Backend API

A RESTful backend system for an e-commerce application built with **Spring Boot**.
This project provides APIs for managing users, products, carts, and orders.

---

## 🚀 Key Features

* 🔐 JWT Authentication (Access + Refresh Token) & Authorization
* 🔁 Refresh Token & Token Rotation
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

## 🔑 API Endpoints

### Auth

* POST /api/auth/login
* POST /api/auth/register
* POST /api/auth/refresh
* POST /api/auth/logout

### Profile

* GET /api/profile - getProfile
* PUT /api/profile - updateProfile


### Product
**ADMIN**
* POST /api/admin/products - createProduct
* PUT /api/admin/products/{id} - updateProduct
* POST /api/admin/products/{id}/image - uploadImage
* PATCH /api/admin/products/{id}/hide - deleteProduct

**USER**
* GET  /api/products - getAll
* GET  /api/products/search - searchProducts
* GET  /api/products/{id} - getProductById

### Cart
**USER**
* GET /api/cart - getCart
* POST /api/cart/items - addToCart
* PUT /api/cart/items/{cartItemId} - updateCartItem
* DELETE /api/cart/items/{cartItemId} - removeCartItem
* DELETE /api/cart - clearCart

### Category
**ADMIN**
* GET /api/admin/categories - getRootCategories
* GET /api/admin/categories/{id} - getCategoryById
* POST /api/admin/categories - createCategory
* PUT /api/admin/categories/{id} - updateCategory
* PATCH /api/admin/categories/{id}/hide - deleteCategory

**USER**
* GET /api/categories/{id} - getCategoryById
* GET /api/categories/roots - getRootCategories
* GET /api/categories/search - searchCategories


### Order

**ADMIN**
* GET /api/admin/orders - getAllOrders
* GET /api/admin/orders/{id} - getOrderDetail
* PATCH /api/admin/orders/{id}/status - updateOrderStatus

**USER**
* POST /api/orders - placeOrder
* GET /api/orders/{id} - viewOrder
* GET /api/orders/my - getMyOrders
* PATCH /api/orders/{id}/cancel - cancelOrder

---

## ⚙️ Setup & Run

### 1. Clone project

git clone https://github.com/TRLAC/ecommerce-backend.git
cd ecommerce-backend

### 2. Configure database

Update in `application.properties`:

* spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=shop;encrypt=true;trustServerCertificate=true
* spring.datasource.username=YOUR_DB_USERNAME
* spring.datasource.password=YOUR_DB_PASSWORD

### 3. Run project

* mvn spring-boot:run

---

## 🔐 Authentication

Use JWT access token in header:

**Authorization**: Bearer your_token_here

=> Access tokens are short-lived; refresh tokens are required to obtain new access tokens.

---

## 📌 Future Improvements

### 🔐 Authentication & Security
* Improve refresh token management (expiration, multi-device support)

### 🛒 Order & Business Logic
* Complete order lifecycle (status, cancellation)
* Add stock validation when placing orders

### ⚡ Performance
* Optimize database queries and pagination

### 🧪 Testing
* Add unit tests for service layer

### 🚀 Deployment
* Dockerize application

### 💳 Payment
* Integrate payment gateway (VNPay / Stripe)

---

## 👨‍💻 Author

 **Lạc Quan**  
* Backend Developer (Java | Spring Boot)  
* GitHub: https://github.com/TRLAC

---

## 📄 License

MIT License
