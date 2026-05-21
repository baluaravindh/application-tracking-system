package com.balu.application_tracking_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InterviewNotificationDTO {

    private Long interviewId;
    private Long applicationId;
    private String candidateName;
    private String candidateEmail;
    private String jobTitle;
    private LocalDateTime scheduledAt;
    private String meetingLink;
    private String venue;
    private String status;
}
