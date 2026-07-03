<div align="center">

<img src="https://img.shields.io/badge/NeoBank-Digital%20Banking%20Platform-1E3A5F?style=for-the-badge&logoColor=white" />

# 🏦 NeoBank Platform

### *A Modern, Full-Stack Digital Banking Application*

<p align="center">
  <img src="https://img.shields.io/badge/Angular-17-DD0031?style=for-the-badge&logo=angular&logoColor=white" />
  <img src="https://img.shields.io/badge/Spring%20Boot-3.5-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" />
  <img src="https://img.shields.io/badge/MySQL-9.4-4479A1?style=for-the-badge&logo=mysql&logoColor=white" />
  <img src="https://img.shields.io/badge/Railway-Backend-0B0D0E?style=for-the-badge&logo=railway&logoColor=white" />
  <img src="https://img.shields.io/badge/Vercel-Frontend-000000?style=for-the-badge&logo=vercel&logoColor=white" />
  <img src="https://img.shields.io/badge/JWT-Auth-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white" />
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Status-Live%20%26%20Deployed-2ecc71?style=for-the-badge" />
  <img src="https://img.shields.io/badge/Internship-Infosys%20PMIS-0057A8?style=for-the-badge" />
  <img src="https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge" />
</p>

> **NeoBank** is a premium, full-stack digital banking platform with a glassmorphic UI, secure JWT authentication, role-based access control, and a complete banking feature suite — from fund transfers and loan management to budget tracking and bill scheduling.

