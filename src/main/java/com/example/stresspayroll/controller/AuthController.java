package com.example.stresspayroll.controller;

import com.example.stresspayroll.model.User;
import com.example.stresspayroll.model.Employee;
import com.example.stresspayroll.repository.UserRepository;
import com.example.stresspayroll.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody Map<String,Object> body) {
        String username = (String) body.get("username");
        String password = (String) body.get("password");
        String fullName = (String) body.get("fullName");
        String email = (String) body.get("email");
        Double baseSalary = Double.valueOf(body.getOrDefault("baseSalary", 30000).toString());

        if(userRepository.existsByUsername(username)) {
            return ResponseEntity.badRequest().body(Map.of("error","username exists"));
        }
        Employee emp = new Employee();
        emp.setFullName(fullName);
        emp.setEmail(email);
        emp.setBaseSalary(baseSalary);
        emp = employeeRepository.save(emp);

        User u = new User();
        u.setUsername(username);
        u.setPassword(passwordEncoder.encode(password));
        u.setRole("EMPLOYEE");
        u.setEmployeeId(emp.getId());
        userRepository.save(u);

        return ResponseEntity.ok(Map.of("message","created","employeeId", emp.getId()));
    }
}
