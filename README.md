Stress-Based Employee Payroll System (minimal runnable demo)

Structure:
- backend/: Spring Boot app (maven). Static frontend files placed in resources/static
- sql/schema.sql: DB schema for MySQL

Setup:
1. Install MySQL and create a user. Update backend/src/main/resources/application.properties with DB credentials.
2. Run SQL schema: mysql -u root -p < sql/schema.sql
3. Build and run backend:
   cd backend
   mvn spring-boot:run
4. Access:
   - Landing page: http://localhost:8080/
   - Signup: http://localhost:8080/signup.html
   - Use HTTP Basic auth for protected endpoints.

Default psychologist number is in application.properties as app.psychologist.contact.

Notes:
- This is a starting demo. For production, secure endpoints, add CSRF protection, HTTPS, input validation, and production-ready auth (JWT/OAuth).
