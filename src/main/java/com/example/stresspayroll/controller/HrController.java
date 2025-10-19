package com.example.stresspayroll.controller;

import com.example.stresspayroll.model.Employee;
import com.example.stresspayroll.model.Payslip;
import com.example.stresspayroll.model.StressEntry;
import com.example.stresspayroll.repository.EmployeeRepository;
import com.example.stresspayroll.repository.PayslipRepository;
import com.example.stresspayroll.repository.StressEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hr")
public class HrController {

    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private PayslipRepository payslipRepository;
    @Autowired private StressEntryRepository stressEntryRepository;

    @GetMapping("/employees")
    public List<Employee> allEmployees() {
        return employeeRepository.findAll();
    }

    @GetMapping("/employee/{id}/payslips")
    public List<Payslip> employeePayslips(@PathVariable Long id) {
        return payslipRepository.findByEmployeeIdOrderByMonthYearDesc(id);
    }

    @GetMapping("/employee/{id}/stress")
    public List<StressEntry> employeeStress(@PathVariable Long id) {
        return stressEntryRepository.findByEmployeeIdOrderByMonthYearDesc(id);
    }
}
