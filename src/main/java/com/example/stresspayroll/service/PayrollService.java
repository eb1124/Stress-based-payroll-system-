package com.example.stresspayroll.service;

import com.example.stresspayroll.model.Employee;
import com.example.stresspayroll.model.Payslip;
import com.example.stresspayroll.model.StressEntry;
import com.example.stresspayroll.repository.EmployeeRepository;
import com.example.stresspayroll.repository.PayslipRepository;
import com.example.stresspayroll.repository.StressEntryRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PayrollService {

    @Autowired
    private PayslipRepository payslipRepository;

    @Autowired
    private StressEntryRepository stressEntryRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    public Payslip generateAndSavePayslip(Employee emp, int overtimeHours, int leavesTaken, double attendancePercent, String monthYear) {

        // Calculate gross, deductions, and net salary
        double grossSalary = emp.getBaseSalary() + (overtimeHours * 200); // example calculation
        double deductions = leavesTaken * (emp.getBaseSalary() / 30); // deduction per leave
        double netSalary = grossSalary - deductions;

        // Create Payslip
        Payslip p = new Payslip();
        p.setEmployee(emp); // ✅ set the entire Employee object instead of ID
        p.setMonthYear(monthYear);
        p.setAttendancePercent(attendancePercent);
        p.setOvertimeHours(overtimeHours);
        p.setLeavesTaken(leavesTaken);
        p.setGrossSalary(grossSalary);
        p.setDeductions(deductions);
        p.setNetSalary(netSalary);
        p.setDownloadedAt(LocalDateTime.now());

        payslipRepository.save(p);

        // Create StressEntry
        double stressScore = leavesTaken + (overtimeHours / 2.0); // example stress formula
        StressEntry s = new StressEntry();
        s.setEmployee(emp); // ✅ set the entire Employee object
        s.setMonthYear(monthYear);
        s.setStressScore(stressScore);
        s.setReason("Auto-calculated");
        s.setSuggestion("Take rest if stress is high");

        stressEntryRepository.save(s);

        // Update employee's paid leaves if needed
        emp.setPaidLeaves(emp.getPaidLeaves() + leavesTaken);
        employeeRepository.save(emp);

        return p;
    }
}
