package com.library.management_system.services;

import com.library.management_system.models.PdfJobTracker;
import com.library.management_system.repositories.ReportJobRepository;
import com.library.management_system.services.ReportService; // the interface for report generation
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
public class ReportOrchestrationService {

    private final ReportJobRepository jobRepository;
    private final ApplicationContext applicationContext;
    private final FileStorageService fileStorageService;

    public ReportOrchestrationService(ReportJobRepository jobRepository,
                                      ApplicationContext applicationContext,
                                      FileStorageService fileStorageService) {
        this.jobRepository = jobRepository;
        this.applicationContext = applicationContext;
        this.fileStorageService = fileStorageService;
    }

    // Called by controller to start a report generation
    public PdfJobTracker startReport(String reportType) {
        ReportJob job = new ReportJob();
        job.setReportType(reportType.toUpperCase());
        job.setStatus("PENDING");
        job.setCreatedAt(LocalDateTime.now());
        job = jobRepository.save(job);

        // Trigger async generation
        generateReportAsync(job.getId(), job.getReportType());

        return job; // returns only job metadata (not the PDF)
    }

    // Async method that does the actual work
    @Async
    protected void generateReportAsync(String jobId, String reportType) {
        ReportJob job = jobRepository.findById(jobId).orElseThrow();
        job.setStatus("PROCESSING");
        jobRepository.save(job);

        try {
            // Get the correct report generator bean
            ReportService reportGenerator = getReportGenerator(reportType);
            byte[] pdfBytes = reportGenerator.getReport();

            // Use file storage service to save the PDF
            String filename = jobId + ".pdf";
            String filePath = fileStorageService.storePdf(pdfBytes, filename);

            job.setStatus("COMPLETED");
            job.setFilePath(filePath);
            job.setCompletedAt(LocalDateTime.now());
        } catch (Exception e) {
            job.setStatus("FAILED");
            job.setErrorMessage(e.getMessage());
        }
        jobRepository.save(job);
    }

    private ReportService getReportGenerator(String reportType) {
        // Map report type to Spring bean name (e.g., "revenueReport")
        String beanName = switch (reportType) {
            case "REVENUE" -> "revenueReport";
            case "ACCESS" -> "accessReport";
            default -> throw new IllegalArgumentException("Unknown report type: " + reportType);
        };
        return applicationContext.getBean(beanName, ReportService.class);
    }

    // Retrieve job status
    public Optional<ReportJob> getJob(String jobId) {
        return jobRepository.findById(jobId);
    }

    // Get PDF bytes for a completed job
    public byte[] getReportFile(String jobId) {
        ReportJob job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));

        if (!"COMPLETED".equals(job.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Report not ready");
        }

        // Use file storage service to read the file
        return fileStorageService.getPdf(job.getFilePath()); // you need this method
    }
}