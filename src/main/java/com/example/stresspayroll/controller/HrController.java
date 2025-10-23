package com.example.stresspayroll.controller;

import com.example.stresspayroll.model.Employee;
import com.example.stresspayroll.model.Payslip;
import com.example.stresspayroll.model.StressEntry;
import com.example.stresspayroll.repository.EmployeeRepository;
import com.example.stresspayroll.repository.PayslipRepository;
import com.example.stresspayroll.repository.StressEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/hr")
@CrossOrigin(origins = "*")
public class HrController {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PayslipRepository payslipRepository;

    @Autowired
    private StressEntryRepository stressEntryRepository;

    @GetMapping("/employees")
    public ResponseEntity<?> getAllEmployees() {
        try {
            List<Employee> employees = employeeRepository.findAll();
            return ResponseEntity.ok(employees);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<?> getEmployee(@PathVariable String employeeId) {
        try {
            Employee employee = employeeRepository.findById(UUID.fromString(employeeId))
                .orElseThrow(() -> new RuntimeException("Employee not found"));
            return ResponseEntity.ok(employee);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/employee/{employeeId}/payslips")
    public ResponseEntity<?> getEmployeePayslips(@PathVariable String employeeId) {
        try {
            UUID empId = UUID.fromString(employeeId);
            List<Payslip> payslips = payslipRepository.findByEmployeeIdOrderByYearDescMonthDesc(empId);
            return ResponseEntity.ok(payslips);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/employee/{employeeId}/stress-entries")
    public ResponseEntity<?> getEmployeeStressEntries(@PathVariable String employeeId) {
        try {
            UUID empId = UUID.fromString(employeeId);
            List<StressEntry> entries = stressEntryRepository.findByEmployeeIdOrderByYearDescMonthDesc(empId);
            return ResponseEntity.ok(entries);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/stress-analytics")
    public ResponseEntity<?> getStressAnalytics() {
        try {
            List<StressEntry> allEntries = stressEntryRepository.findAll();
            long criticalCount = allEntries.stream()
                .filter(e -> "CRITICAL".equals(e.getStressCategory()))
                .count();
            long highCount = allEntries.stream()
                .filter(e -> "HIGH".equals(e.getStressCategory()))
                .count();

            return ResponseEntity.ok(Map.of(
                "totalEntries", allEntries.size(),
                "criticalStress", criticalCount,
                "highStress", highCount,
                "requiresCounseling", allEntries.stream().filter(StressEntry::getRequiresCounseling).count()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
