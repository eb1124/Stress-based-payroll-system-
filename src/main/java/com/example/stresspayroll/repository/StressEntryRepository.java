package com.example.stresspayroll.repository;

import com.example.stresspayroll.model.StressEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StressEntryRepository extends JpaRepository<StressEntry, UUID> {
    List<StressEntry> findByEmployeeIdOrderByYearDescMonthDesc(UUID employeeId);
    Optional<StressEntry> findByEmployeeIdAndMonthAndYear(UUID employeeId, Integer month, Integer year);
}
