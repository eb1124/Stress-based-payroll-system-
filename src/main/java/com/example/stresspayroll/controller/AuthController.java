package com.example.stresspayroll.controller;

import com.example.stresspayroll.model.User;
import com.example.stresspayroll.model.Employee;
import com.example.stresspayroll.repository.UserRepository;
import com.example.stresspayroll.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody Map<String, Object> body) {
        try {
            String email = (String) body.get("email");
            String password = (String) body.get("password");
            String fullName = (String) body.get("fullName");
            String department = (String) body.getOrDefault("department", "General");
            String designation = (String) body.getOrDefault("designation", "Employee");
            String role = (String) body.getOrDefault("role", "EMPLOYEE");
            BigDecimal baseSalary = new BigDecimal(body.getOrDefault("baseSalary", "50000").toString());

            if (userRepository.existsByEmail(email)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email already exists"));
            }

            User user = new User();
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setRole(role);
            user = userRepository.save(user);

            Employee employee = new Employee();
            employee.setUserId(user.getId());
            employee.setFullName(fullName);
            employee.setEmployeeCode("EMP" + System.currentTimeMillis());
            employee.setDepartment(department);
            employee.setDesignation(designation);
            employee.setBaseSalary(baseSalary);
            employee.setPaidLeaves(24);
            employee.setJoinDate(LocalDate.now());
            employee.setIsActive(true);
            employee = employeeRepository.save(employee);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "User created successfully");
            response.put("userId", user.getId());
            response.put("employeeId", employee.getId());
            response.put("role", user.getRole());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, Object> body) {
        try {
            String email = (String) body.get("email");
            String password = (String) body.get("password");

            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid credentials"));
            }

            User user = userOpt.get();
            if (!passwordEncoder.matches(password, user.getPassword())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid credentials"));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Login successful");
            response.put("userId", user.getId());
            response.put("email", user.getEmail());
            response.put("role", user.getRole());

            if ("EMPLOYEE".equals(user.getRole())) {
                Optional<Employee> empOpt = employeeRepository.findByUserId(user.getId());
                empOpt.ifPresent(employee -> {
                    response.put("employeeId", employee.getId());
                    response.put("fullName", employee.getFullName());
                    response.put("employeeCode", employee.getEmployeeCode());
                });
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
