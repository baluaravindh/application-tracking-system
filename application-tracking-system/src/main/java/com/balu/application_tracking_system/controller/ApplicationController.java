package com.balu.application_tracking_system.controller;

import com.balu.application_tracking_system.dto.ApplicationRequestDTO;
import com.balu.application_tracking_system.dto.ApplicationResponseDTO;
import com.balu.application_tracking_system.service.ApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Applications", description = "Job Application by Candidate APIs")
@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    //  POST   /api/applications/{jobId}        → Candidate applies
    @Operation(summary = "Candidate will apply for job.")
    @PostMapping("/{jobId}")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ApplicationResponseDTO> applyForJob(@PathVariable Long jobId, @Valid @RequestBody ApplicationRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(applicationService.applyForJob(jobId, dto));
    }

    //  GET    /api/applications/my             → Candidate views own applications
    @Operation(summary = "Candidate views own applications.")
    @GetMapping("/my")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<List<ApplicationResponseDTO>> getMyApplications() {
        return ResponseEntity.ok(applicationService.getMyApplications());
    }

    //  GET    /api/applications/job/{jobId}    → HR views all applications for a job
    @Operation(summary = "HR views all applications for a job.")
    @GetMapping("/job/{jobId}")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<List<ApplicationResponseDTO>> getApplicationsByJob(@PathVariable Long jobId) {
        return ResponseEntity.ok(applicationService.getApplicationsByJob(jobId));
    }

    //  PATCH  /api/applications/{id}/status   → HR updates application status
    @Operation(summary = "HR updates application status.")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<ApplicationResponseDTO> updateApplicationStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(applicationService.updateApplicationStatus(id, status));
    }

    //  DELETE /api/applications/{id}          → Candidate withdraws application
    @Operation(summary = "Candidate withdraws application.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ApplicationResponseDTO> withdrawApplication(@PathVariable Long id) {
        return ResponseEntity.ok(applicationService.withdrawApplication(id));
    }
}