**🌐 [Live Frontend](https://neobank-platform.vercel.app)** &nbsp;|&nbsp; **⚙️ [Live API](https://neobank-platform-production.up.railway.app)** &nbsp;|&nbsp; **📖 [Swagger Docs](https://neobank-platform-production.up.railway.app/swagger-ui.html)**

</div>

---

## ✨ Features

### 👤 Customer Features
| Feature | Description |
|---|---|
| 🔐 **Multi-Step Registration** | 7-step onboarding capturing personal info, KYC, address, nominee, and account preferences |
| 🛡️ **Secure Login** | Email/password login with OTP-based sign-in via email or SMS |
| 🏦 **Account Management** | View savings/current accounts, balances, and account details |
| 💸 **Fund Transfers** | Interactive bank card UI with live ledger receipt, overdraft warnings, DEBIT/CREDIT toggle |
| 📊 **Transaction History** | Full transaction ledger with summary statistics |
| 💳 **Loan Applications** | Apply for loans with product selection, view EMI schedule and repayment tracking |
| 📅 **Bill Scheduling** | Schedule recurring bills with category chips, countdown timers, and notification toggle |
| 💰 **Budget Tracking** | Set monthly spending limits per category with visual progress meters |
| 🎁 **Rewards** | Points balance tracking for loyalty features |
| 📈 **Insights** | Financial summary and spending insights per user |
| 👤 **Profile Management** | View and update personal profile information |

### 🔧 Admin Features
| Feature | Description |
|---|---|
| 📋 **User Approval Workflow** | Review and activate/deactivate customer accounts |
| 📊 **Dashboard Metrics** | Platform-wide statistics — users, accounts, transactions |
| 💼 **Loan Underwriting** | Review loan applications, approve/reject with remarks |
| 🔍 **Transaction Monitoring** | View all platform transactions across all users |
| ⚙️ **System Health** | Monitor system health indicators |

---

## 🏛️ System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        CLIENT LAYER                             │
│         Angular 17 SPA (Vercel CDN)                             │
│   Standalone Components · RxJS · Signals · Glassmorphic UI      │
└──────────────────────────┬──────────────────────────────────────┘
                           │ HTTPS / REST / JSON
                           │ Authorization: Bearer <JWT>
┌──────────────────────────▼──────────────────────────────────────┐
│                       SECURITY LAYER                            │
│          Spring Security 6 · JWT Auth Filter                    │
│      Route Guards · Role-Based Access (CUSTOMER / ADMIN)        │
└──────────────────────────┬──────────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────────┐
│                      API / CONTROLLER LAYER                     │
│   AuthController · AccountController · TransactionController    │
│   LoanController · BudgetController · BillController · Admin    │
└──────────────────────────┬──────────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────────┐
│                      SERVICE LAYER                              │
│   @Transactional Business Logic · Validation · Amortization     │
│   EmailService · SmsService · JwtUtil                           │
└──────────────────────────┬──────────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────────┐
│                    PERSISTENCE LAYER                            │
│        Spring Data JPA · Hibernate ORM · Flyway Migrations      │
│                   MySQL 9.4 (Railway)                           │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🛠️ Technology Stack

### Frontend
| Technology | Version | Purpose |
|---|---|---|
| **Angular** | 17 (Standalone) | SPA framework — routing, DI, lazy loading |
| **TypeScript** | 5.x | Type-safe JavaScript |
| **RxJS** | 7.x | Async streams, HTTP client, operators |
| **Angular Signals** | Built-in | Fine-grained reactive state management |
| **Vanilla CSS** | CSS3 | Custom design system, glassmorphism, animations |
| **FontAwesome** | 6 | Icon library |

### Backend
| Technology | Version | Purpose |
|---|---|---|
| **Spring Boot** | 3.5.x | REST API framework with auto-configuration |
| **Spring Security** | 6.5.x | JWT authentication, role-based authorization |
| **Spring Data JPA** | 3.5.x | Repository abstraction over Hibernate |
| **Hibernate ORM** | 6.6.x | Entity mapping, HQL queries |
| **JJWT** | 0.12.x | JWT token generation and validation |
| **Flyway** | 11.x | Database migration versioning (14 migrations) |
| **Lombok** | 1.18.x | Boilerplate reduction |
| **SpringDoc OpenAPI** | 2.8.x | Auto-generated Swagger UI |
| **JavaMail** | Spring Starter | Email notifications (OTP, registration) |
| **MySQL Connector** | 9.7.x | JDBC driver for MySQL |

### Infrastructure
| Service | Purpose |
|---|---|
| **Railway** | Spring Boot backend + MySQL database hosting |
| **Vercel** | Angular frontend hosting with global CDN |
| **GitHub** | Source control + CI/CD trigger (auto-deploy on push) |

---

## 🗄️ Database Schema

```
users ──────────────────────────────────────────────────────┐
│ id · email · password_hash · full_name · phone            │
│ dob · gender · nationality · id_type · id_number          │
│ street · city · state · zip_code                          │
│ nominee_name · nominee_relation · nominee_dob             │
│ preferred_account_type · currency · role · is_active      │
└───────────────────────────────────────────────────────────┘
    │ 1:N              │ 1:N           │ 1:N        │ 1:N
    ▼                  ▼               ▼            ▼
accounts          loan_applications  budgets      bills
│ account_number  │ amount           │ category   │ biller_name
│ account_type    │ tenure           │ limit_amt  │ amount
│ balance         │ status           │ month      │ due_date
│ user_id         │ remarks          │ user_id    │ is_paid
    │ 1:N              │ 1:1
    ▼                  ▼
transactions      loan_accounts
│ type (D/C)      │ principal
│ amount          │ interest_rate
│ memo            │ remaining_bal
│ timestamp            │ 1:N
                       ▼
                  loan_repayments
                  │ due_date
                  │ emi_amount
                  │ status (SCHEDULED/PAID/OVERDUE)
```

> All schema changes are managed by **14 Flyway migration scripts** — validated and applied automatically on every startup.

---

## 📖 API Documentation (Swagger UI)

NeoBank uses **SpringDoc OpenAPI** to auto-generate interactive API documentation. Every endpoint is documented with request/response schemas, authentication requirements, and example payloads.

### 🔗 Access Swagger UI

| Environment | URL |
|---|---|
| **Production** | https://neobank-platform-production.up.railway.app/swagger-ui.html |
| **Local** | http://localhost:8081/swagger-ui.html |
| **OpenAPI JSON** | https://neobank-platform-production.up.railway.app/v3/api-docs |

### 📋 How to Use Swagger UI

1. **Open** the Swagger UI URL in your browser
2. **Browse** all available API endpoints grouped by controller tag
3. **Authenticate** — click the 🔒 **Authorize** button → enter your JWT token as:
   ```
   Bearer eyJhbGciOiJIUzI1NiJ9...
   ```
4. **Try it out** — click any endpoint → **Try it out** → fill in parameters → **Execute**
5. **View responses** — see the live response body, status code, and headers

### 🗂️ API Endpoint Groups

#### 🔐 Authentication (`/api/auth`)
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `POST` | `/api/auth/register` | Public | Register new customer (7-step data) |
| `POST` | `/api/auth/login` | Public | Login and receive JWT token |
| `GET` | `/api/auth/verify-document` | Public | Check ID number uniqueness |
| `POST` | `/api/auth/otp/request` | Public | Request OTP via email or SMS |
| `POST` | `/api/auth/otp/verify` | Public | Verify OTP and get JWT token |

#### 👤 Users (`/api/users`)
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `GET` | `/api/users/me` | Customer | Get own profile |
| `PUT` | `/api/users/me` | Customer | Update profile |
| `GET` | `/api/users/admin/users` | Admin | List all users |
| `PUT` | `/api/users/admin/users/{id}/toggle-status` | Admin | Activate/deactivate user |

#### 🏦 Accounts (`/api/accounts`)
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `GET` | `/api/accounts` | Customer | List own accounts |
| `POST` | `/api/accounts` | Customer | Create new account |
| `GET` | `/api/accounts/{id}` | Customer | Get account by ID |
| `GET` | `/api/accounts/{id}/transactions` | Customer | Get account transactions |

#### 💸 Transactions (`/api/transactions`)
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `POST` | `/api/transactions` | Customer | Execute fund transfer |
| `GET` | `/api/transactions/history` | Customer | Full transaction history |
| `GET` | `/api/transactions/summary` | Customer | Transaction summary stats |

#### 💳 Loans (`/api/loans`)
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `GET` | `/api/loans/products` | Public | Available loan products |
| `POST` | `/api/loans/apply` | Customer | Submit loan application |
| `GET` | `/api/loans/my-applications` | Customer | Own loan applications |
| `GET` | `/api/loans/my-accounts` | Customer | Active loan accounts |
| `GET` | `/api/loans/{id}/repayments` | Customer | EMI repayment schedule |
| `GET` | `/api/loans/admin/applications` | Admin | All loan applications |
| `PUT` | `/api/loans/{id}/decision` | Admin | Approve/reject loan |

#### 📅 Bills (`/api/bills`)
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `GET` | `/api/bills` | Customer | List scheduled bills |
| `POST` | `/api/bills` | Customer | Schedule new bill |
| `GET` | `/api/bills/{id}` | Customer | Get bill details |
| `PUT` | `/api/bills/{id}/status` | Customer | Update bill status |

#### 💰 Budgets (`/api/budgets`)
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `GET` | `/api/budgets` | Customer | List budgets |
| `POST` | `/api/budgets` | Customer | Create spending limit |
| `GET` | `/api/budgets/{userId}/{month}` | Customer | Budget summary by month |
| `DELETE` | `/api/budgets/{id}` | Customer | Delete budget |

#### 🔧 Admin (`/api/admin`)
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `GET` | `/api/admin/dashboard` | Admin | Platform dashboard metrics |
| `GET` | `/api/admin/pending-approvals` | Admin | Pending account activations |
| `GET` | `/api/admin/system-health` | Admin | System health status |
| `GET` | `/api/admin/transactions` | Admin | All platform transactions |

### 📦 Standard Error Response Format
```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid email or password",
  "timestamp": "2026-06-28T09:27:02.180"
}
```

---

## 🔐 Security Model

```
                    ┌─────────────────────┐
                    │   JWT Token         │
                    │  { userId, email,   │
                    │    role, expiry }   │
                    └──────────┬──────────┘
                               │ signed with JWT_SECRET
          ┌────────────────────▼─────────────────────┐
          │           JwtAuthFilter                   │
          │  Intercepts every request                 │
          │  Validates token signature & expiry       │
          │  Populates SecurityContext                │
          └────────────────────┬─────────────────────┘
                               │
          ┌────────────────────▼─────────────────────┐
          │        Route Authorization               │
          │  /api/auth/**      → Public              │
          │  /api/admin/**     → ROLE_ADMIN only     │
          │  everything else   → authenticated       │
          └──────────────────────────────────────────┘
```

**Security Features:**
- 🔒 BCrypt password hashing (strength factor 10)
- 🎟️ Stateless JWT tokens — no server-side sessions
- 🛑 Account activation gate — new accounts require admin approval
- 🌐 CORS whitelisted origins only
- 🔑 Role-based method security via `@EnableMethodSecurity`

---

## 🚀 Deployment

### Architecture
```
GitHub Repository
       │
       ├── push to main ──► Railway (auto-deploy)
       │                        ├── Spring Boot Backend
       │                        └── MySQL Database
       │
       └── push to main ──► Vercel (auto-deploy)
                                └── Angular Frontend
```

### Live URLs
| Service | URL |
|---|---|
| 🌐 Frontend | https://neobank-platform.vercel.app |
| ⚙️ Backend API | https://neobank-platform-production.up.railway.app |
| 📖 Swagger UI | https://neobank-platform-production.up.railway.app/swagger-ui.html |
| ❤️ Health Check | https://neobank-platform-production.up.railway.app/actuator/health |

### Railway Environment Variables
```env
DB_URL=jdbc:mysql://${{MySQL.MYSQLHOST}}:${{MySQL.MYSQLPORT}}/${{MySQL.MYSQLDATABASE}}?useSSL=false&serverTimezone=UTC
DB_USERNAME=${{MySQL.MYSQLUSER}}
DB_PASSWORD=${{MySQL.MYSQLPASSWORD}}
JWT_SECRET=your-minimum-32-character-secret-key
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=your-gmail@gmail.com
SPRING_MAIL_PASSWORD=your-gmail-app-password
PORT=8080
```

---

## 💻 Local Development Setup

### Prerequisites
| Tool | Version |
|---|---|
| Java JDK | 21+ |
| Maven | 3.9+ |
| Node.js | 18+ |
| MySQL | 8.0+ |
| Git | Latest |

### 1️⃣ Clone the Repository
```bash
git clone https://github.com/Sumankanta/neobank-platform.git
cd neobank-platform
```

### 2️⃣ Backend Setup
```bash
cd NeoBank-Backend

# Create local database
mysql -u root -p -e "CREATE DATABASE neobank_db;"

# Set environment variables (or create .env file)
export DB_URL=jdbc:mysql://localhost:3306/neobank_db
export DB_USERNAME=root
export DB_PASSWORD=yourpassword
export JWT_SECRET=neobank-local-dev-secret-key-32chars

# Run the application
./mvnw spring-boot:run
```
Backend runs at: `http://localhost:8081`

### 3️⃣ Frontend Setup
```bash
cd neobank-frontend

# Install dependencies
npm install

# Update environment file
# src/environments/environment.ts
# apiUrl: 'http://localhost:8081/api'

# Start development server
npx ng serve
```
Frontend runs at: `http://localhost:4200`

### 4️⃣ Create Admin Account
```sql
-- Run in MySQL after backend starts once (tables created by Flyway)
INSERT INTO users (email, password_hash, full_name, phone, role, is_active, currency, created_at, updated_at)
VALUES ('admin@neobank.com', '<bcrypt-hash-of-your-password>', 'NeoBank Admin', '9999999999', 'ADMIN', 1, 'INR', NOW(), NOW());
```

> Generate a BCrypt hash by temporarily calling: `GET http://localhost:8081/api/auth/generate-hash?password=YourPassword`

---

## 📁 Project Structure

```
neobank-platform/
│
├── NeoBank-Backend/                    # Spring Boot Application
│   ├── src/main/java/com/infy/NeoBank/
│   │   ├── config/                     # SecurityConfig, CorsConfig
│   │   ├── controller/                 # REST Controllers (Auth, Account, Loan...)
│   │   ├── dto/                        # Request & Response DTOs
│   │   │   ├── request/                # LoginRequest, RegisterRequest...
│   │   │   └── response/              # AuthResponse, UserResponse...
│   │   ├── entity/                     # JPA Entities (User, Account, Transaction...)
│   │   ├── enums/                      # Role, AccountType, LoanStatus...
│   │   ├── exception/                  # GlobalExceptionHandler, custom exceptions
│   │   ├── repository/                 # Spring Data JPA interfaces
│   │   ├── security/                   # JwtAuthFilter, JwtUtil, UserDetailsServiceImpl
│   │   └── service/                    # Business logic (AuthService, LoanService...)
│   ├── src/main/resources/
│   │   ├── application.properties      # App configuration
│   │   └── db/migration/              # Flyway SQL migration scripts (V1-V14)
│   └── pom.xml
│
└── neobank-frontend/                   # Angular Application
    ├── src/app/
    │   ├── core/
    │   │   ├── guards/                 # authGuard, adminGuard
    │   │   ├── models/                 # TypeScript interfaces
    │   │   ├── services/               # AuthService, HTTP services
    │   │   └── utils/                  # ApiEndpoints, StorageUtil
    │   ├── features/
    │   │   ├── auth/                   # Login, Register components
    │   │   ├── dashboard/              # Home dashboard
    │   │   ├── accounts/               # Account management
    │   │   ├── transactions/           # Fund transfers, history
    │   │   ├── loans/                  # Loan application & tracking
    │   │   ├── budget/                 # Budget management
    │   │   ├── bills/                  # Bill scheduling
    │   │   ├── rewards/                # Rewards tracking
    │   │   ├── insights/               # Financial insights
    │   │   ├── admin/                  # Admin portal
    │   │   ├── landing/                # Public landing page
    │   │   └── user/                   # Profile management
    │   └── shared/
    │       └── components/             # Layout, Header, Sidebar, Toast
    ├── src/environments/
    │   ├── environment.ts              # Local dev config
    │   └── environment.prod.ts        # Production config
    └── vercel.json                     # Vercel SPA routing config
```

---

## 🎨 UI Design System

NeoBank uses a custom **glassmorphic design system** built entirely with Vanilla CSS:

```css
/* Glass Card Effect */
background: rgba(255, 255, 255, 0.05);
backdrop-filter: blur(20px) saturate(160%);
border: 1px solid rgba(255, 255, 255, 0.09);
box-shadow: 0 8px 40px rgba(0, 0, 0, 0.2);
```

**Color Tokens:**
| Token | Color | Usage |
|---|---|---|
| `--color-primary` | `#1E3A5F` | Deep navy — backgrounds, headers |
| `--color-accent` | `#2563EB` | Vivid blue — CTAs, links |
| `--color-success` | `#059669` | Emerald — credits, approvals |
| `--color-danger` | `#DC2626` | Ruby red — debits, errors |
| `--color-warning` | `#D97706` | Amber — pending, warnings |

---

## 👤 User Journey

```
1. Visit Landing Page
        │
        ▼
2. Register (7-step form)
        │
        ▼
3. Wait for Admin Approval
        │ (Admin activates account)
        ▼
4. Login → Receive JWT Token
        │
        ├──► Dashboard (overview)
        ├──► Accounts (balances)
        ├──► Transfer Funds
        ├──► Apply for Loan
        ├──► Schedule Bills
        ├──► Set Budgets
        └──► View Insights
```

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature`
3. Commit your changes: `git commit -m "feat: add your feature"`
4. Push to branch: `git push origin feature/your-feature`
5. Open a Pull Request

---

## 📄 License

This project is licensed under the MIT License.

---

<div align="center">

**Built with ❤️ by [Sumankanta Padhan](https://github.com/Sumankanta)**

*Infosys PMIS Internship Program — 2026*

<img src="https://img.shields.io/badge/Angular-17-DD0031?style=flat-square&logo=angular" />
<img src="https://img.shields.io/badge/Spring%20Boot-3-6DB33F?style=flat-square&logo=springboot" />
<img src="https://img.shields.io/badge/MySQL-4479A1?style=flat-square&logo=mysql&logoColor=white" />
<img src="https://img.shields.io/badge/Deployed%20on-Railway%20%26%20Vercel-black?style=flat-square" />

⭐ **Star this repo if you found it helpful!**

</div>
