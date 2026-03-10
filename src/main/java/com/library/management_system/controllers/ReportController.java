package com.library.management_system.controllers;

import com.library.entity.ReportJob;
import com.library.service.ReportOrchestrationService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportOrchestrationService reportService;

    public ReportController(ReportOrchestrationService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/{reportType}")
    public ResponseEntity<ReportJob> generateReport(@PathVariable String reportType) {
        ReportJob job = reportService.startReport(reportType);
        return ResponseEntity.accepted().body(job);
    }

    @GetMapping("/job/{jobId}")
    public ResponseEntity<ReportJob> getJobStatus(@PathVariable String jobId) {
        return reportService.getJob(jobId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/download/{jobId}")
    public ResponseEntity<byte[]> downloadReport(@PathVariable String jobId) {
        byte[] pdfContent = reportService.getReportFile(jobId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "report.pdf");
        return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);
    }
}
