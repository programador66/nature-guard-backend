package com.natureguard.backend.repository;

import com.natureguard.backend.domain.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
}
