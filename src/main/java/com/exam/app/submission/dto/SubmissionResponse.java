package com.exam.app.submission.dto;

import com.exam.app.submission.entity.SubmissionStatus;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SubmissionResponse {
  private UUID id;
  private String fileName;
  private String email;
  private SubmissionStatus status;
  private String processedImageUrl;
  private Instant submissionDate;
}
