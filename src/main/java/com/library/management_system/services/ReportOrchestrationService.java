package com.library.management_system.services;


import com.library.management_system.models.PdfJobTracker;
import com.library.management_system.models.UserModel;
import com.library.management_system.repositories.ReportJobRepository;
import com.library.management_system.services.ContactService;
import com.library.management_system.services.ReportService; // interface implemented by all report generators
//import com.library.util.SecurityUtils; // your utility to get current user
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ReportOrchestrationService {

    private final ReportJobRepository jobRepository;
    private final ApplicationContext applicationContext;
    private final FileStorageService fileStorageService;
    private final ContactService contactService;

    // Constructor exactly as you specified
    public ReportOrchestrationService(ReportJobRepository jobRepository,
                                      ApplicationContext applicationContext,
                                      FileStorageService fileStorageService,
                                      ContactService contactService) {
        this.jobRepository = jobRepository;
        this.applicationContext = applicationContext;
        this.fileStorageService = fileStorageService;
        this.contactService = contactService;
    }

    // ----- Public methods for each report type -----
    public PdfJobTracker startRevenueReport() {
        UserModel currentUser = contactService.getCurrentAuthenticatedUser(); // get authenticated user
        PdfJobTracker job = createJob(currentUser);
        generateRevenueReportAsync(job.getId());
        return job;
    }

    public PdfJobTracker startAccessReport() {
        UserModel currentUser = contactService.getCurrentAuthenticatedUser();
        PdfJobTracker job = createJob(currentUser);
        generateAccessReportAsync(job.getId());
        return job;
    }

    // ----- Private job creation -----
    private PdfJobTracker createJob(UserModel user) {
        PdfJobTracker job = new PdfJobTracker();
        job.setUser(user);
        job.setStatus("PENDING");
        job.setCreatedAt(LocalDateTime.now());
        return jobRepository.save(job);
    }

    // ----- Separate async methods for each report type -----
    @Async
    protected void generateRevenueReportAsync(Long jobId) {
        ReportService reportGenerator = applicationContext.getBean("general-report", ReportService.class);
        generateReport(jobId, reportGenerator);
    }

    @Async
    protected void generateAccessReportAsync(Long jobId) {
        ReportService reportGenerator = applicationContext.getBean("general-report", ReportService.class);
        generateReport(jobId, reportGenerator);
    }

    // ----- Common report generation logic -----
    // generateReport method
    private void generateReport(Long jobId, ReportService reportGenerator) {
        PdfJobTracker job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));
        job.setStatus("PROCESSING");
        jobRepository.save(job);

        try {
            byte[] pdfBytes = reportGenerator.getReport();
            String filename = "report_" + jobId + ".pdf";

            // This returns a file system path, not storing in DB
            String filePath = fileStorageService.storePdf(pdfBytes, filename);

            job.setStatus("COMPLETED");
            job.setFilePath(filePath);  // Only the path is stored in DB
            job.setCompletedAt(LocalDateTime.now());
        } catch (Exception e) {
            job.setStatus("FAILED");
            job.setErrorMessage(e.getMessage());
        }
        jobRepository.save(job);
    }

    // ----- Public methods for job status and download -----
    public Optional<PdfJobTracker> getJob(Long jobId) {
        return jobRepository.findById(jobId);
    }

    public byte[] getReportFile(Long jobId) {
        PdfJobTracker job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));

        if (!"COMPLETED".equals(job.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Report not ready");
        }

        try {
            // This uses the file path stored in DB to load the actual file
            Resource resource = fileStorageService.loadPdfReport(job.getFilePath());
            return resource.getContentAsByteArray();
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to read report file");
        }
    }
}