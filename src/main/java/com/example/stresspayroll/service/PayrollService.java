package com.example.stresspayroll.service;

import com.example.stresspayroll.model.Employee;
import com.example.stresspayroll.model.Payslip;
import com.example.stresspayroll.model.StressEntry;
import com.example.stresspayroll.repository.EmployeeRepository;
import com.example.stresspayroll.repository.PayslipRepository;
import com.example.stresspayroll.repository.StressEntryRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
public class PayrollService {

    @Autowired
    private PayslipRepository payslipRepository;

    @Autowired
    private StressEntryRepository stressEntryRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Value("${app.psychologist.contact}")
    private String psychologistContact;

    @Transactional
    public Payslip generatePayslip(UUID employeeId, Integer month, Integer year,
                                   BigDecimal overtimeHours, Integer workingDays,
                                   Integer daysPresent, Integer paidLeavesTaken,
                                   Integer unpaidLeavesTaken) {

        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new RuntimeException("Employee not found"));

        BigDecimal stressLevel = calculateStressLevel(overtimeHours, workingDays, daysPresent);
        String stressCategory = categorizeStress(stressLevel);
        String stressFactors = analyzeStressFactors(overtimeHours, unpaidLeavesTaken);
        String recommendations = generateRecommendations(stressCategory, overtimeHours);
        boolean requiresCounseling = stressLevel.compareTo(new BigDecimal("80")) >= 0;

        StressEntry stressEntry = new StressEntry();
        stressEntry.setEmployeeId(employeeId);
        stressEntry.setMonth(month);
        stressEntry.setYear(year);
        stressEntry.setOvertimeHours(overtimeHours);
        stressEntry.setWorkingDays(workingDays);
        stressEntry.setDaysPresent(daysPresent);
        stressEntry.setPaidLeavesTaken(paidLeavesTaken);
        stressEntry.setUnpaidLeavesTaken(unpaidLeavesTaken);
        stressEntry.setStressLevel(stressLevel);
        stressEntry.setStressCategory(stressCategory);
        stressEntry.setStressFactors(stressFactors);
        stressEntry.setRecommendations(recommendations);
        stressEntry.setRequiresCounseling(requiresCounseling);

        stressEntry = stressEntryRepository.save(stressEntry);

        BigDecimal overtimePay = overtimeHours.multiply(new BigDecimal("200"));
        BigDecimal perDaySalary = employee.getBaseSalary().divide(new BigDecimal(workingDays), 2, RoundingMode.HALF_UP);
        BigDecimal unpaidLeaveDeduction = perDaySalary.multiply(new BigDecimal(unpaidLeavesTaken));

        BigDecimal netSalary = employee.getBaseSalary()
            .add(overtimePay)
            .subtract(unpaidLeaveDeduction);

        BigDecimal paperSavedGrams = new BigDecimal("4.5");
        BigDecimal treesSaved = paperSavedGrams.divide(new BigDecimal("8333.33"), 6, RoundingMode.HALF_UP);

        Payslip payslip = new Payslip();
        payslip.setEmployeeId(employeeId);
        payslip.setStressEntryId(stressEntry.getId());
        payslip.setMonth(month);
        payslip.setYear(year);
        payslip.setBaseSalary(employee.getBaseSalary());
        payslip.setOvertimePay(overtimePay);
        payslip.setDeductions(unpaidLeaveDeduction);
        payslip.setNetSalary(netSalary);
        payslip.setPaperSavedGrams(paperSavedGrams);
        payslip.setTreesSaved(treesSaved);

        return payslipRepository.save(payslip);
    }

    private BigDecimal calculateStressLevel(BigDecimal overtimeHours, Integer workingDays, Integer daysPresent) {
        BigDecimal baseStress = new BigDecimal("20");

        BigDecimal overtimeStress = overtimeHours.multiply(new BigDecimal("2.5"));

        BigDecimal attendanceRate = new BigDecimal(daysPresent)
            .divide(new BigDecimal(workingDays), 2, RoundingMode.HALF_UP);
        BigDecimal attendanceStress = BigDecimal.ONE.subtract(attendanceRate).multiply(new BigDecimal("30"));

        BigDecimal totalStress = baseStress.add(overtimeStress).add(attendanceStress);

        if (totalStress.compareTo(new BigDecimal("100")) > 0) {
            return new BigDecimal("100");
        }
        return totalStress.setScale(2, RoundingMode.HALF_UP);
    }

    private String categorizeStress(BigDecimal stressLevel) {
        if (stressLevel.compareTo(new BigDecimal("30")) < 0) {
            return "LOW";
        } else if (stressLevel.compareTo(new BigDecimal("60")) < 0) {
            return "MODERATE";
        } else if (stressLevel.compareTo(new BigDecimal("80")) < 0) {
            return "HIGH";
        } else {
            return "CRITICAL";
        }
    }

    private String analyzeStressFactors(BigDecimal overtimeHours, Integer unpaidLeaves) {
        StringBuilder factors = new StringBuilder();

        if (overtimeHours.compareTo(new BigDecimal("20")) > 0) {
            factors.append("Excessive overtime hours (").append(overtimeHours).append(" hours). ");
        } else if (overtimeHours.compareTo(new BigDecimal("10")) > 0) {
            factors.append("High overtime hours (").append(overtimeHours).append(" hours). ");
        }

        if (unpaidLeaves > 5) {
            factors.append("Excessive unpaid leaves taken (").append(unpaidLeaves).append(" days). ");
        } else if (unpaidLeaves > 2) {
            factors.append("Multiple unpaid leaves (").append(unpaidLeaves).append(" days). ");
        }

        if (factors.length() == 0) {
            factors.append("Normal workload within acceptable limits.");
        }

        return factors.toString();
    }

    private String generateRecommendations(String category, BigDecimal overtimeHours) {
        StringBuilder recommendations = new StringBuilder();

        switch (category) {
            case "LOW":
                recommendations.append("Your stress level is healthy. Keep maintaining a good work-life balance.");
                break;
            case "MODERATE":
                recommendations.append("Consider taking regular breaks and practice stress management techniques. ");
                recommendations.append("Try to limit overtime work when possible.");
                break;
            case "HIGH":
                recommendations.append("Your stress level is concerning. Please prioritize self-care and rest. ");
                recommendations.append("Speak with your manager about workload distribution. ");
                recommendations.append("Consider using wellness resources available to you.");
                break;
            case "CRITICAL":
                recommendations.append("URGENT: Your stress level is critically high. ");
                recommendations.append("You are required to schedule an appointment with our office psychologist. ");
                recommendations.append("Contact: ").append(psychologistContact).append(". ");
                recommendations.append("Please also discuss workload concerns with HR immediately.");
                break;
        }

        if (overtimeHours.compareTo(new BigDecimal("15")) > 0) {
            recommendations.append(" Try to reduce overtime hours by delegating tasks or requesting additional support.");
        }

        return recommendations.toString();
    }

    public String getPsychologistContact() {
        return psychologistContact;
    }
}
