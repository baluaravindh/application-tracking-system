package com.balu.application_tracking_system.controller;

import com.balu.application_tracking_system.dto.JobRequestDTO;
import com.balu.application_tracking_system.dto.JobResponseDTO;
import com.balu.application_tracking_system.dto.PagedResponseDTO;
import com.balu.application_tracking_system.service.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Job", description = "Job Posting APIs")
@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    //  POST   /api/jobs    → HR posts a job
    @Operation(summary = "HR post a Job.")
    @PostMapping
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<JobResponseDTO> postJob(@Valid @RequestBody JobRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(jobService.postJob(dto));
    }

    //  GET    /api/jobs    → All users view open jobs (paginated)
//    @Operation(summary = "Get all jobs.")
//    @GetMapping
//    public ResponseEntity<List<JobResponseDTO>> getAllJobs(){
//        return ResponseEntity.ok(jobService.getAllJobs());
//    }

    // GET /api/jobs/{id}
    @GetMapping("/{id}")
    public ResponseEntity<JobResponseDTO> getJobById(@PathVariable Long id) {
        return ResponseEntity.ok(jobService.getJobById(id));
    }

    // PUT /api/jobs/{id}
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<JobResponseDTO> updateJob(
            @PathVariable Long id,
            @Valid @RequestBody JobRequestDTO dto) {
        return ResponseEntity.ok(jobService.updateJob(id, dto));
    }

    // PATCH /api/jobs/{id}/status
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('HR') or hasRole('ADMIN')")
    public ResponseEntity<JobResponseDTO> updateJobStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return ResponseEntity.ok(jobService.updateJobStatus(email, id, status));
    }

    // DELETE /api/jobs/{id}
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteJob(@PathVariable Long id) {
        jobService.deleteJob(id);
        return ResponseEntity.ok("Job deleted successfully");
    }

    // Update controller method
    @Operation(summary = "Get all jobs.")
    @GetMapping
    public ResponseEntity<PagedResponseDTO<JobResponseDTO>> getAllJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        return ResponseEntity.ok(jobService.getAllJobs(page, size, sortBy, direction));
    }
}
