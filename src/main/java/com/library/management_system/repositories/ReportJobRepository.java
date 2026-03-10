package com.library.management_system.repositories;

import com.library.management_system.models.PdfJobTracker;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportJobRepository extends JpaRepository<PdfJobTracker, String > {
}
