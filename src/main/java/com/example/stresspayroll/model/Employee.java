package com.example.stresspayroll.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "employees")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;
    private String email;
    private Double baseSalary;
    private int paidLeaves;
    private int totalPrintsSaved;
}
