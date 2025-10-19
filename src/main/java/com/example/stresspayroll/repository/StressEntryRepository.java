package com.example.stresspayroll.repository;

import com.example.stresspayroll.model.StressEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StressEntryRepository extends JpaRepository<StressEntry, Long> {
    List<StressEntry> findByEmployeeIdOrderByMonthYearDesc(Long employeeId);
}
