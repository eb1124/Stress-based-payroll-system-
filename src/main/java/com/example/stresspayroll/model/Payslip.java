package com.example.stresspayroll.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "payslips")
public class Payslip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String monthYear;
    private double attendancePercent;
    private int overtimeHours;
    private int leavesTaken;
    private double grossSalary;
    private double deductions;
    private double netSalary;
    private LocalDateTime downloadedAt;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee; // <-- use this, not setEmployeeId
}
