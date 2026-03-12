package com.library.management_system.controllers;



import com.library.management_system.models.PdfJobTracker;
import com.library.management_system.services.ReportOrchestrationService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportOrchestrationService reportService;

    public ReportController(ReportOrchestrationService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/general-report")
    public ResponseEntity<PdfJobTracker> generateRevenueReport() {
        PdfJobTracker job = reportService.startRevenueReport();
        return ResponseEntity.accepted().body(job);
    }



    @GetMapping("/job/{jobId}")
    public ResponseEntity<PdfJobTracker> getJobStatus(@PathVariable Long jobId) {
        return reportService.getJob(jobId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/download/{jobId}")
    public ResponseEntity<byte[]> downloadReport(@PathVariable Long jobId) {
        byte[] pdfContent = reportService.getReportFile(jobId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "report.pdf");
        return new ResponseEntity<>(pdfContent, HttpStatus.OK);
    }
}