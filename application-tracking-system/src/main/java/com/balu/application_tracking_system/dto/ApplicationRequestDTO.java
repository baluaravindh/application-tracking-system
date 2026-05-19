package com.balu.application_tracking_system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ApplicationRequestDTO {

//    @NotNull(message = "Candidate Id is required.")
//    private Long candidateId;
//
//    @NotNull(message = "Job Id is required.")
//    private Long jobId;

//    @NotBlank(message = "Application status is required.")
//    private String status;

    @NotBlank(message = "Cover letter is required.")
    private String coverLetter;
}
