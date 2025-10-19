package com.example.stresspayroll.repository;

import com.example.stresspayroll.model.Payslip;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PayslipRepository extends JpaRepository<Payslip, Long> {
    List<Payslip> findByEmployeeIdOrderByMonthYearDesc(Long employeeId);
}
