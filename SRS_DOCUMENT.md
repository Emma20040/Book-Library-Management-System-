# Software Requirements Specification (SRS)

## ReadQuest — Digital Library Management System

| Field | Details |
|---|---|
| **Document Title** | Software Requirements Specification |
| **Project Name** | ReadQuest — Digital Library Management System |
| **Version** | 1.0 |
| **Date** | April 15, 2026 |
| **Prepared By** | Emma |
| **Repository** | [Book-Library-Management-System-](https://github.com/Emma20040/Book-Library-Management-System-.git) |
| **License** | MIT License |

---

## Table of Contents

1. [Introduction](#1-introduction)
   - 1.1 [Purpose](#11-purpose)
   - 1.2 [Scope](#12-scope)
   - 1.3 [Definitions, Acronyms, and Abbreviations](#13-definitions-acronyms-and-abbreviations)
   - 1.4 [References](#14-references)
   - 1.5 [Overview of the Document](#15-overview-of-the-document)
2. [Overall Description](#2-overall-description)
   - 2.1 [Product Perspective](#21-product-perspective)
   - 2.2 [Product Functions (Summary)](#22-product-functions-summary)
   - 2.3 [User Classes and Characteristics](#23-user-classes-and-characteristics)
   - 2.4 [Operating Environment](#24-operating-environment)
   - 2.5 [Design and Implementation Constraints](#25-design-and-implementation-constraints)
   - 2.6 [Assumptions and Dependencies](#26-assumptions-and-dependencies)
3. [System Architecture](#3-system-architecture)
   - 3.1 [High-Level Architecture](#31-high-level-architecture)
   - 3.2 [Technology Stack](#32-technology-stack)
   - 3.3 [Database Schema (Entity-Relationship Summary)](#33-database-schema-entity-relationship-summary)
4. [Functional Requirements](#4-functional-requirements)
   - 4.1 [User Authentication & Registration Module](#41-user-authentication--registration-module)
   - 4.2 [User Profile Management Module](#42-user-profile-management-module)
   - 4.3 [Book Management Module](#43-book-management-module)
   - 4.4 [Payment & Book Access Module](#44-payment--book-access-module)
   - 4.5 [Contact / Support Module](#45-contact--support-module)
   - 4.6 [Mailing List (Newsletter) Module](#46-mailing-list-newsletter-module)
   - 4.7 [Report Generation Module](#47-report-generation-module)
   - 4.8 [Admin User Management Module](#48-admin-user-management-module)
5. [API Endpoint Specification](#5-api-endpoint-specification)
   - 5.1 [User Endpoints](#51-user-endpoints)
   - 5.2 [Book Endpoints](#52-book-endpoints)
   - 5.3 [Payment Endpoints](#53-payment-endpoints)
   - 5.4 [Contact Endpoints](#54-contact-endpoints)
   - 5.5 [Mailing List Endpoints](#55-mailing-list-endpoints)
   - 5.6 [Report Endpoints](#56-report-endpoints)
   - 5.7 [Webhook Endpoints](#57-webhook-endpoints)
6. [Non-Functional Requirements](#6-non-functional-requirements)
   - 6.1 [Performance](#61-performance)
   - 6.2 [Security](#62-security)
   - 6.3 [Scalability](#63-scalability)
   - 6.4 [Reliability & Availability](#64-reliability--availability)
   - 6.5 [Usability](#65-usability)
   - 6.6 [Maintainability](#66-maintainability)
   - 6.7 [Portability](#67-portability)
7. [Data Dictionary](#7-data-dictionary)
8. [UML Diagrams](#8-uml-diagrams)
   - 8.1 [Use-Case Diagram](#81-use-case-diagram)
   - 8.2 [Class Diagram](#82-class-diagram)
   - 8.3 [Activity Diagrams](#83-activity-diagrams)
9. [Error Handling & Status Codes](#9-error-handling--status-codes)
10. [Appendices](#10-appendices)
    - 10.1 [Environment Configuration](#101-environment-configuration)
    - 10.2 [Database Initialisation](#102-database-initialisation)
    - 10.3 [Build & Run Instructions](#103-build--run-instructions)

---

## 1. Introduction

### 1.1 Purpose

The purpose of this Software Requirements Specification (SRS) document is to provide a complete and detailed description of the **ReadQuest — Digital Library Management System**. This document describes all functional and non-functional requirements, system architecture, API specifications, data models, and design constraints for the system. It is intended to serve as the primary reference for developers, testers, project managers, academic supervisors, and any stakeholders involved in the development, evaluation, or maintenance of the system.

### 1.2 Scope

ReadQuest is a web-based digital library management system that enables users to browse, search, read, and purchase time-limited access to digital books (PDFs). The system supports both **free** and **paid** content with secure payment processing via Stripe. It provides comprehensive user management with role-based access control (Admin and User roles), profile management with profile picture upload, email verification, password reset, and a contact/support system.

**Key capabilities include:**
- User registration with email verification
- JWT-based authentication and authorization (RSA asymmetric keys)
- Book catalogue management (CRUD) with PDF and cover image uploads
- Free and paid book access control with time-limited subscriptions
- Stripe payment integration with webhook-based payment confirmation
- Book search with SOUNDEX-based fuzzy matching and genre filtering
- User profile management with profile picture upload/delete
- Contact/support messaging system with email notifications to admin
- Mailing list subscription with email verification
- Asynchronous PDF report generation for administrators
- Admin dashboard with user management (suspend, activate, delete)
- Email notifications for verification, password reset, and payment confirmation

**Out of scope:** The frontend client application is not part of this backend system. This SRS covers only the backend REST API server.

### 1.3 Definitions, Acronyms, and Abbreviations

| Term | Definition |
|---|---|
| **SRS** | Software Requirements Specification |
| **API** | Application Programming Interface |
| **REST** | Representational State Transfer |
| **JWT** | JSON Web Token — a compact, URL-safe means of representing claims for authentication |
| **RSA** | Rivest–Shamir–Adleman — an asymmetric cryptographic algorithm used for JWT signing |
| **CRUD** | Create, Read, Update, Delete |
| **DTO** | Data Transfer Object — an object that carries data between processes |
| **JPA** | Java Persistence API — specification for ORM in Java |
| **ORM** | Object-Relational Mapping |
| **CORS** | Cross-Origin Resource Sharing |
| **CSRF** | Cross-Site Request Forgery |
| **BCrypt** | A password-hashing function based on the Blowfish cipher |
| **Stripe** | A third-party online payment processing platform |
| **Webhook** | An HTTP callback that is triggered by an event in a source system |
| **UUID** | Universally Unique Identifier |
| **SOUNDEX** | A phonetic algorithm for indexing names by sound |
| **PDF** | Portable Document Format |
| **SMTP** | Simple Mail Transfer Protocol |
| **MIME** | Multipurpose Internet Mail Extensions |
| **RBAC** | Role-Based Access Control |

### 1.4 References

1. IEEE Std 830-1998 — IEEE Recommended Practice for Software Requirements Specifications
2. Spring Boot 3.5.5 Official Documentation — https://docs.spring.io/spring-boot/
3. Spring Security Reference — https://docs.spring.io/spring-security/reference/
4. Stripe API Documentation — https://stripe.com/docs/api
5. Stripe Payments in Spring Boot Guide — https://www.codingshuttle.com/blogs/integrating-stripe-payments-in-spring-boot-step-by-step-beginner-s-guide-2025/
6. JWT with Email Reset Password — https://dev.to/mspilari/login-system-with-jwt-token-and-email-reset-password-571
7. Background PDF Generation with Spring Boot — https://medium.com/@AlexanderObregon/background-pdf-generation-with-spring-boot-and-queued-jobs-4dab47a8e458

### 1.5 Overview of the Document

- **Section 2** provides a high-level description of the product, user classes, and operating environment.
- **Section 3** describes the system architecture, technology stack, and database schema.
- **Section 4** specifies all functional requirements grouped by module.
- **Section 5** provides a detailed API endpoint specification for every route in the system.
- **Section 6** specifies non-functional requirements (performance, security, scalability, etc.).
- **Section 7** contains the data dictionary for all database entities.
- **Section 8** identifies where UML diagrams (use-case, class, and activity diagrams) should be inserted.
- **Section 9** documents the error handling strategy and HTTP status codes.
- **Section 10** contains appendices with configuration, database setup, and build instructions.

---

## 2. Overall Description

### 2.1 Product Perspective

ReadQuest is a **standalone backend REST API server** designed to power a digital library platform. It exposes RESTful endpoints consumed by a frontend client (e.g., a single-page application served at `http://localhost:5502`). The system integrates with the following external services:

- **MySQL Database** — Persistent storage for all application data (users, books, transactions, etc.)
- **Stripe Payment Gateway** — Processes payments for paid book access via Stripe Checkout Sessions and Webhooks
- **SMTP Email Server (Gmail)** — Sends transactional emails (verification, password reset, payment confirmation, support notifications)
- **Local File System** — Stores uploaded files (book PDFs, cover images, profile pictures, generated PDF reports) in the `uploads/` directory

The system follows a **layered architecture**: Controllers → Services → Repositories → Database, with configuration managed through Spring Boot profiles (`dev` and `prod`).

### 2.2 Product Functions (Summary)

| # | Function | Description |
|---|---|---|
| F1 | User Registration | Users register with username, email, and password; a verification email is sent |
| F2 | Email Verification | Users verify their email via a token-based link |
| F3 | User Login | Users authenticate via email/username + password; receive a JWT token |
| F4 | User Logout | JWT token is blacklisted to invalidate the session |
| F5 | Password Reset | Users request a 6-digit reset code via email; use the code to set a new password |
| F6 | Profile Management | Users view and update their profile (name, address, bio, phone, country) |
| F7 | Profile Picture | Users upload, view, and delete their profile picture |
| F8 | Book CRUD (Admin) | Admins create, update, and delete books with PDF and cover image uploads |
| F9 | Book Browsing | Users browse all books with pagination, sorted by creation date |
| F10 | Book Search | Users search books by title, author, genre, or ISBN with fuzzy matching |
| F11 | Genre Filtering | Users filter books by genre with pagination |
| F12 | Book Reading | Authenticated users with access can read books via secure PDF streaming |
| F13 | Payment Processing | Users pay for time-limited book access via Stripe Checkout |
| F14 | Payment Webhook | Stripe webhook confirms payment and grants book access |
| F15 | Transaction History | Users view their transaction history with access dates |
| F16 | Book Access Check | System checks if a user has active access to a specific book |
| F17 | Contact Support | Authenticated users submit support messages; admin notified via email |
| F18 | Mailing List | Users subscribe to a newsletter with email verification |
| F19 | Report Generation | Admins generate PDF reports asynchronously; download when ready |
| F20 | Admin User Management | Admins list all users, view user profiles, suspend/activate/delete user accounts |
| F21 | Dashboard Counts | Admins view total counts of users, books, and transactions |

### 2.3 User Classes and Characteristics

The system supports two distinct user roles, defined by the `Role` enum:

#### 2.3.1 Regular User (Role: `USER`)

- **Description:** An end user of the digital library platform.
- **Capabilities:**
  - Register, verify email, login, and logout
  - Reset forgotten password
  - View and update their profile (name, bio, phone, address, country, profile picture)
  - Browse, search, and filter books
  - Purchase time-limited access to paid books via Stripe
  - Read books (free books immediately; paid books after purchase)
  - View personal transaction history
  - Submit contact/support messages
  - Subscribe to the mailing list
- **Technical proficiency:** Basic familiarity with web applications.

#### 2.3.2 Administrator (Role: `ADMIN`)

- **Description:** A system administrator who manages the library's content and users.
- **Capabilities (in addition to all User capabilities):**
  - Create, update, and delete books (with PDF and cover image management)
  - View any user's profile by username
  - List all users with pagination
  - Suspend and activate user accounts
  - Delete user accounts (cannot delete other admins or self)
  - View book PDFs directly (admin-only PDF viewer)
  - View total user count, total book count, total transaction count
  - Generate and download PDF reports
- **Technical proficiency:** Moderate; managed through direct API access or an admin dashboard.
- **Account creation:** Admin accounts are created manually via SQL insertion (see Appendix 10.2).

### 2.4 Operating Environment

| Component | Specification |
|---|---|
| **Runtime** | Java 21+ (JDK 21) |
| **Framework** | Spring Boot 3.5.5 |
| **Build Tool** | Apache Maven 3.6.0+ |
| **Database** | MySQL (any recent version, e.g. 8.x) |
| **Server Port** | 8080 (configurable via `SERVER_PORT` environment variable) |
| **Operating System** | Cross-platform (Linux, macOS, Windows) |
| **Email Provider** | Gmail SMTP (`smtp.gmail.com:587`) |
| **Payment Gateway** | Stripe (test mode supported) |
| **File Storage** | Local filesystem (`./uploads/` directory) |
| **Spring Profiles** | `dev` (development) and `prod` (production) |

### 2.5 Design and Implementation Constraints

1. **Authentication:** The system uses RSA asymmetric key pairs (public/private) for JWT signing and verification. Keys must be pre-generated and placed in `src/main/resources/`.
2. **Database Schema:** JPA with Hibernate manages schema via `ddl-auto=update` (dev) or `ddl-auto=validate` (prod). No explicit migration tool (e.g., Flyway/Liquibase) is used.
3. **File Storage:** All files (PDFs, images, reports) are stored on the local filesystem under `./uploads/`. No cloud storage (e.g., AWS S3) is used at this time, despite being mentioned in the README as a future consideration.
4. **Payment Processing:** Stripe is the only supported payment provider. Payments are processed in USD.
5. **Token Blacklisting:** JWT blacklisting is stored in-memory (`ConcurrentHashMap`), meaning blacklisted tokens are lost on server restart.
6. **CORS:** The system restricts cross-origin requests to specific frontend origins configured in `SecurityConfig`.
7. **Single-Server Deployment:** The application is designed for single-server deployment. The in-memory JWT blacklist does not support multi-instance scaling without modification.
8. **Max File Sizes:** PDFs up to 100 MB; cover images up to 10 MB; profile pictures up to 5 MB. The global Spring multipart limit is 20 MB.

### 2.6 Assumptions and Dependencies

**Assumptions:**
1. A MySQL database is available and a database named `library_management_system` has been created.
2. RSA key pair files (`private.key` and `public.key`) have been generated and placed in `src/main/resources/`.
3. A valid Gmail account with an App Password is available for sending emails.
4. A Stripe account (test or live) is available with API keys and a webhook endpoint configured.
5. The frontend client handles JWT token storage, automatic logout on token expiry, and provides the token in the `Authorization: Bearer <token>` header for authenticated requests.

**Dependencies:**
- Spring Boot Starter Web, Data JPA, Security, Mail, Validation, Thymeleaf, Freemarker
- Spring Security OAuth2 Resource Server
- MySQL Connector/J
- Lombok
- Stripe Java SDK (v29.5.0)
- Flying Saucer PDF (v10.0.5) — for HTML-to-PDF rendering
- Spring Boot DevTools (development only)

---

## 3. System Architecture

### 3.1 High-Level Architecture

The system follows a classic **Model-View-Controller (MVC)** layered architecture:

```
┌─────────────────────────────────────────────────────────────┐
│                     Frontend Client                         │
│            (SPA at http://localhost:5502)                    │
└─────────────────────┬───────────────────────────────────────┘
                      │  HTTP/REST (JSON)
                      ▼
┌─────────────────────────────────────────────────────────────┐
│                   Security Layer                            │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  JwtBlacklistFilter → OAuth2 Resource Server (JWT)   │   │
│  │  CORS Configuration · CSRF Disabled · RBAC Rules     │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────┬───────────────────────────────────────┘
                      ▼
┌─────────────────────────────────────────────────────────────┐
│                  Controller Layer                           │
│  UserController · BookController · PaymentController        │
│  ContactController · MailingController · ReportController   │
│  StripeWebhookController                                    │
└─────────────────────┬───────────────────────────────────────┘
                      ▼
┌─────────────────────────────────────────────────────────────┐
│                   Service Layer                             │
│  UserService · BookService · PaymentService                 │
│  EmailService · FileStorageService · ContactService         │
│  MailingService · JwtBlacklistService · JwtValidationService│
│  ReportOrchestrationService · GeneralReport                 │
│  JwtActions (Utility)                                       │
└─────────────────────┬───────────────────────────────────────┘
                      ▼
┌─────────────────────────────────────────────────────────────┐
│                 Repository Layer (JPA)                      │
│  UserRepository · BookRepository · TransactionRepository    │
│  BookAccessRepository · ContactUsRepository                 │
│  MailingRepository · ReportJobRepository                    │
└─────────────────────┬───────────────────────────────────────┘
                      ▼
┌─────────────────────────────────────────────────────────────┐
│               MySQL Database                                │
│  Tables: users, book, book_access, transactions,            │
│  support_messages, email_subscribers, pdf_job_tracker        │
└─────────────────────────────────────────────────────────────┘

External Integrations:
  ├── Stripe API ←→ PaymentService (Checkout Sessions)
  ├── Stripe Webhooks → StripeWebhookController
  ├── Gmail SMTP ←→ EmailService
  └── Local Filesystem ←→ FileStorageService (uploads/)
```

### 3.2 Technology Stack

| Layer | Technology | Version |
|---|---|---|
| Language | Java | 21 |
| Framework | Spring Boot | 3.5.5 |
| Security | Spring Security + OAuth2 Resource Server | — |
| Authentication | JWT (RSA RS256 via Nimbus JOSE) | — |
| ORM | Hibernate (via Spring Data JPA) | — |
| Database | MySQL | 8.x |
| DB Driver | MySQL Connector/J | — |
| Payment | Stripe Java SDK | 29.5.0 |
| Email | JavaMailSender (Spring Boot Starter Mail) | — |
| PDF Generation | FreeMarker + Flying Saucer PDF | 10.0.5 |
| Password Hashing | BCrypt (Spring Security Crypto) | — |
| Build Tool | Apache Maven | 3.6.0+ |
| Code Generation | Lombok | — |
| Template Engine | Thymeleaf, FreeMarker | — |
| Async Processing | Spring @Async with ThreadPoolTaskExecutor | — |

### 3.3 Database Schema (Entity-Relationship Summary)

The system defines **7 JPA entities** mapped to MySQL tables:

```
┌──────────────┐       ┌──────────────┐        ┌──────────────────┐
│    users     │       │     book     │        │  transactions    │
│──────────────│       │──────────────│        │──────────────────│
│ id (UUID) PK │──┐    │ id (BIGINT)  │──┐     │ id (UUID) PK     │
│ username     │  │    │ title        │  │     │ user_id FK→users  │
│ email        │  │    │ author       │  │     │ book_id FK→book   │
│ password     │  │    │ isbn         │  │     │ amount            │
│ enabled      │  │    │ description  │  │     │ currency          │
│ role         │  │    │ genre        │  │     │ duration_days     │
│ verif_token  │  │    │ published_dt │  │     │ payment_status    │
│ reset_token  │  │    │ pdf_path     │  │     │ transaction_date  │
│ reset_expiry │  │    │ cover_path   │  │     │ stripe_intent_id  │
│ firstName    │  │    │ price/month  │  │     └──────────────────┘
│ lastName     │  │    │ access_type  │  │               │
│ address      │  │    │ num_pages    │  │               │
│ bio          │  │    │ created_at   │  │     ┌─────────┴────────┐
│ phoneNumber  │  │    │ updated_at   │  │     │   book_access    │
│ country      │  │    └──────────────┘  │     │──────────────────│
│ profile_img  │  │                      │     │ id (BIGINT) PK   │
│ created_at   │  ├──────────────────────┤     │ user_id FK→users │
│ updated_at   │  │                      │     │ book_id FK→book  │
└──────────────┘  │    ┌──────────────┐  │     │ start_date       │
       │          │    │support_msgs  │  │     │ end_date         │
       │          │    │──────────────│  │     │ duration_days    │
       │          │    │ id PK        │  │     │ transaction_id   │
       │          │    │ email        │  │     │   FK→transactions│
       │          │    │ firstName    │  │     └──────────────────┘
       │          │    │ lastName     │  │
       │          │    │ message      │  │     ┌──────────────────┐
       │          │    │ subject      │  │     │email_subscribers │
       │          │    │ created_at   │  │     │──────────────────│
       │          │    │ user_id FK   │  │     │ id PK            │
       │          │    └──────────────┘  │     │ email            │
       │          │                      │     │ verif_token      │
       │          │    ┌──────────────┐  │     │ created_at       │
       │          │    │pdf_job_track │  │     └──────────────────┘
       │          │    │──────────────│  │
       │          └───▶│ user_id FK   │  │
       │               │ status       │  │
       │               │ filePath     │  │
       │               │ errorMessage │  │
       │               │ created_at   │  │
       │               │ completed_at │  │
       │               └──────────────┘  │
       └─────────────────────────────────┘
```

**Relationships:**
- `UserModel` ← one-to-many → `Transaction` (a user has many transactions)
- `Book` ← one-to-many → `Transaction` (a book has many transactions)
- `UserModel` ← one-to-many → `BookAccess` (a user has many book access records)
- `Book` ← one-to-many → `BookAccess` (a book has many access records)
- `Transaction` ← one-to-one → `BookAccess` (each access is linked to one transaction)
- `UserModel` ← one-to-many → `ContactUsModel` (a user can submit many support messages)
- `UserModel` ← one-to-many → `PdfJobTracker` (a user can trigger many report jobs)

---
