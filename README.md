# 🍔 CampusFood

A full-stack **campus food ordering application** consisting of an **Android mobile app** (Kotlin + Jetpack Compose) and a **Spring Boot REST API** backend connected to a MySQL database on AWS RDS.

> **🔧 Recent Fixes (April 2026):** Image upload 500 error and Google Sign-In issues have been fixed! See [SUMMARY.md](SUMMARY.md) for details.

---

## 📋 Table of Contents

- [Project Overview](#project-overview)
- [Recent Fixes](#recent-fixes)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Features](#features)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Backend Setup](#backend-setup)
  - [Android App Setup](#android-app-setup)
- [API Reference](#api-reference)
- [Database Schema](#database-schema)
- [Configuration](#configuration)
- [Screenshots](#screenshots)
- [Contributing](#contributing)

---

## 🔧 Recent Fixes

### Issues Resolved ✅

1. **Image Upload 500 Error** - Fixed AWS S3 configuration and error handling
2. **Google Sign-In Not Working** - Added dedicated backend endpoint and proper OAuth flow

### Documentation

- **[SUMMARY.md](SUMMARY.md)** - Quick overview of fixes
- **[QUICK_START.md](QUICK_START.md)** - Get started quickly
- **[FIXES_APPLIED.md](FIXES_APPLIED.md)** - Detailed technical documentation
- **[GOOGLE_SIGNIN_SETUP.md](GOOGLE_SIGNIN_SETUP.md)** - Google Sign-In configuration guide
- **[TESTING_CHECKLIST.md](TESTING_CHECKLIST.md)** - Comprehensive testing guide
- **[DOCUMENTATION_INDEX.md](DOCUMENTATION_INDEX.md)** - Documentation overview

### Quick Setup

```powershell
# 1. Setup AWS credentials
cd backend
.\setup-aws-credentials.ps1

# 2. Start backend
.\mvnw.cmd spring-boot:run

# 3. Setup Google Sign-In (see GOOGLE_SIGNIN_SETUP.md)
# 4. Build and run Android app
```

---

## 📖 Project Overview

CampusFood is a campus food ordering platform that allows students to:
- Browse available food items by category
- Add items to a shopping cart
- Place and track orders in real time
- View order history with status updates

Admins can manage products, view/update all orders, control inventory, and manage delivery partners.

---

## 🏗️ Architecture

```
┌──────────────────────────┐         ┌──────────────────────────┐
│   Android App (Client)   │  HTTP   │  Spring Boot REST API    │
│                          │◄───────►│                          │
│  Kotlin + Compose + MVVM │         │  Java 17 + Spring Boot 3 │
│  Retrofit + Moshi        │         │  Spring Data JPA + MySQL │
└──────────────────────────┘         └──────────────┬───────────┘
                                                    │
                                       ┌────────────▼────────────┐
                                       │   MySQL on AWS RDS       │
                                       │   (eu-north-1)           │
                                       └──────────────────────────┘
```

### Android App Architecture (MVVM)
```
UI Layer (Compose Screens)
        │
ViewModel Layer (StateFlow + viewModelScope)
        │
Network Layer (Retrofit + Moshi + OkHttp)
        │
REST API (Spring Boot Backend)
```

---

## 🛠️ Tech Stack

### Android App
| Technology | Version | Purpose |
|---|---|---|
| Kotlin | 2.x | Primary language |
| Jetpack Compose | BOM managed | Declarative UI |
| Material 3 | latest | UI components & theming |
| Navigation Compose | latest | Screen navigation |
| Retrofit 2 | latest | HTTP client |
| Moshi | latest | JSON serialization |
| OkHttp + Logging Interceptor | latest | HTTP logging |
| Coil | latest | Async image loading |
| Room | latest | Local database (offline) |
| DataStore Preferences | latest | Persistent key-value storage |
| Coroutines + StateFlow | latest | Async / reactive state |
| CameraX | latest | Camera integration |
| Play Services Location | latest | GPS / location |
| Accompanist Permissions | latest | Runtime permission handling |
| Android minSdk | 24 | Android 7.0+ |
| Android targetSdk | 36 | Latest Android |
| KSP | latest | Annotation processing |

### Backend
| Technology | Version | Purpose |
|---|---|---|
| Java | 17 | Primary language |
| Spring Boot | 3.5.0 | Application framework |
| Spring Web | - | REST API layer |
| Spring Data JPA | - | ORM / data access |
| Spring Validation | - | Request validation |
| Spring DevTools | - | Hot reload (dev) |
| Hibernate | - | JPA implementation |
| MySQL Connector | - | JDBC driver |
| Lombok | - | Boilerplate reduction |
| Maven | - | Build tool |

### Infrastructure
| Service | Purpose |
|---|---|
| AWS RDS (MySQL) | Production database |
| HikariCP | Connection pooling |

---

## 📁 Project Structure

```
CampusFood/
├── app/                            # Android Application
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/com/example/campusfood/
│   │   │   ├── MainActivity.kt              # Entry point
│   │   │   ├── model/                       # Data models
│   │   │   │   ├── Product.kt
│   │   │   │   ├── CartItem.kt
│   │   │   │   └── Order.kt
│   │   │   ├── network/                     # API layer
│   │   │   │   ├── ApiService.kt            # Retrofit interface
│   │   │   │   └── RetrofitInstance.kt      # Retrofit singleton
│   │   │   └── ui/                          # UI layer
│   │   │       ├── MainScreen.kt            # Bottom nav host
│   │   │       ├── Navigation.kt            # Nav graph
│   │   │       ├── components/
│   │   │       │   └── ProductCard.kt       # Reusable card
│   │   │       ├── screens/
│   │   │       │   ├── MenuScreen.kt        # Food browse screen
│   │   │       │   ├── MenuViewModel.kt
│   │   │       │   ├── CartScreen.kt        # Shopping cart
│   │   │       │   ├── CartViewModel.kt
│   │   │       │   ├── OrderScreen.kt       # Order history
│   │   │       │   ├── OrderViewModel.kt
│   │   │       │   └── ProfileScreen.kt     # User profile
│   │   │       └── theme/                   # Material3 theme
│   │   └── res/
│   └── build.gradle.kts
│
├── backend/                        # Spring Boot REST API
│   ├── src/main/
│   │   ├── java/com/campusfood/
│   │   │   ├── CampusFoodBackendApplication.java   # Entry point
│   │   │   ├── controller/
│   │   │   │   ├── AuthController.java      # /api/auth/*
│   │   │   │   ├── ProductController.java   # /api/products/*
│   │   │   │   ├── OrderController.java     # /api/orders/*
│   │   │   │   ├── DeliveryController.java  # /api/delivery/*
│   │   │   │   └── AdminController.java     # /api/admin/*
│   │   │   ├── service/
│   │   │   │   ├── UserService.java
│   │   │   │   ├── ProductService.java
│   │   │   │   ├── OrderService.java
│   │   │   │   └── DeliveryService.java
│   │   │   ├── repository/
│   │   │   │   ├── (JPA Repositories)
│   │   │   ├── entity/
│   │   │   │   ├── User.java
│   │   │   │   ├── Product.java
│   │   │   │   ├── Order.java
│   │   │   │   ├── OrderItem.java
│   │   │   │   ├── Inventory.java
│   │   │   │   └── Delivery.java
│   │   │   ├── dto/
│   │   │   │   ├── request/                 # Incoming payloads
│   │   │   │   └── response/                # Outgoing payloads
│   │   │   ├── enums/
│   │   │   │   ├── UserRole.java            # STUDENT | ADMIN | DELIVERY
│   │   │   │   ├── OrderStatus.java         # PENDING | PREPARING | COMPLETED
│   │   │   │   └── DeliveryStatus.java
│   │   │   ├── exception/                   # Custom exceptions
│   │   │   └── config/                      # CORS, security config
│   │   └── resources/
│   │       └── application.properties
│   └── pom.xml
│
├── build.gradle.kts                # Root Gradle config
├── settings.gradle.kts
└── README.md
```

---

## ✨ Features

### 👨‍🎓 Student (Mobile App)
| Feature | Description |
|---|---|
| 🍽️ Browse Menu | View all food items with images, descriptions, prices |
| 🔍 Search & Filter | Search by name, filter by category |
| 🛒 Cart Management | Add/remove items, view cart total |
| ✅ Checkout | Place orders with one tap |
| 📦 Order Tracking | View order history and live status |
| 👤 Profile | View user profile and logout |

### 🛠️ Admin (REST API)
| Feature | Endpoint |
|---|---|
| Product CRUD | `POST/PUT/DELETE /api/admin/products` |
| Inventory Update | `PUT /api/admin/inventory/{productId}` |
| Order Management | `GET/PUT /api/admin/orders` |
| Delivery Partners | `GET /api/admin/delivery-partners` |

### 🚴 Delivery
| Feature | Endpoint |
|---|---|
| View Assigned Orders | `GET /api/delivery/orders` |
| Update Delivery Status | `PUT /api/delivery/{id}/status` |

---

## 🚀 Getting Started

### Prerequisites

| Tool | Version |
|---|---|
| Android Studio | Ladybug (2024.x) or newer |
| JDK | 17 |
| Maven | 3.9+ |
| MySQL | 8.0+ (or AWS RDS MySQL) |
| Android Device / Emulator | API 24+ |

---

### Backend Setup

1. **Clone the repository:**
   ```bash
   git clone https://github.com/your-username/CampusFood.git
   cd CampusFood/backend
   ```

2. **Configure the database** in `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/campusfood
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   ```

3. **Create the database:**
   ```sql
   CREATE DATABASE campusfood;
   ```

4. **Run the application:**
   ```bash
   ./mvnw spring-boot:run
   ```
   The API will start at: `http://localhost:8080`

5. **Verify the server is running:**
   ```bash
   curl http://localhost:8080/api/products
   ```

---

### Android App Setup

1. **Open the project in Android Studio:**
   - Open Android Studio → `File > Open` → Select the root `CampusFood/` directory

2. **Set the correct base URL** in `app/src/main/java/com/example/campusfood/network/RetrofitInstance.kt`:
   ```kotlin
   // For Android Emulator connecting to local backend:
   private const val BASE_URL = "http://10.0.2.2:8080/api/"

   // For real device on same WiFi network:
   // private const val BASE_URL = "http://YOUR_PC_LOCAL_IP:8080/api/"

   // For production:
   // private const val BASE_URL = "https://api.campusfood.com/api/"
   ```

3. **Sync Gradle** by clicking **"Sync Now"** in the notification bar or:
   `File > Sync Project with Gradle Files`

4. **Build and run** on an emulator or connected device:
   - Select your target device from the toolbar
   - Click ▶️ **Run** (Shift+F10)

> **Note:** If connecting a physical device, make sure both your PC and Android device are on the **same WiFi network** and use your PC's local IP address instead of `10.0.2.2`.

---

## 📡 API Reference

**Base URL:** `http://localhost:8080/api`

### Authentication — `/api/auth`
| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `POST` | `/auth/register` | Register a new user | No |
| `POST` | `/auth/login` | Login and get user info | No |
| `GET` | `/auth/user/{id}` | Get user by ID | No |

### Products — `/api/products`
| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `GET` | `/products` | List all available products | No |
| `GET` | `/products/{id}` | Get product by ID | No |
| `GET` | `/products/category/{category}` | Filter by category | No |

### Orders — `/api/orders`
| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `POST` | `/orders` | Place a new order | User |
| `GET` | `/orders` | Get current user's orders | User |
| `GET` | `/orders/{id}` | Get order by ID | User |

### Admin — `/api/admin`
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/admin/products` | Create a product |
| `PUT` | `/admin/products/{id}` | Update a product |
| `DELETE` | `/admin/products/{id}` | Delete a product |
| `PUT` | `/admin/inventory/{productId}` | Update product inventory |
| `GET` | `/admin/orders` | Get all orders (paginated) |
| `PUT` | `/admin/orders/{id}/status` | Update order status |
| `GET` | `/admin/delivery-partners` | List all delivery partners |

### Delivery — `/api/delivery`
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/delivery/orders` | View assigned deliveries |
| `PUT` | `/delivery/{id}/status` | Update delivery status |

---

## 🗄️ Database Schema

```
users
├── id (PK)
├── name
├── mobile (UNIQUE, INDEXED)
├── email (UNIQUE)
├── password_hash
├── role (STUDENT | ADMIN | DELIVERY, INDEXED)
├── active
├── created_at
└── updated_at

products
├── id (PK)
├── name
├── description
├── price
├── category
├── image_url
├── available
├── created_at
└── updated_at

inventory
├── id (PK)
├── product_id (FK → products)
└── quantity

orders
├── id (PK)
├── user_id (FK → users, INDEXED)
├── status (PENDING | PREPARING | OUT_FOR_DELIVERY | COMPLETED | CANCELLED, INDEXED)
├── total_amount
├── delivery_address
├── notes
├── created_at (INDEXED)
└── updated_at

order_items
├── id (PK)
├── order_id (FK → orders)
├── product_id (FK → products)
├── quantity
└── unit_price

deliveries
├── id (PK)
├── order_id (FK → orders, UNIQUE)
├── delivery_partner_id (FK → users)
├── status
├── assigned_at
└── delivered_at
```

---

## ⚙️ Configuration

### Backend — `application.properties`

| Property | Default | Description |
|---|---|---|
| `server.port` | `8080` | Server port |
| `spring.datasource.url` | AWS RDS | Database connection URL |
| `spring.jpa.hibernate.ddl-auto` | `update` | Schema generation mode |
| `spring.jpa.show-sql` | `true` | Log SQL queries |
| `logging.level.com.campusfood` | `DEBUG` | App log level |

### Android — `RetrofitInstance.kt`

| Scenario | Base URL |
|---|---|
| Android Emulator + Local Backend | `http://10.0.2.2:8080/api/` |
| Physical Device + Local PC Backend | `http://<YOUR_PC_IP>:8080/api/` |
| Production | `https://your-domain.com/api/` |

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature-name`
3. Commit your changes: `git commit -m 'feat: add some feature'`
4. Push to the branch: `git push origin feature/your-feature-name`
5. Open a Pull Request

---

## 📄 License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.

---

## 👨‍💻 Author

**Kiran Tilekar**  
Senior Java Developer  
📧 Connect via GitHub

---

> ⭐ If you find this project helpful, consider giving it a star!
