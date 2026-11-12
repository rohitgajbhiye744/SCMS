# Smart Complaint Management System (SCMS) 🚀

![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.5-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![React](https://img.shields.io/badge/React-19-20232A?style=for-the-badge&logo=react&logoColor=61DAFB)
![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS-v4-38B2AC?style=for-the-badge&logo=tailwind-css&logoColor=white)
![TanStack Query](https://img.shields.io/badge/TanStack_Query-v5-FF4154?style=for-the-badge&logo=react-query&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-Auth-000000?style=for-the-badge&logo=json-web-tokens&logoColor=white)
![Cloudinary](https://img.shields.io/badge/Cloudinary-Media-3448C5?style=for-the-badge&logo=cloudinary&logoColor=white)
![JUnit 5](https://img.shields.io/badge/JUnit-5-25A162?style=for-the-badge&logo=junit5&logoColor=white)
![CI/CD](https://img.shields.io/badge/GitHub_Actions-CI%2FCD-2088FF?style=for-the-badge&logo=github-actions&logoColor=white)

An enterprise-grade, full-stack **Smart Complaint & Issue Management Platform** designed for organizations, educational institutions, and municipalities. It streamlines issue reporting, department routing, staff assignment, and resolution tracking using **Role-Based Access Control (RBAC)**, **stateless JWT authentication**, and **innovative QR-code location tagging**.

---

## ✨ Key Features

- **🛡️ Role-Based Access Control (RBAC):** Multi-tier organizational workflows enforced at both controller and service layers:
  - **Reporter:** Submit complaints with descriptions, category, priority, room/zone tagging, and image proof. View live status updates and audit trails for their own tickets.
  - **Department Coordinator:** Triage department-specific tickets, assign tickets strictly to staff belonging to the responsible department, and update resolution states within their department.
  - **Staff Member:** View assigned tickets, transition workflow state (*In Progress*, *Resolved*, *Closed*) strictly for tickets assigned to them, and log maintenance remarks.
  - **Admin:** Enterprise-wide visibility across all departments, cross-department analytics dashboards, and privileged user role assignment.
- **📍 QR Code Location Tagging:** Report issues instantly by linking complaints to physical room/zone QR UUIDs (`LocationQR`).
- **📸 Cloudinary Media Integration:** Seamless multipart file uploads for storing high-resolution image evidence.
- **📜 Complete Audit Trail:** Automated history tracking (`ComplaintHistory`) capturing timestamped state transitions and user remarks.
- **📊 Interactive Analytics:** Visual reports and charts built with Recharts to monitor department efficiency and resolution timelines.
- **🧪 Automated Backend Test Suite:** Professional automated test harness using JUnit 5, Mockito, Spring Boot Test, MockMvc, and Spring Security Test.
- **🔄 CI/CD Pipeline:** Fully automated GitHub Actions workflow verifying builds and test suites on pushes and pull requests.

---

## 🛠️ Technology Stack

### Backend
- **Framework:** Spring Boot 3.2.5 (Java 17)
- **Security:** Spring Security 6 + Stateless JWT (jjwt 0.12.5) + BCrypt Password Encoding
- **Database & ORM:** PostgreSQL + Spring Data JPA / Hibernate
- **Integrations:** Cloudinary API (Image Storage), JavaMail Sender

### Frontend
- **Framework & Build Tool:** React 19 + Vite
- **Styling:** Tailwind CSS v4 + Lucide React Icons
- **State Management & Caching:** TanStack React Query v5
- **Routing & Forms:** React Router DOM v7 + React Hook Form
- **Data Visualization:** Recharts

### Testing & Quality Assurance
- **Unit Testing:** JUnit 5 (Jupiter)
- **Mocking Framework:** Mockito
- **API & Integration Testing:** Spring Boot Test + MockMvc
- **Security Testing:** Spring Security Test (`@WithMockUser`, method security checks)
- **Continuous Integration:** GitHub Actions (`.github/workflows/backend-tests.yml`)

---

## 🏛️ System Architecture & Roles

| Role | Responsibility | Scope & Security Boundaries |
| :--- | :--- | :--- |
| **`REPORTER`** | General Students / Employees | Publicly self-registered safe default tier. Can submit complaints, attach image proof, tag room QR codes, and view their own tickets. **Cannot** assign tickets, access other reporters' tickets, or update status. |
| **`DEPARTMENT_COORDINATOR`** | Department Heads | Provisioned administrative tier. Triages department tickets, assigns tasks strictly to staff belonging to the responsible department, and updates department ticket status. **Cannot** view, modify, or assign complaints outside their department. |
| **`STAFF`** | Maintenance & IT Staff | Provisioned operational tier. Views tickets assigned to them, updates workflow status (`In Progress`, `Resolved`, `Closed`), and logs repair remarks. **Cannot** update complaints not assigned to them or assign tickets. |
| **`ADMIN`** | System Administrator | System monitoring, enterprise analytics across all departments, system-wide complaint oversight, and user role management (`PUT /api/users/{id}/role`). |

---

## 🔒 Security Architecture

The application implements a defense-in-depth security model:

```
[ HTTP Request ]
       │
       ▼
[ AuthTokenFilter ] ── Validates JWT Bearer Token & Populates SecurityContext
       │
       ▼
[ WebSecurityConfig ] ── URL Path Access Rules (Public vs Authenticated)
       │
       ▼
[ Controller Tier ] ── Role-Based Authorization via @PreAuthorize
       │
       ▼
[ Service Tier ] ── Resource-Level Access & Departmental Ownership Verification
       │
       ▼
[ JPA Repositories / DB ]
```

### 1. Stateless Authentication (JWT)
- Authenticated requests pass a signed HMAC-SHA token in the `Authorization: Bearer <token>` header.
- `JwtUtils` generates and verifies tokens with configurable expiration windows.
- Unauthenticated requests to protected endpoints return `HTTP 401 Unauthorized`.

### 2. Controller-Level Authorization
- Declarative method security enabled via `@EnableMethodSecurity`.
- Endpoints enforce role constraints using `@PreAuthorize`:
  - `POST /api/complaints`: `REPORTER`, `STAFF`, `DEPARTMENT_COORDINATOR`, `ADMIN`
  - `GET /api/complaints`: Filtered per authenticated user's role
  - `PATCH /api/complaints/{id}/status`: `STAFF`, `DEPARTMENT_COORDINATOR`, `ADMIN`
  - `PATCH /api/complaints/{id}/assign`: `DEPARTMENT_COORDINATOR`, `ADMIN`
  - `GET /api/users/staff`: `DEPARTMENT_COORDINATOR`, `ADMIN`
  - `PUT /api/users/{id}/role`: `ADMIN` only

### 3. Service-Layer Resource Authorization & Invariants
- **Public Registration Safe Default:** Public self-registration (`POST /api/auth/register`) strictly assigns role `REPORTER`. Any attempted privilege escalation (e.g. requesting `STAFF` or `DEPARTMENT_COORDINATOR`) is safely overridden to `REPORTER`. Privileged role provisioning is restricted to the administrator endpoint `PUT /api/users/{id}/role`.
- **Staff Ticket Ownership:** Staff members can only transition the status of complaints assigned to their specific user ID. Attempting to update another ticket throws `AccessDeniedException` (`HTTP 403 Forbidden`).
- **Coordinator Department Boundary:** Coordinators can only view, update, and assign complaints belonging to their assigned department. Attempting to manage an out-of-department ticket throws `AccessDeniedException` (`HTTP 403 Forbidden`).
- **Staff Department Validation:** When assigning a complaint, the assigned staff member must belong to the department responsible for the complaint. Assigning cross-department staff throws `IllegalArgumentException` (`HTTP 400 Bad Request`).
- **Reporter Isolation:** Reporters can only retrieve complaints they submitted. Attempting to inspect another reporter's ticket returns `HTTP 403 Forbidden`.

---

## 🧪 Testing & Quality Assurance

SCMS includes a comprehensive automated test suite in `backend/src/test/java`:

### A. Service-Layer Unit Tests (`ComplaintServiceTest`)
- **Frameworks:** JUnit 5, Mockito.
- **Coverage:**
  - Complaint creation with valid input and default "Submitted" status.
  - Validation failures for missing/blank required fields (`title`, `description`, `category`, `priority`, `departmentId`).
  - Missing user and department lookup exceptions (`ResourceNotFoundException`).
  - Scoped complaint retrievals: Reporter (own tickets), Staff (assigned tickets), Coordinator (department tickets), Admin (all tickets).
  - Resource access authorization: Reporter accessing own vs foreign tickets, Staff accessing assigned vs unassigned tickets, Coordinator accessing in-department vs out-of-department tickets, Admin accessing any ticket.
  - Complaint assignment: Successful assignment within department, rejection when assigning tickets outside department, rejection when assigning to non-staff user, and rejection when assigning to staff belonging to a different department (Department Boundary Check).
  - Status transitions: Valid transitions (`Submitted` → `Assigned` → `In Progress` → `Resolved` → `Closed`), audit history creation, rejection of unauthorized transitions, rejection of unrecognized status strings.

### B. Controller & API Integration Tests (`AuthControllerTest`, `ComplaintControllerTest`, `DepartmentControllerTest`, `UserControllerTest`)
- **Frameworks:** Spring Boot Test, MockMvc, Spring Security Test.
- **Coverage:**
  - User registration with safe `REPORTER` role assignment.
  - Privilege escalation prevention during registration.
  - Duplicate username and email conflict rejection (`HTTP 400`).
  - Bean validation on registration payloads (`HTTP 400`).
  - Successful user login with JWT issuance (`HTTP 200`).
  - Bad credentials handling (`HTTP 401`).
  - Multipart complaint submission and error handling.
  - Complaint retrieval, assignment, and status update endpoints.
  - Public department listing endpoint.
  - Administrator user management and role assignment endpoints.

### C. Security & RBAC Matrix Tests (`SecurityRBACTest`)
- **Frameworks:** Spring Boot Test, MockMvc, `@WithMockUser`.
- **Explicit Scenarios Tested:**
  1. Unauthenticated users cannot access protected endpoints (`HTTP 401 Unauthorized`).
  2. Reporter cannot assign complaints (`HTTP 403 Forbidden`).
  3. Reporter cannot perform Coordinator-only actions (`HTTP 403 Forbidden`).
  4. Staff cannot perform Coordinator-only actions (`HTTP 403 Forbidden`).
  5. Coordinator can assign complaints within their department (`HTTP 200 OK`).
  6. Staff can update status only when assigned to them (`HTTP 200 OK` vs `HTTP 403 Forbidden`).
  7. Coordinator cannot modify complaints outside their department (`HTTP 403 Forbidden`).
  8. Reporter cannot access another Reporter's complaints (`HTTP 403 Forbidden`).
  9. Public registration endpoint is accessible anonymously (`HTTP 200 OK`).
  10. Department listing is accessible anonymously (`HTTP 200 OK`).

---

## 🏃 Running Backend Tests

Navigate to the `backend` directory and execute tests using Maven:

```bash
cd backend
```

### Run the entire test suite:
```bash
mvn test
```

### Run a specific test class:
```bash
# Service unit tests
mvn test -Dtest=ComplaintServiceTest

# Controller integration tests
mvn test -Dtest=AuthControllerTest
mvn test -Dtest=ComplaintControllerTest
mvn test -Dtest=DepartmentControllerTest
mvn test -Dtest=UserControllerTest

# Security & RBAC tests
mvn test -Dtest=SecurityRBACTest
```

### Run a specific test method:
```bash
mvn test -Dtest=SecurityRBACTest#reporterCannotAssignComplaint
```

---

## 🔄 CI/CD Pipeline

Continuous Integration is automated via GitHub Actions in [`.github/workflows/backend-tests.yml`](.github/workflows/backend-tests.yml):

- **Triggers:** Automatically runs on every `push` and `pull_request` targeting `main` or `master`.
- **Isolated Database:** Boots an isolated PostgreSQL service container (`postgres:16-alpine`) with health checks, avoiding any exposure of production credentials.
- **Build & Verification:**
  - Checks out repository.
  - Configures Java 17 (Temurin) with automated Maven dependency caching.
  - Compiles the backend (`mvn -B compile`).
  - Executes the automated test suite (`mvn -B test`).
  - Uploads Surefire test reports as workflow artifacts.
  - **Fails the pipeline** immediately if any test fails.

---

## 🚀 Getting Started

### Prerequisites
- **Java 17+** and **Maven**
- **Node.js (v18+)** and **npm**
- **PostgreSQL** (Local instance or Cloud DB)
- **Cloudinary Account** (for image storage credentials)

---

### 1️⃣ Database & Backend Setup

1. Configure your PostgreSQL database credentials and Cloudinary keys in `backend/src/main/resources/application.properties` or via environment variables:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/scms_db
   spring.datasource.username=your_db_user
   spring.datasource.password=your_db_password

   jwt.secret=your_64_character_hex_jwt_secret_key
   cloudinary.cloud_name=your_cloud_name
   cloudinary.api_key=your_api_key
   cloudinary.api_secret=your_api_secret
   ```
2. Navigate to the backend directory and start the Spring Boot server:
   ```bash
   cd backend
   mvn spring-boot:run
   ```
   *The backend API will start on `http://localhost:8080`.*

---

### 2️⃣ Frontend Setup

1. Navigate to the frontend directory and install dependencies:
   ```bash
   cd frontend
   npm install
   ```
2. Start the Vite development server:
   ```bash
   npm run dev
   ```
   *The web application will be accessible at `http://localhost:5173`.*

---

## 📄 License
This project is open-source and available under the [MIT License](LICENSE).
