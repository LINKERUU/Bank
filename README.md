# 🏦 Binance - Personal Finance Management

[![Java](https://img.shields.io/badge/Java-17%2B-red)](https://www.java.com/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2%2B-brightgreen)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18-blue)](https://react.dev/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5-blue)](https://www.typescriptlang.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15%2B-336791)](https://www.postgresql.org/)

**Binance** is a secure, modern personal banking web application that allows users to manage accounts, cards, transactions, and view detailed financial analytics — all in one beautiful and intuitive interface.

## 🚀 Features

### 🏦 ***Account & Card Management**
- Create and manage multiple bank accounts
- Add credit/debit cards with automatic masking (****-****-****-1234)
- Full ownership control — no one can access your data
- Delete accounts (only when balance is zero)

### 📋 **Smart Transactions**
- Record income and expenses with descriptions
- Automatic balance updates
- Advanced filtering (by date, type, amount)
- Search transactions instantly

### 📊 **Financial Analytics Dashboard**
- Interactive charts (line, pie, bar)
- Income vs Expense breakdown
- Balance trends over time
- Statistics by week, month, year or custom period

### 🔐 **Bank-Grade Security**
- BCrypt password hashing
- Full data isolation between users
- Protected API endpoints
- Sensitive data never exposed

### 🎨 **Modern UI/UX**
- Clean, modern design with React + Tailwind
- Fully responsive (desktop, tablet, mobile)
- Real-time feedback and validation
- Dark mode ready

## 🛠️ Tech Stack

### Backend (Spring Boot - Java)
- **Framework:** Spring Boot 3.2 + Spring Security + Spring Data JPA
- **Database:** PostgreSQL 15+
- **Authentication:** BCrypt
- **Validation:** Hibernate Validator + Custom Constraints
- **Testing:** JUnit 5, Mockito, Testcontainers
- **Build Tool:** Maven

### Frontend (React + TypeScript)
- **Framework:** React 18 + Vite
- **Language:** TypeScript
- **Styling:** Tailwind CSS
- **Charts:** Recharts or Chart.js
- **State Management:** React Context / Zustand
- **Forms:** React Hook Form + Zod validation
- **HTTP Client:** Axios

## 📁 Project Structure

```
Bank/
│    ├── main/                              # Java backend
│    │   ├── java/                          # Application entry point
│    │   │   ├── com.bank/                  # Java package
│    │   │   │   ├── aspect/                # Aspect
│    │   │   │   ├── config/                # Configuration management
│    │   │   │   ├── controller/            # HTTP handlers
│    │   │   │   ├── dto/                   # Data transfer object
│    │   │   │   ├── repository/            # Repository for work with database
│    │   │   │   ├── exception/             # Exceptions for errors
│    │   │   │   ├── model/                 # Data models
│    │   │   │   ├── repository/            # Repository for working with database
│    │   │   │   ├── security/              # Security configuration
│    │   │   │   ├── service/               # Business logic
│    │   │   │   ├── utils/                 # Cache
│    │   │   │   └── BankApplication.java   # main file
├── pom.xml                                 # Configuration
└── docs/                                   # Project documentation
```

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Node.js 18+
- PostgreSQL 15+
- Maven
- Docker (optional, for Testcontainers)

### 1. Clone & Setup
```bash
git clone https://github.com/yourusername/bank-app.git
cd bank-app
```

### 2. Start Backend 

```bash
cd backend
./mvnw spring-boot:run
```

The backend will be available at `http://localhost:8080`

### 3. Start Frontend

```bash
cd frontend
npm install
npm run dev
```

The frontend will be available at `http://localhost:3000`

### 4. Access the Application

1. Open `http://localhost:3000` in your browser
2. Register a new account or login
3. Add a card and start tracking transactions!

## 📚 API Documentation

### Authentication Endpoints

- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - User login

### Accounts Endpoints

- `GET /api/accounts` - Get user accounts
- `POST /api/accounts` - Create account
- `PUT /api/accounts/{id}` - Update account
- `DELETE /api/accounts/{id}` - Delete (if balance = 0)

### Cards Endpoints

- `POST /api/cards` - Add card
- `GET /api/cards` - List masked cards
- `DELETE /api/cards/{id}` - Delete card
- `PUT /api/cards/{id}` - Update card

### Transactions Endpoints

- `POST /api/transactions` - Add transaction
- `GET /api/transactions` - List transactions
- `DELETE /api/transactions/{id}` - Delete transaction
- `PUT /api/transactions/{id}` - Update card

## 🧪 Testing

### Backend Tests

```bash
# Backend tests
cd backend
./mvnw test

# Integration tests with real DB
./mvnw verify -Pintegration
```

## 🔧 Configuration

### Backend Configuration

Edit `server/.env`:

```env
spring.datasource.url=jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

spring.jpa.hibernate.ddl-auto=update

```

### Frontend Configuration

Edit `client/.env.local`:

```env
NEXT_PUBLIC_API_URL=http://localhost:8080/api/v1
```

## 📄 Documentation

- **Software Requirements Specification:** [SRS](./docs/SRS.md)
- **Domain Model:** [DM](./docs/DOMAIN_MODEL.md)
- **Use Cases:** [UC](./docs/USE_CASES.md)
- **Architecture Diagrams:** [Component](./docs/COMPONENT_DIAGRAMS.md), [Deployment](./docs/DEPLOYMENT_DIAGRAMS.md)

## 📝 License

This project is for educational purposes only.

## Future Enhancements

- [ ] **Multi-currency accounts** – Support for USD, EUR, GBP, etc. with real-time exchange rates (ECB/CBR)
- [ ] **Recurring & scheduled transactions** – Automate rent, salary, subscriptions, and bill payments
- [ ] **Budgets & spending limits** – Set monthly caps per category with real-time alerts and progress bars
- [ ] **Cashback & rewards tracker** – Automatically import and calculate cashback from linked bank cards
- [ ] **Split bills with friends** – Easy expense sharing with QR codes or direct in-app requests
- [ ] **Dark/Light mode toggle** – User-controlled theme with system preference detection
- [ ] **Investment portfolio integration** – Sync with brokers (Tinkoff Invest, Interactive Brokers, Binance)
- [ ] **Custom dashboards** – Drag-and-drop widgets for personalized financial overview
- [ ] **2FA & hardware key support** – TOTP (Google Authenticator) + YubiKey/WebAuthn