package com.balu.application_tracking_system.controller;

import com.balu.application_tracking_system.dto.InterviewRequestDTO;
import com.balu.application_tracking_system.dto.InterviewResponseDTO;
import com.balu.application_tracking_system.service.InterviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Interviews", description = "Interview scheduled processing by HR APIs")
@RestController
@RequestMapping("/api/interviews")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;

    //  POST   /api/interviews/{applicationId}     → HR schedules interview
    @Operation(summary = "HR will schedule the interview.")
    @PostMapping("/{applicationId}")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<InterviewResponseDTO> scheduleInterview(
            @PathVariable Long applicationId,
            @Valid @RequestBody InterviewRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).
                body(interviewService.scheduleInterview(applicationId, dto));
    }

    //  GET    /api/interviews/{applicationId}     → View interview for application
    @Operation(summary = "HR will view interview for application.")
    @GetMapping("/{applicationId}")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<InterviewResponseDTO> getInterviewByApplication(@PathVariable Long applicationId) {
        return ResponseEntity.ok(interviewService.getInterviewByApplication(applicationId));
    }

    //  PATCH  /api/interviews/{id}/reschedule    → HR reschedules
    @Operation(summary = "HR will reschedule the interview.")
    @PatchMapping("/{interviewId}/reschedule")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<InterviewResponseDTO> rescheduleInterview(
            @PathVariable Long interviewId,
            @Valid @RequestBody InterviewRequestDTO dto) {
        return ResponseEntity.ok(interviewService.rescheduleInterview(interviewId, dto));
    }

    //  PATCH  /api/interviews/{id}/status        → HR updates status
    @Operation(summary = "HR will updates the status.")
    @PatchMapping("/{interviewId}/status")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<InterviewResponseDTO> updateInterviewStatus(
            @PathVariable Long interviewId,
            @RequestParam String status) {
        return ResponseEntity.ok(interviewService.updateInterviewStatus(interviewId, status));
    }

    //  DELETE /api/interviews/{id}               → HR cancels interview
    @Operation(summary = "HR will cancels the interview.")
    @DeleteMapping("/{interviewId}")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<InterviewResponseDTO> cancelInterview(@PathVariable Long interviewId) {
        return ResponseEntity.ok(interviewService.cancelInterview(interviewId));
    }
}
