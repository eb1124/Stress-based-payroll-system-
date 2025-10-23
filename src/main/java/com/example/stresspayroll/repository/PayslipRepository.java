package com.example.stresspayroll.repository;

import com.example.stresspayroll.model.Payslip;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PayslipRepository extends JpaRepository<Payslip, UUID> {
    List<Payslip> findByEmployeeIdOrderByYearDescMonthDesc(UUID employeeId);
    Optional<Payslip> findByEmployeeIdAndMonthAndYear(UUID employeeId, Integer month, Integer year);
}
