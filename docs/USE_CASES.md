# Binance – Use Case Analysis

## Actors
### **Guest (Unauthenticated User)**
A user who is not logged into the system. Can view the registration and login pages only.

### **Registered User (Client)**
An authenticated user. Can manage personal accounts, cards, transactions, and view analytics.

### **Admin** *(future extension)*
System administrator. Will be able to view global statistics and audit logs.

### **External System (Bank / Payment Gateway)** *(mocked)*
External services simulated via stubs for card validation and transaction processing.

## Use Case Scenarios

### **UC1: Register User**
**Actor:** Guest  
**Precondition:** User is not authenticated and is on the registration page.

**Flow of Events:**
1. User opens the registration page
2. Registration form contains fields: First Name, Last Name, Login, Email, Password, Confirm Password
3. User fills in the form and clicks **Register**
4. System validates:
   - All required fields are filled
   - Email has correct format
   - Password is at least 8 characters, contains letters and digits
   - Password and confirmation match
   - Login and Email are unique
5. On success: new user is created, password is hashed with BCrypt, stored in DB
6. Success message displayed, user redirected to login page

**Alternative Flows:**
- Login/Email already exists → "This login/email is already taken"
- Weak password → "Password must be at least 8 characters and contain letters and numbers"
- Other validation errors → inline red errors under fields

**Postcondition:** New user account created and ready for login.

---

### **UC2: Login**
**Actor:** Guest → Registered User  
**Precondition:** User is on the login page.

**Flow of Events:**
1. User enters login and password
2. Clicks **Login**
3. System verifies user existence and password correctness (BCrypt)
4. On success: JWT token is generated and returned, user redirected to Dashboard
5. Token stored in HttpOnly cookie / localStorage

**Alternative Flows:**
- Invalid credentials → "Invalid login or password"
- Empty fields → client-side validation blocks submission

**Postcondition:** User is authenticated and has access to protected features.

---

### **UC3: View and Manage Accounts**
**Actor:** Registered User  
**Precondition:** User is authenticated.

**Flow of Events:**
1. User navigates to **Accounts** section
2. System displays list of accounts belonging to the current user (account number, balance, creation date)
3. User can:
   - Create a new account (unique account number generated)
   - View account details
   - Delete an account (only if balance = 0 and no linked cards)

**Alternative Flows:**
- Attempt to delete account with non-zero balance → "Cannot delete account with non-zero balance"
- Access to foreign account → 403 Forbidden

**Postcondition:** Account list is up-to-date, operations successfully performed.

---

### **UC4: View and Manage Cards**
**Actor:** Registered User  
**Precondition:** User is authenticated.

**Flow of Events:**
1. User navigates to **Cards** section
2. System displays user’s cards with masked numbers (****-****-****-1234)
3. User can:
   - Add a new card (card number, CVV, expiration date)
   - Delete a card
4. Sensitive data (CVV) is encrypted before storage

**Alternative Flows:**
- Invalid card number → Luhn algorithm validation fails
- Expired card → "Card has expired"

**Postcondition:** Card list is current, confidential data protected.

---

### **UC5: Create and View Transactions**
**Actor:** Registered User  
**Precondition:** User is authenticated and has at least one account.

**Flow of Events:**
1. User navigates to **Transactions** section
2. System displays transaction history with filters (date range, type, account)
3. User creates a new transaction:
   - Selects account
   - Chooses type (Income / Expense)
   - Enters amount and description
   - Clicks **Save**
4. Account balance is automatically updated
5. Transaction is persisted in the database

**Alternative Flows:**
- Insufficient funds for expense → "Insufficient funds on the account"
- Invalid input → fields highlighted with errors

**Postcondition:** Transaction recorded, account balance updated.

---

### **UC6: View Analytics and Statistics**
**Actor:** Registered User (Admin in future)  
**Precondition:** User is authenticated, transaction history exists.

**Flow of Events:**
1. User navigates to **Statistics** section
2. Selects time period (week, month, year, custom range)
3. System calculates and displays:
   - Total income / expenses
   - Balance dynamics chart
   - Pie/bar charts by category
   - Top transactions
4. Charts update instantly when period is changed

**Postcondition:** User sees accurate financial analytics for the selected period.