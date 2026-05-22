package com.balu.application_tracking_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationResponseDTO {

    private Long id;
    private Long candidateId;
    private String candidateFullName;
    private String candidateEmail;
    private Long jobId;
    private String jobTitle;
    private String jobDescription;
    private String jobSkills;
    private String status;
    private String coverLetter;
    private String resumeUrl;
    private Integer matchScore;
    private LocalDateTime appliedAt;
}
