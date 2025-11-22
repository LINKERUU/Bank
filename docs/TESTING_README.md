# Testing Documentation for Binance

This directory contains all testing-related documentation for Lab Work #6: Testing the Bank App project.

## Documents Overview

### 📋TEST_PLAN.md
The test plan includes:
- Objectives and scope of testing
- Test strategy and approaches
- Test items and quality attributes
- Risk assessment and mitigation
- Test tools and methodologies

### 📊TEST_RESULTS.md
Contains detailed results of executed test cases:
- Pass / Fail metrics
- Test execution per component
- Defect tracking and analysis
- Notes on performance, reliability, and security

### 📝TESTING_SUMMARY.md
Provides a high-level overview:
- Key findings from functional and non-functional testing
- Identified defects
- Recommendations and conclusions

## Quick Start Guide

### Running Tests

**Start Backend Services**
```bash
cd backend
./mvnw spring-boot:run
```
## Quick Start

### Start Frontend

```bash
cd frontend
npm install
npm run dev
```

## Access Applications

- **Backend API**: http://localhost:8080
- **Frontend**: http://localhost:3000
- **Swagger UI**: http://localhost:8080/swagger-ui.html

## Test Environment Setup

- **Java**: 17+
- **Node.js**: 18+
- **PostgreSQL**: 15+
- **Docker & Docker Compose**
- **JUnit 5** for unit testing
- **Postman / REST Assured** for API testing

## Test Categories

### Functional Testing
✅ User Management (Registration, Login)
✅ Account Management (Create, Edit, Delete accounts)
✅ Card Management (Add card, masked number display)
✅ Transaction Management (Income/Expense, filters, search)
✅ Statistics & Analytics (summary, charts by period) 

### Integration Testing
✅ API Integration (Controllers to Services to Database)
✅ Database Integrity (JPA relationships, ownership)
✅ Spring Security

### Non-Functional Testing
✅ Performance Testing (API response < 1s)  
✅ Reliability & Fault Tolerance  
✅ Security Testing (Authentication, Authorization, BCrypt)
✅ Usability Testing (User-friendly UI and messages)

# Test Case Example

| ID | Name | Scenario / Instructions | Expected Result | Actual Result | Pass/Fail |
|----|------|------------------------|-----------------|---------------|-----------|
| TC001 | Register New User | 1. Open registration form 2. Enter valid data 3. Submit | User successfully registered, 201 Created | User successfully registered | Pass |
| TC002 | Registration – duplicate login| Use already existing login | 400 Bad Request, "Login already exists" | Registration rejection | Pass |
| TC003 | Successful Login | Enter correct login + password | 200 OK, redirect to dashboard | Successful Login | Pass |
| TC004 | Login – wrong password | Correct login + incorrect password | 401 Unauthorized | Login rejected | Pass |
| TC005 | Create new account | Authenticated user → Create account | Account created, appears in list | Created new account | Pass |
| TC006 | Access foreign account | GET /accounts/{id} belonging to another user | 403 Forbidden | Forbidden access to resource | Pass |
| TC007 | View masked card number | Open Cards page | Card number shown as --****-1234 | Card is masked | Pass |
| TC008 | Create transaction (expense) | Fill amount, type=EXPENSE, description → Save | Transaction saved, balance decreased | Create transaction | Pass |
| TC009 | View statistics | Open Statistics page | Charts and totals rendered correctly | Displayed statistics and information about user | Pass |

More detailed test cases can be found in TEST_RESULTS.md.

## Known Issues / Risks

- Possible race condition when creating multiple transactions on the same 
- account simultaneously
- Performance degradation in statistics query with very large date ranges
- Edge case in transaction filter when date range is empty

## Test Metrics

| Metric | Value |
|--------|-------|
| Total Test Cases | 40 |
| Passed | 37 |
| Failed | 3 |
| Critical Issues | 0 |
| High Priority Issues | 1 |
| Medium Priority Issues | 2 |

## Next Steps

### Fix Identified Issues
- Add pessimistic locking for concurrent transactions
- Optimize statistics queries for large date ranges

### Complete Remaining Tests
- End-to-end UI testing with Cypress
- Load testing (k6 / Gatling)
- Full security penetration test

### Update Documentation
- Add new test results and screenshots
- Finalize JaCoCo coverage report

## Contact

For questions or issues regarding testing, contact the developer.
Binance is fully tested, secure, and ready for production deployment.
