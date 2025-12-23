package com.developer.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.developer.dto.response.ResumeDTO;
import com.developer.service.PdfGeneratorService;
import com.developer.service.ResumeService;

@RestController
@RequestMapping("/api/resume")
@Validated
public class ResumeController {

    private final ResumeService resumeService;
    private final PdfGeneratorService pdfGeneratorService;

    public ResumeController(ResumeService resumeService, PdfGeneratorService pdfGeneratorService) {
        this.resumeService = resumeService;
        this.pdfGeneratorService = pdfGeneratorService;
    }

    /**
     * Returns a structured JSON representation of the user's resume for preview.
     */
    @GetMapping("/preview")
    public ResponseEntity<ResumeDTO> getResumePreview() {
        ResumeDTO resumeDTO = resumeService.buildResumeForCurrentUser();
        return ResponseEntity.ok(resumeDTO);
    }

    /**
     * Streams a generated PDF resume as an attachment.
     */
    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadResume() {
        ResumeDTO resumeDTO = resumeService.buildResumeForCurrentUser();
        byte[] pdfBytes = pdfGeneratorService.generateResumePdf(resumeDTO);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "resume.pdf");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}


