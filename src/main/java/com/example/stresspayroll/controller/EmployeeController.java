package com.example.stresspayroll.controller;

import com.example.stresspayroll.model.Employee;
import com.example.stresspayroll.model.Payslip;
import com.example.stresspayroll.model.StressEntry;
import com.example.stresspayroll.repository.EmployeeRepository;
import com.example.stresspayroll.repository.PayslipRepository;
import com.example.stresspayroll.repository.StressEntryRepository;
import com.example.stresspayroll.service.PayrollService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/employee")
@CrossOrigin(origins = "*")
public class EmployeeController {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PayrollService payrollService;

    @Autowired
    private PayslipRepository payslipRepository;

    @Autowired
    private StressEntryRepository stressEntryRepository;

    @GetMapping("/{employeeId}")
    public ResponseEntity<?> getEmployee(@PathVariable String employeeId) {
        try {
            Employee employee = employeeRepository.findById(UUID.fromString(employeeId))
                .orElseThrow(() -> new RuntimeException("Employee not found"));
            return ResponseEntity.ok(employee);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{employeeId}/generate-payslip")
    public ResponseEntity<?> generatePayslip(
            @PathVariable String employeeId,
            @RequestBody Map<String, Object> body) {
        try {
            UUID empId = UUID.fromString(employeeId);
            Integer month = Integer.parseInt(body.get("month").toString());
            Integer year = Integer.parseInt(body.get("year").toString());
            BigDecimal overtimeHours = new BigDecimal(body.get("overtimeHours").toString());
            Integer workingDays = Integer.parseInt(body.get("workingDays").toString());
            Integer daysPresent = Integer.parseInt(body.get("daysPresent").toString());
            Integer paidLeavesTaken = Integer.parseInt(body.getOrDefault("paidLeavesTaken", 0).toString());
            Integer unpaidLeavesTaken = Integer.parseInt(body.getOrDefault("unpaidLeavesTaken", 0).toString());

            Payslip payslip = payrollService.generatePayslip(
                empId, month, year, overtimeHours, workingDays,
                daysPresent, paidLeavesTaken, unpaidLeavesTaken
            );

            StressEntry stressEntry = stressEntryRepository.findById(payslip.getStressEntryId())
                .orElseThrow(() -> new RuntimeException("Stress entry not found"));

            Map<String, Object> response = new HashMap<>();
            response.put("payslip", payslip);
            response.put("stressEntry", stressEntry);
            response.put("psychologistContact", payrollService.getPsychologistContact());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{employeeId}/payslips")
    public ResponseEntity<?> getPayslips(@PathVariable String employeeId) {
        try {
            UUID empId = UUID.fromString(employeeId);
            List<Payslip> payslips = payslipRepository.findByEmployeeIdOrderByYearDescMonthDesc(empId);
            return ResponseEntity.ok(payslips);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{employeeId}/stress-entries")
    public ResponseEntity<?> getStressEntries(@PathVariable String employeeId) {
        try {
            UUID empId = UUID.fromString(employeeId);
            List<StressEntry> entries = stressEntryRepository.findByEmployeeIdOrderByYearDescMonthDesc(empId);
            return ResponseEntity.ok(entries);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/payslip/{payslipId}/download")
    public ResponseEntity<?> markPayslipDownloaded(@PathVariable String payslipId) {
        try {
            UUID id = UUID.fromString(payslipId);
            Payslip payslip = payslipRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payslip not found"));

            payslip.setDownloadedAt(LocalDateTime.now());
            payslip = payslipRepository.save(payslip);

            Map<String, Object> response = new HashMap<>();
            response.put("payslip", payslip);
            response.put("paperSavedGrams", payslip.getPaperSavedGrams());
            response.put("treesSaved", payslip.getTreesSaved());
            response.put("message", String.format(
                "You've saved %.2f grams of paper and %.6f trees by downloading digitally!",
                payslip.getPaperSavedGrams(),
                payslip.getTreesSaved()
            ));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
