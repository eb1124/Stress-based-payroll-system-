package com.example.stresspayroll.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "stress_entries")
public class StressEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String monthYear;
    private double stressScore;
    private String reason;
    private String suggestion;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee; // <-- use this, not setEmployeeId
}
