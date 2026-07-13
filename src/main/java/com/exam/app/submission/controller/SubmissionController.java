package com.exam.app.submission.controller;

import com.exam.app.endpoint.event.EventProducer;
import com.exam.app.endpoint.event.model.SubmissionCreatedEvent;
import com.exam.app.submission.dto.SubmissionResponse;
import com.exam.app.submission.dto.SubmitRequest;
import com.exam.app.submission.entity.Submission;
import com.exam.app.submission.service.SubmissionCreationResult;
import com.exam.app.submission.service.SubmissionService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/submission")
@AllArgsConstructor
public class SubmissionController {

  private final SubmissionService submissionService;
  private final EventProducer<SubmissionCreatedEvent> eventProducer;

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<SubmissionResponse> submit(@Valid @RequestBody SubmitRequest request) {

    SubmissionCreationResult result =
        submissionService.submit(
            request.getEmail(), request.getFileName(), request.getImageBase64());
    Submission submission = result.submission();

    eventProducer.accept(List.of(result.event()));

    return ResponseEntity.ok(toResponse(submission));
  }

  @GetMapping
  public List<SubmissionResponse> getAll() {
    return submissionService.getAll().stream().map(this::toResponse).toList();
  }

  private SubmissionResponse toResponse(Submission submission) {
    return SubmissionResponse.builder()
        .id(submission.getId())
        .fileName(submission.getFileName())
        .email(submission.getEmail())
        .status(submission.getStatus())
        .processedImageUrl(submission.getProcessedImageUrl())
        .submissionDate(submission.getSubmissionDate())
        .build();
  }
}
