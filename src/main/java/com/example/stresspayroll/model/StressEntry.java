package com.example.stresspayroll.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "stress_entries", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"employee_id", "month", "year"})
})
public class StressEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(nullable = false)
    private Integer month;

    @Column(nullable = false)
    private Integer year;

    @Column(name = "overtime_hours", nullable = false)
    private BigDecimal overtimeHours;

    @Column(name = "working_days", nullable = false)
    private Integer workingDays;

    @Column(name = "days_present", nullable = false)
    private Integer daysPresent;

    @Column(name = "paid_leaves_taken")
    private Integer paidLeavesTaken = 0;

    @Column(name = "unpaid_leaves_taken")
    private Integer unpaidLeavesTaken = 0;

    @Column(name = "stress_level", nullable = false)
    private BigDecimal stressLevel;

    @Column(name = "stress_category", nullable = false)
    private String stressCategory;

    @Column(name = "stress_factors", columnDefinition = "TEXT")
    private String stressFactors;

    @Column(columnDefinition = "TEXT")
    private String recommendations;

    @Column(name = "requires_counseling")
    private Boolean requiresCounseling = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
