# Test Plan for Binance

## Document Information
- **Project:** Bank App – Personal Finance Management System
- **Version:** 1.0
- **Status:** Approved
- **Date:** 22.11.2025

## Table of Contents
1. [Introduction](#introduction)
2. [Test Objectives](#test-objectives)
3. [Test Scope](#test-scope)
4. [Test Strategy](#test-strategy)
5. [Test Types](#test-types)
6. [Test Environment](#test-environment)
7. [Test Data](#test-data)
8. [Test Schedule](#test-schedule)
9. [Test Deliverables](#test-deliverables)
10. [Risk Assessment](#risk-assessment)
11. [Test Tools](#test-tools)
12. [Test Execution Guidelines](#test-execution-guidelines)

## 1. Introduction
### 1.1 Purpose
This Test Plan defines the testing approach for the **Bank App** — a secure personal banking web application that allows users to manage accounts, cards, transactions, and view financial analytics. The plan ensures all functional, security, and non-functional requirements are thoroughly validated before release.

### 1.2 Scope
This test plan covers the entire application including:
- Backend services (Spring Boot + Spring Security + JPA)
- Frontend React interface
- PostgreSQL database with proper relationships and constraints
- based authorization
- Financial transaction processing and balance management
- Sensitive data protection (card masking, password hashing)

### 1.3 References
- Software Requirements Specification (SRS.md)
- Domain Model & Database Schema
- Use Case Analysis
- Architecture & Sequence Diagrams

## 2. Test Objectives
### 2.1 Primary Objectives
- Verify full implementation of all functional requirements
- Ensure banking-grade security (authentication, authorization, data protection)
- Validate correctness of financial operations and balance calculations
- Confirm data ownership and isolation between users
- Test integration between frontend and backend
- Validate usability and responsive design

### 2.2 Success Criteria
- 100% of critical test cases pass
- No unauthorized access to user data
- All financial transactions are atomic and consistent
- API response time < 800ms under normal load
- Code coverage ≥ 90%
- Zero critical/high-severity defects at release

## 3. Test Scope
### 3.1 In Scope
- User registration, login
- Account CRUD operations with ownership control
- Card management with number masking (****-****-****-1234)
- Transaction creation (Income/Expense) with balance updates
- Transaction filtering and search
- Financial statistics and analytics dashboard
- Security mechanisms (BCrypt, HttpOnly cookies)
- Database integrity and transactional behavior

### 3.2 Out of Scope
- Real payment gateway integration
- Multi-factor authentication (MFA)
- Mobile application
- Export to PDF/Excel
- Admin panel (planned for future phases)

## 4. Test Strategy
### 4.1 Testing Approach
- **Risk-Based Testing:** Highest priority on security and financial accuracy
- **Layered Testing:** Unit → Integration → System → Acceptance
- **Security-First:** All endpoints tested with 
- **Data Ownership:** Every scenario tested with correct and foreign user IDs

### 4.2 Test Levels
1. **Unit Testing** – Service and repository methods
2. **Integration Testing** – Controllers + real database via Testcontainers
3. **System Testing** – Full user workflows
4. **Acceptance Testing** – Business scenario validation

## 5. Test Types
### 5.1 Functional Testing
#### User Management
- Valid/invalid registration scenarios
- Login with correct/incorrect credentials
- Password strength and BCrypt verification

#### Account Management
- Create, list, delete accounts
- Ownership validation (cannot access foreign accounts)
- Balance constraints on deletion

#### Card Management
- Add card with Luhn validation
- Automatic number masking in responses
- CVV encryption/storage
- Card expiration checks

#### Transaction Management
- Income and expense transactions
- Automatic balance recalculation
- Transaction filtering by date, type, account
- Prevention of overdraft on expenses

#### Statistics & Analytics
- Income/expense totals
- Balance trend charts
- Category breakdown
- Large date range performance

### 5.2 Non-Functional Testing
#### Performance
- API response time under normal and peak load
- Statistics queries with large datasets
- Concurrent transaction creation

#### Security
- Prevention of SQL injection, XSS, CSRF
- Secure password storage (BCrypt)
- Data isolation between users
- Protection against brute force

#### Usability
- Clear error messages
- Responsive design (desktop + mobile)
- Intuitive navigation and workflow

#### Compatibility
- Chrome, Firefox, Edge, Safari (latest versions)
- Various screen resolutions

### 5.3 Integration & Data Integrity Testing
- JPA relationships and cascade operations
- @Transactional behavior and rollback
- Concurrent transaction safety (future pessimistic locking)
- Foreign key constraints enforcement

## 6. Test Environment
### 6.1 Hardware
- Development machine: 8+ GB RAM, modern CPU
- Test runner: Local or CI environment

### 6.2 Software
- Java 17+, Spring Boot 3.x
- PostgreSQL 15+ (Testcontainers)
- Node.js 18+ (frontend)
- Maven 3.9+

### 6.3 Environment Setup
- Clean test database initialized via Flyway/Liquibase
- Application runs with `test` Spring profile
- Testcontainers spin up real PostgreSQL instance

## 7. Test Data
- 5+ test users with different roles and data volumes
- Accounts with various balances and transaction histories
- Cards (valid, expired, invalid numbers)
- Transactions covering 2+ years (for performance testing)
- Edge cases: zero balance, negative attempts, special characters

## 8. Test Schedule
| Phase                  | Duration     | Dates            |
|------------------------|--------------|------------------|
| Unit Testing           | Ongoing      | Throughout dev   |
| Integration Testing    | Week 4–5     | 10.11 – 20.11    |
| System Testing         | Week 6       | 21.11 – 27.11    |
| Performance & Security | Week 7       | 28.11 – 04.12    |
| Final Regression       | Final week   | Before release   |

## 9. Test Deliverables
- Test Plan (this document)
- Detailed test cases and results
- Defect reports with screenshots
- JaCoCo code coverage report
- Testing Summary Report
- Release recommendation

## 10. Risk Assessment
### High Risk
- Unauthorized access to another user’s financial data
- Incorrect balance calculation
- Transaction loss during concurrent operations

### Medium Risk
- Slow statistics with large datasets

### Low Risk
- Minor UI inconsistencies
- Non-critical validation messages

### Mitigation
- Strict ownership checks in all services
- @Transactional with proper isolation
- Pessimistic locking (planned)
- Regular security reviews

## 11. Test Tools
### Backend
- JUnit 5 + Spring Boot Test
- Mockito
- Testcontainers (PostgreSQL)
- REST Assured
- JaCoCo (coverage)

### API & Manual
- Postman / Newman
- Swagger UI

### Frontend (when needed)
- React Testing Library
- Cypress (future)

### Performance & Security
- k6 or Gatling (load)
- OWASP ZAP (security scanning)

## 12. Test Execution Guidelines
- All protected endpoints tested
- Every financial operation verified against database state
- Defects classified by severity and tracked
- Regression suite run after each fix
- Final sign-off only after 100% critical tests pass

### Exit Criteria
- All critical and high-priority tests: **Passed**
- No open critical/high-severity defects
- Code coverage ≥ 90%
- Security review completed
- Performance within limits
- Documentation updated

---
**Document Control**  
**Author:** Development Team  
**Version:** 1.0  
**Next Review:** After major architectural changes or new features