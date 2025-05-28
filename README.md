Loan Management System
Project Overview
The Loan Management System is a Spring Boot web application designed to manage the entire loan lifecycle. It allows Customers to apply for loans and track their status, while Loan Officers (Admins) can review, approve, or reject applications and manage repayment schedules.

Key Features
User Authentication: Secure registration/login for Customers and Admins using Spring Security and JWT.

Loan Application: Customers can submit loan applications with essential details.

Admin Dashboard: Loan Officers can view, approve, or reject applications. Approved loans trigger automatic EMI and repayment schedule generation.

EMI & Repayment: Calculates EMIs using the formula EMI= 
(1+R) 
N
 −1
P⋅R⋅(1+R) 
N
 
​
  and tracks payment status.

Customer Dashboard: Provides customers with an overview of their loans, repayment history, and upcoming EMIs.

Technical Stack
Backend: Spring Boot (Spring Web, Spring Security, Spring Data JPA)

Authentication: Spring Security + JWT

Database: PostgreSQL or MySQL

ORM: Hibernate

API Docs: Swagger/OpenAPI

Validation: Bean Validation

Optional Frontend: Thymeleaf or React/Angular

API Endpoints (Examples)
Method

Endpoint

Description

POST

/auth/register

Customer registration

POST

/auth/login

User login

POST

/loans/apply

Submit loan application

GET

/admin/loans

Admin: get all applications

PUT

/admin/loans/{id}/approve

Admin: Approve a loan

GET

/loans/{id}/repayment

View repayment schedule for a specific loan

Setup Instructions
Prerequisites
JDK 17+

Maven 3.6+

PostgreSQL or MySQL

Steps
Clone Repository: git clone https://github.com/your-username/loan_management.git

Database Setup: Create loan_db and configure application.properties (example for PostgreSQL/MySQL provided in the file).

Build Project: mvn clean install

Run Application: mvn spring-boot:run

Access Swagger UI: http://localhost:8080/swagger-ui.html

Test with Postman: Import the provided Postman collection.

Deliverables
Complete codebase with README.md.

REST APIs with Swagger UI.

SQL script for DB schema.

Postman collection for testing.

Evaluation Criteria
Code quality and readability.

Correctness of EMI logic.

Secure authentication and role-based access.

RESTful API design and validation.

Modular architecture.

Testability via Swagger and Postman.
