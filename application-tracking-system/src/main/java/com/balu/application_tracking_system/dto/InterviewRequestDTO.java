package com.balu.application_tracking_system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InterviewRequestDTO {

//    @NotNull(message = "Application Id is required.")
//    private Long applicationId;

    @NotNull(message = "Scheduled date and time is required.")
    private LocalDateTime scheduledAt;

    @NotBlank(message = "Meeting link is required.")
    private String meetingLink;

    @NotBlank(message = "Venue is required.")
    private String venue;

    @NotNull(message = "Minutes duration is required.")
    private Integer durationMinutes;

    @NotBlank(message = "Notes is required.")
    private String notes;

//    @NotBlank(message = "Status is required.")
//    private String status;
}
