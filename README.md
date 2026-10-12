# AI-Driven Cyber Threat Monitoring & Awareness Network for Educational Institutions

A full-stack cybersecurity monitoring and awareness platform for educational institutions. It combines suspicious-content reporting, rule-based analysis, machine-learning inference, incident review, cyber alerts, and awareness resources.

## Table of Contents

- [Project Overview](#project-overview)
- [Problem Statement](#problem-statement)
- [Key Features](#key-features)
- [User Roles](#user-roles)
- [Threat Detection Workflow](#threat-detection-workflow)
- [AI/ML Components](#aiml-components)
- [System Architecture](#system-architecture)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Installation and Setup](#installation-and-setup)
- [Environment Configuration](#environment-configuration)
- [Running the Application](#running-the-application)
- [Main API Modules](#main-api-modules)
- [Testing](#testing)
- [Security Considerations](#security-considerations)
- [Current Limitations](#current-limitations)
- [Future Enhancements](#future-enhancements)
- [Contributing](#contributing)
- [License](#license)
- [Project Links](#project-links)

## Project Overview

Educational institutions can be targeted by phishing messages, deceptive login pages, suspicious links, and harmful documents. Users need a clear way to report suspicious content, while security administrators need tools to review reports and communicate warnings.

The AI-Driven Cyber Threat Monitoring & Awareness Network is designed as a multi-service application consisting of:

- A React frontend for student and administrator workflows.
- A Spring Boot backend for REST APIs, application logic, persistence, and access control.
- A MySQL database for application records.
- A Python FastAPI service for machine-learning inference.
- Rule-based checks and model predictions to support threat analysis.
- Administrative dashboards, incident review, threat history, and cyber alerts.

Detection results are decision-support signals. They do not guarantee that a file, URL, or message is safe or malicious.

## Problem Statement

### The Problem

- **Phishing and impersonation:** Attackers may imitate campus portals, libraries, finance offices, or other trusted services.
- **Multiple delivery formats:** Suspicious content can arrive as URLs, email text, documents, or screenshots.
- **Delayed reporting:** Users may not know how to report suspicious items, delaying review.
- **Fragmented awareness:** Security guidance and incident alerts may not reach the people who need them.

### Objectives

1. Provide a guided process for reporting suspicious content.
2. Combine configurable heuristic checks with machine-learning predictions.
3. Maintain incident records and status history.
4. Give administrators a central workflow for reviewing reports.
5. Publish cybersecurity alerts and awareness information.
6. Help campus users recognize common online threats.

## Key Features

### Student and Campus User Features

- **Suspicious URL Reporting:** Submit a URL for configured lexical, structural, and rule-based analysis.
- **Phishing Email Reporting:** Submit email text and related details for analysis.
- **Document Analysis:** Submit supported documents for text extraction and threat checks, where configured.
- **Screenshot Analysis:** Use OCR to extract visible text from screenshots where the OCR dependency is installed and configured.
- **My Reports:** Review submitted reports and their current statuses.
- **Cyber Alerts:** View available security bulletins and threat advisories.
- **Awareness and Learning:** Access cybersecurity guidance and educational content.
- **Domain Checks:** Check reported domains against the application's configured domain data.

### Administrator Features

- **Dashboard and Analytics:** Review available threat statistics and incident summaries.
- **Incident Triage:** Inspect submitted reports and their analysis results.
- **Incident Decisions:** Verify, monitor, or dismiss reports where those actions are implemented.
- **Alert Management:** Publish, update, or withdraw cyber alerts where supported.
- **User Management:** Search and manage user accounts where supported.
- **Threat History:** Review recorded incident and administrative history.
- **Blocked-Domain Registry:** Review domains recorded by the application.

Feature availability depends on the current source-code implementation and configuration.

## User Roles

| Role | Purpose |
|---|---|
| `STUDENT` | Submit suspicious content, view personal reports, access awareness resources, and view available alerts. |
| `ADMIN` | Review incidents, manage alerts, access administrative views, and use supported user-management functions. |

Authorization should be enforced by the backend. Hiding a page or button in the frontend is not sufficient protection for an administrative operation.

## Threat Detection Workflow

The project describes a multi-stage threat-analysis workflow. Confirm the exact execution path against the current implementation.

```text
Threat Input (URL / Email / Document / Image)
                       |
                       v
              Input Validation
                       |
                       v
          Content Extraction (if needed)
                       |
                       v
             ML Inference Service
                       |
                       v
          Heuristic and Pattern Checks
                       |
                       v
           Risk Classification / Summary
                       |
                       v
              Store Incident Record
                       |
                       v
              Administrator Review
                       |
                       v
                Alert / Follow-up
```

1. A user submits suspicious content through the frontend.
2. The backend validates the request and processes the supported input type.
3. The application extracts text or features where required.
4. The backend calls the ML service when configured and available.
5. Model output and configured heuristic checks contribute to the analysis.
6. The application records the report and its available results.
7. An administrator reviews the incident and chooses an available status or response.
8. Relevant alerts and history are displayed through the application.

A risk score is an estimate produced by configured logic, not a definitive security verdict.

## AI/ML Components

The project documentation describes a Python ML service. Verify the current model files and implementation before relying on exact behavior.

### URL Classification

- A Random Forest classifier is documented for URL-related features.
- Potential features include URL length, domain structure, suspicious characters, IP-address usage, and selected lexical indicators.
- The model output may contribute to the application's overall threat assessment.

### Text and Phishing Classification

- A scikit-learn text pipeline is documented using TF-IDF vectorization and Logistic Regression.
- Text processing may consider word patterns and language commonly associated with phishing or urgency.
- Predictions depend on the training data and preprocessing used by the model.

### Document Parsing and OCR

- Apache Tika is documented for extracting text and metadata from supported document formats.
- Tesseract OCR and Pillow are documented for extracting text from images or screenshots.
- Extracted text can be routed into text-analysis logic where implemented.

### Model Limitations

- Model predictions may produce false positives and false negatives.
- Training data may not represent current or institution-specific threats.
- A model score does not prove that a resource is safe.
- Do not upload confidential institutional data unless the deployment is approved for that purpose.
- No verified model-accuracy figures are claimed in this README.

## System Architecture

```text
                 Students and Administrators
                              |
                              v
                    React Frontend (Vite)
                              |
                       HTTP / REST API
                              |
                              v
                    Spring Boot Backend
                     /                \
                    v                  v
              MySQL Database      FastAPI ML Service
                                         |
                                         v
                                  ML Inference Models
```

| Component | Responsibility |
|---|---|
| Frontend | User interface, forms, dashboards, and API requests. |
| Spring Boot Backend | Business logic, REST endpoints, persistence coordination, and security configuration. |
| MySQL | Persistent storage for supported application data. |
| FastAPI ML Service | Model inference through configured endpoints. |
| Document/OCR Tools | Content extraction for supported input types. |

## Technology Stack

| Layer | Technology | Purpose |
|---|---|---|
| Frontend | React | Interactive web interface |
| Build Tool | Vite | Frontend development and production build |
| Styling | Tailwind CSS | Utility-based UI styling |
| Charts | Recharts | Data visualization, where used |
| HTTP Client | Axios | Frontend-to-backend requests, where used |
| Backend Language | Java 17 | Backend application |
| Backend Framework | Spring Boot | REST APIs and application configuration |
| Security | Spring Security and JWT libraries | Authentication and access control, as implemented |
| Persistence | Spring Data JPA / Hibernate | Database mapping and persistence |
| Database | MySQL | Relational data storage |
| ML Language | Python 3.10+ | ML service and related scripts |
| ML API | FastAPI | Inference API |
| ASGI Server | Uvicorn | Runs the FastAPI application |
| ML Library | scikit-learn | Classification and text-processing pipelines |
| Document Parsing | Apache Tika | Document text and metadata extraction, where configured |
| OCR | Tesseract, pytesseract, and Pillow | Image text extraction, where configured |
| Containers | Docker | Container packaging, where configured |

Check `package.json`, `pom.xml`, and `requirements.txt` for the actual dependency versions.

## Project Structure

The documented high-level layout is shown below. Confirm the exact directory structure in the current repository.

```text
cyber-threat-monitoring-system/
├── cyber-threat-frontend/              # React frontend
│   ├── public/
│   ├── src/
│   │   ├── components/
│   │   ├── context/
│   │   ├── layouts/
│   │   ├── pages/
│   │   │   ├── admin/
│   │   │   ├── auth/
│   │   │   └── student/
│   │   ├── services/
│   │   └── utils/
│   ├── package.json
│   └── vite.config.js
├── cyber-threat-monitoring-system/      # Spring Boot backend
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   └── resources/
│   │   └── test/
│   ├── pom.xml
│   └── Dockerfile
├── ml-service/                          # Python ML service
│   ├── models/
│   ├── main.py
│   ├── requirements.txt
│   └── train_models.py
├── uploads/                             # Local uploads, if used
├── .gitignore
└── README.md
```

### Important Directories

- `cyber-threat-frontend/src/pages/` — Frontend pages and dashboards.
- `cyber-threat-frontend/src/services/` — API communication helpers, where present.
- `cyber-threat-monitoring-system/src/main/java/` — Java backend source code.
- `cyber-threat-monitoring-system/src/main/resources/` — Backend configuration resources.
- `cyber-threat-monitoring-system/src/test/` — Backend tests.
- `ml-service/` — Python inference service and model-related scripts.
- `uploads/` — May be used for local file uploads. Verify storage and access restrictions before deployment.

## Prerequisites

Install the tools required by the actual project configuration:

- Node.js and npm
- JDK 17
- Maven or the repository's Maven Wrapper
- Python 3.10 or a compatible version
- MySQL 8.0 or a compatible instance
- Tesseract OCR, if image OCR is required
- Git

Check installed versions:

```bash
node --version
npm --version
java -version
python --version
git --version
mvn -version
```

If the repository includes a Maven Wrapper, use it instead of installing Maven separately.

## Installation and Setup

### 1. Clone the Repository

```bash
git clone https://github.com/Noormohamed8056/cyber-threat-monitoring-system.git
cd cyber-threat-monitoring-system
```

### 2. Set Up the ML Service

Open a terminal at the repository root:

```bash
cd ml-service
python -m venv .venv
```

Activate the environment on Windows PowerShell:

```powershell
.\.venv\Scripts\Activate.ps1
```

On Windows Command Prompt:

```bat
.venv\Scripts\activate.bat
```

On Linux or macOS:

```bash
source .venv/bin/activate
```

Install dependencies:

```bash
python -m pip install --upgrade pip
pip install -r requirements.txt
```

If model files are missing or you intentionally want to retrain them, inspect `train_models.py` and follow the procedure documented in the source. Do not retrain models automatically just to start the application.

### 3. Configure MySQL

Start MySQL and create a development database. For example:

```sql
CREATE DATABASE cyber_threat_db;
```

Use your own local database credentials. Do not put production credentials or cloud database URLs in the README or commit them to Git.

### 4. Configure the Backend

Open a separate terminal at the repository root:

```bash
cd cyber-threat-monitoring-system
```

Configure the database connection, JWT secret, and ML service URL using the mechanism supported by the backend.

Build using the Maven Wrapper if it exists.

**Windows:**

```powershell
.\mvnw.cmd clean compile
```

**Linux or macOS:**

```bash
./mvnw clean compile
```

If the wrapper is absent, use Maven if installed:

```bash
mvn clean compile
```

### 5. Install Frontend Dependencies

Open another terminal at the repository root:

```bash
cd cyber-threat-frontend
npm install
```

Configure the frontend API base URL through the configuration mechanism supported by the project. Never put server-side credentials in frontend variables.

## Environment Configuration

Use environment variables or another local configuration method supported by the source code.

The following names are suggested configuration placeholders. Verify them against the current implementation before use.

| Variable | Purpose | Example Placeholder |
|---|---|---|
| `SPRING_DATASOURCE_URL` | MySQL JDBC connection URL | `jdbc:mysql://localhost:3306/cyber_threat_db` |
| `DB_USERNAME` | MySQL username | `your_database_username` |
| `DB_PASSWORD` | MySQL password | `your_database_password` |
| `JWT_SECRET` | Secret used to sign JWTs | `your_long_random_secret` |
| `JWT_EXPIRATION` | Token validity duration, if configurable | `604800000` |
| `ML_SERVICE_URL` | ML inference endpoint | `http://localhost:8000/predict` |
| `UPLOAD_DIR` | Local upload directory, if configurable | `uploads/` |
| `VITE_API_BASE_URL` | Frontend API base URL, if configurable | `http://localhost:8080/api` |

### Configuration Security Warning

An earlier source-code review reported a hard-coded JWT signing secret and a cloud database connection URL in the backend configuration. This README does not reproduce those values.

Before public release, externalize the JWT secret and database URL, review the tracked configuration, and rotate potentially exposed credentials. Updating the README alone does not fix secrets in source code or remove them from Git history.

Important notes:

- These placeholders are not real credentials.
- Variables must be supported by the application before they will take effect.
- Do not use a weak fallback JWT secret in production.
- Keep signing keys and database passwords outside source control.
- Never put a server-side JWT secret, database password, or API secret in frontend code.
- Do not publish `.env` files.
- Keep local secret files excluded from Git.
- Do not commit real cloud database connection details.

## Running the Application

Run the services in separate terminals. These commands assume the documented directory layout and common local ports. Verify the application's configuration if they differ.

### Terminal 1: Start the ML Service

From the `ml-service` directory, activate `.venv` and run:

```bash
uvicorn main:app --host 127.0.0.1 --port 8000
```

If the entry point or configured port differs, use the values defined in the ML service.

If enabled, FastAPI's interactive API documentation is commonly available at:

```text
http://localhost:8000/docs
```

### Terminal 2: Start the Backend

**Windows:**

```powershell
.\mvnw.cmd spring-boot:run
```

**Linux or macOS:**

```bash
./mvnw spring-boot:run
```

Or, if Maven is installed and the wrapper is absent:

```bash
mvn spring-boot:run
```

The backend commonly uses port `8080` unless configured otherwise.

### Terminal 3: Start the Frontend

From the frontend directory:

```bash
npm run dev
```

Vite commonly serves local development applications at:

```text
http://localhost:5173
```

Open the URL printed in your terminal. Keep the backend and ML service running when the frontend depends on them.

## Main API Modules

The following API groups are documented for the project. Verify current controller mappings and authorization annotations before relying on exact endpoint details.

| Module | Purpose |
|---|---|
| `/api/auth` | Registration, login, token validation, and profile operations |
| `/api/reports/intelligent` | Multi-format threat submissions and analysis summaries |
| `/api/reports` | Report creation, personal reports, status updates, and domain checks |
| `/api/alerts` | Retrieve and manage cyber alerts |
| `/api/admin` | Administrative dashboard, analytics, user management, and history |
| `/predict` | ML inference endpoint in the Python service, if exposed as documented |

Protected backend endpoints may require an authorization header:

```http
Authorization: Bearer <YOUR_JWT_TOKEN>
```

Use a real token only in your local test environment. Never publish tokens in README examples, screenshots, source control, or issue reports.

For exact HTTP methods, request bodies, response formats, and access requirements, consult the current controller classes and FastAPI application.

## Testing

### Backend Tests

If the Maven Wrapper is available, run from the backend directory.

**Windows:**

```powershell
.\mvnw.cmd test
```

**Linux or macOS:**

```bash
./mvnw test
```

Otherwise, use Maven if installed:

```bash
mvn test
```

### Frontend Checks

Inspect `package.json` to confirm available scripts. If a lint script exists, run:

```bash
npm run lint
```

### ML Service Checks

Review the tests or inference schema in the ML service. If FastAPI exposes interactive API documentation, use the local `/docs` page to inspect the expected request format.

The existence of a test file or a successful build does not prove that every feature works correctly.

## Security Considerations

- **Secret Management:** Keep database credentials, JWT signing keys, and service tokens outside source control.
- **Credential Rotation:** Rotate secrets that may have been committed to a public repository.
- **Authorization:** Verify protected operations enforce role checks on the backend.
- **File Uploads:** Validate file types and sizes, use safe filenames, and restrict access to uploaded content.
- **CORS and Transport:** Configure trusted frontend origins and use HTTPS for deployed environments.
- **Database Security:** Use least-privilege database accounts and avoid publishing cloud connection details.
- **ML Safety:** Treat predictions as indicators requiring appropriate review, not definitive verdicts.
- **Safe Testing:** Use synthetic or authorized test data. Do not test against systems or URLs without permission.
- **Privacy:** Avoid collecting or publishing confidential student, staff, or institutional information.

An earlier review reported that `/uploads/**` was permitted in the backend security configuration. Confirm whether these files are intended to be public. If they may contain private submissions, restrict access and validate file handling before deployment.

A README description does not guarantee that every security control is implemented or correctly configured.

## Current Limitations

Potential limitations that should be evaluated against the current implementation include:

- **Synchronous Inference:** If the backend calls the ML service synchronously, this may affect throughput under load.
- **Training Data:** Model performance depends on the quality and representativeness of the training data.
- **Local Upload Storage:** Local disk storage may not persist across redeployments or work reliably across multiple backend instances.
- **OCR Dependency:** Screenshot text extraction depends on a correctly installed and configured OCR engine.
- **Model Uncertainty:** False positives and false negatives are possible.
- **Deployment Configuration:** Production URLs, database settings, secrets, and CORS rules must be configured for the target environment.

Confirm which limitations apply to the current version before presenting them as definitive implementation findings.

## Future Enhancements

- Secure threat-indicator sharing between participating institutions.
- Browser extensions for easier suspicious-link reporting.
- Mobile notifications for important cyber alerts.
- Clearer explanations of detected phishing indicators.
- Expanded model evaluation and monitoring.
- More automated integration and authorization tests.
- Secure object storage for uploaded documents and screenshots.
- Improved operational monitoring and error reporting.

These are possible future improvements, not claims that the functionality is already implemented.

## Contributing

Contributions and improvements are welcome.

1. Fork the repository.
2. Create a feature branch.
3. Make focused changes.
4. Run relevant tests and checks.
5. Commit your changes with a descriptive message.
6. Push your branch and open a pull request.

Example:

```bash
git checkout -b feature/improve-threat-analysis
git add .
git commit -m "Improve threat analysis documentation"
git push origin feature/improve-threat-analysis
```

Before committing, check that you have not included `.env` files, credentials, private datasets, uploaded documents, or generated files.


## Project Links

- **Source Code:** [GitHub Repository](https://github.com/Noormohamed8056/cyber-threat-monitoring-system)
- **Frontend:** [Cyber Threat Monitoring System](https://cyber-threat-monitoring-system.vercel.app)

