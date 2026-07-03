# Smart Complaint Management System (SCMS) 🚀

![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.5-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![React](https://img.shields.io/badge/React-19-20232A?style=for-the-badge&logo=react&logoColor=61DAFB)
![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS-v4-38B2AC?style=for-the-badge&logo=tailwind-css&logoColor=white)
![TanStack Query](https://img.shields.io/badge/TanStack_Query-v5-FF4154?style=for-the-badge&logo=react-query&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-Auth-000000?style=for-the-badge&logo=json-web-tokens&logoColor=white)
![Cloudinary](https://img.shields.io/badge/Cloudinary-Media-3448C5?style=for-the-badge&logo=cloudinary&logoColor=white)

An enterprise-grade, full-stack **Smart Complaint & Issue Management Platform** designed for organizations, educational institutions, and municipalities. It streamlines issue reporting, department routing, staff assignment, and resolution tracking using **Role-Based Access Control (RBAC)**, **stateless JWT authentication**, and **innovative QR-code location tagging**.

---

## ✨ Key Features

- **🛡️ Role-Based Access Control (RBAC):** Distinct workflows tailored for 4 organizational tiers:
  - **Reporter:** Submit complaints with detailed descriptions, category selection, location tagging, and image proof. View live status updates and complete history trails.
  - **Department Coordinator:** View incoming department-specific complaints, triage priorities, and assign tickets to dedicated staff members.
  - **Staff Member:** View assigned tickets, update resolution status (*In Progress*, *Resolved*, *Closed*), and add maintenance remarks.
  - **Admin:** Access enterprise-wide analytical dashboards and Recharts-powered visual reports.
- **📍 QR Code Location Tagging:** Report issues instantly by linking complaints to physical room/zone QR UUIDs (`LocationQR`).
- **📸 Cloudinary Media Integration:** Seamless multipart file uploads for storing high-resolution image evidence.
- **📜 Complete Audit Trail:** Automated history tracking (`ComplaintHistory`) capturing timestamped state transitions and user remarks.
- **📊 Interactive Analytics:** Visual reports and charts built with Recharts to monitor department efficiency and resolution timelines.
- **🧪 Automated Test Pipeline:** Includes End-to-End API verification workflows and headless/visual UI automation scripts using Puppeteer.

---

## 🛠️ Technology Stack

### Backend
- **Framework:** Spring Boot 3.2.5 (Java 17)
- **Security:** Spring Security + Stateless JWT (JSON Web Tokens)
- **Database & ORM:** PostgreSQL + Spring Data JPA / Hibernate
- **Integrations:** Cloudinary API (Image Storage), JavaMail Sender

### Frontend
- **Framework & Build Tool:** React 19 + Vite
- **Styling:** Tailwind CSS v4 + Lucide React Icons
- **State Management & Caching:** TanStack React Query v5
- **Routing & Forms:** React Router DOM v7 + React Hook Form
- **Data Visualization:** Recharts

### Testing & Quality Assurance
- **API E2E Testing:** Node.js automated workflow (`test-workflow.mjs`)
- **Visual UI Automation:** Puppeteer end-to-end browser testing (`ui-test.mjs`)

---

## 🏛️ System Architecture & Roles

| Role | Primary Responsibility | Key Capabilities |
| :--- | :--- | :--- |
| **`REPORTER`** | General Students / Employees | Create complaints, attach image proof, tag room QR codes, track status. |
| **`DEPARTMENT_COORDINATOR`** | Department Heads | Filter department backlog, assign tasks to specialized staff members. |
| **`STAFF`** | Maintenance & IT Staff | Receive assigned tickets, update workflow state, log repair remarks. |
| **`ADMIN`** | System Administrator | System monitoring, department analytics, user management. |

---

## 🚀 Getting Started

### Prerequisites
- **Java 17+** and **Maven**
- **Node.js (v18+)** and **npm**
- **PostgreSQL** (Local instance or Cloud DB like Neon/Supabase)
- **Cloudinary Account** (for image storage credentials)

---

### 1️⃣ Database & Backend Setup

1. Configure your PostgreSQL database credentials and Cloudinary keys in `backend/src/main/resources/application.properties`:
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

## 🧪 Running Automated Tests

The repository comes pre-packaged with automated E2E testing scripts in the root directory.

### 1. End-to-End API Workflow Test
Tests the full complaint lifecycle across Reporter, Coordinator, and Staff APIs:
```bash
node test-workflow.mjs
```

### 2. Puppeteer Visual UI Test
Launches a browser to simulate user registration, login, complaint submission, and coordinator routing:
```bash
node ui-test.mjs
```

---

## 📄 License
This project is open-source and available under the [MIT License](LICENSE).
