package com.example.stresspayroll.controller;

import com.example.stresspayroll.model.Employee;
import com.example.stresspayroll.model.Payslip;
import com.example.stresspayroll.model.User;
import com.example.stresspayroll.repository.EmployeeRepository;
import com.example.stresspayroll.repository.PayslipRepository;
import com.example.stresspayroll.repository.UserRepository;
import com.example.stresspayroll.service.PayrollService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employee")
public class EmployeeController {

    @Autowired private UserRepository userRepository;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private PayrollService payrollService;
    @Autowired private PayslipRepository payslipRepository;

    @PostMapping("/payslip")
    public ResponseEntity<?> submitPayslipData(@RequestBody Map<String,Object> body, Authentication auth) {
        String username = auth.getName();
        User u = userRepository.findByUsername(username).orElseThrow();
        Employee emp = employeeRepository.findById(u.getEmployeeId()).orElseThrow();
        int overtime = Integer.parseInt(body.getOrDefault("overtimeHours",0).toString());
        int leaves = Integer.parseInt(body.getOrDefault("leavesTaken",0).toString());
        double attendance = Double.parseDouble(body.getOrDefault("attendancePercent",100).toString());
        String month = (String) body.getOrDefault("monthYear","2025-10");

        Payslip p = payrollService.generateAndSavePayslip(emp, overtime, leaves, attendance, month);
        return ResponseEntity.ok(p);
    }

    @GetMapping("/payslips")
    public ResponseEntity<?> myPayslips(Authentication auth) {
        String username = auth.getName();
        User u = userRepository.findByUsername(username).orElseThrow();
        List<Payslip> list = payslipRepository.findByEmployeeIdOrderByMonthYearDesc(u.getEmployeeId());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/payslip/download/{id}")
    public ResponseEntity<?> downloadPayslip(@PathVariable Long id, Authentication auth) {
        Payslip p = payslipRepository.findById(id).orElseThrow();
        // increment prints saved on employee
        Employee emp = employeeRepository.findById(p.getEmployeeId()).orElseThrow();
        emp.setTotalPrintsSaved(emp.getTotalPrintsSaved() + 1);
        employeeRepository.save(emp);
        // return payslip and paper saved message
        return ResponseEntity.ok(Map.of("payslip", p, "paperSavedPages", emp.getTotalPrintsSaved()));
    }
}
