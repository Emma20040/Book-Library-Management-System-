# 📚 Digital Library Management System

A robust digital library management system built with Spring Boot (Java 21) that enables users to browse, read, and manage digital books. The system supports both free and paid content, complete with secure payment processing and comprehensive user management.

## 🌟 Key Features

### 🔐 User Authentication & Authorization
- JWT-based authentication
- Email verification
- Password reset functionality
- Role-based access control (Admin/User)


### 📖 Book Management
- Full CRUD operations for books
- PDF and cover image support
- Genre-based categorization
- Advanced search and filtering
- Pagination support

### 💳 Payment Processing
- Stripe integration
- One-time and subscription-based access
- Transaction history
- Secure payment flow with webhook support

### 🛡️ Content Access Control
- Time-limited access to paid content
- Free/Paid content categorization
- Secure access validation

### 👤 User Experience
- Profile management
- Profile pictures
- Reading history
- Email notifications

## 🛠 Tech Stack

- **Backend**: Spring Boot, Java 21
- **Security**: Spring Security, JWT
- **Database**: MySQL
- **File Storage**: AWS S3
- **Payment**: Stripe Integration
- **Email**: JavaMailSender
- **Build Tool**: Maven

## 🚀 Getting Started

### Prerequisites
- Java 21+
- Maven 3.6.0+
- MySQL
- Stripe account (for payments)
- SMTP server (for email)

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/Emma20040/Book-Library-Management-System-.git
   cd management-system
   ```

2. **Configure Environment**
   Create `.env` file with:
   ```env
   # Database
   DB_URL=jdbc:mysql://localhost:3306/library_management_system
   DB_USERNAME=your_username
   DB_PASSWORD=your_password
   
   # JWT
   jwt.private-key=classpath:private.key
   jwt.public-key= classpath:public.key
   JWT_EXPIRATION=8600000
   
   # Email
   MAIL_HOST=smtp.example.com
   MAIL_PORT=587
   MAIL_USERNAME=your_email@example.com
   MAIL_PASSWORD=your_email_password
   
   # Stripe
   STRIPE_SECRET_KEY=your_stripe_key
   STRIPE_WEBHOOK_SECRET=your_webhook_secret
   ```

3. **Database Setup**
   ```sql
   CREATE DATABASE library_management_system;
   ```

4. **Build & Run**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```
  

## 📚 API Documentation

For detailed API endpoints and usage, see [API Documentation](USER_API_DOCUMENTATION.md).

## 🔒 Security

- JWT Authentication
- BCrypt password hashing
- CORS Configuration
- Input Validation
- Secure File Uploads

## 🧪 Testing

Run tests with:
```bash
mvn test
```

## Reference
-https://www.codingshuttle.com/blogs/integrating-stripe-payments-in-spring-boot-step-by-step-beginner-s-guide-2025/
-https://dev.to/mspilari/login-system-with-jwt-token-and-email-reset-password-571


## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/FeatureBranch`)
3. Commit your changes (`git commit -m 'Add some New Feature`)
4. Push to the branch (`git push origin feature/FeatureBranch`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
