package com.exam.app.submission.service;

import com.exam.app.endpoint.event.model.SubmissionCreatedEvent;
import com.exam.app.file.bucket.BucketComponent;
import com.exam.app.submission.entity.Submission;
import com.exam.app.submission.entity.SubmissionStatus;
import com.exam.app.submission.exception.EmptyFileException;
import com.exam.app.submission.exception.InvalidFileTypeException;
import com.exam.app.submission.repository.SubmissionRepository;
import java.io.File;
import java.nio.file.Files;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class SubmissionService {

  private final SubmissionRepository submissionRepository;
  private final BucketComponent bucketComponent;

  @Transactional
  @SneakyThrows
  public SubmissionCreationResult submit(String email, String fileName, String imageBase64) {

    byte[] imageBytes = Base64.getDecoder().decode(imageBase64);
    if (imageBytes.length == 0) {
      throw new EmptyFileException("Le fichier image est vide");
    }

    String lowerFileName = fileName.toLowerCase();
    if (!lowerFileName.endsWith(".png")
        && !lowerFileName.endsWith(".jpg")
        && !lowerFileName.endsWith(".jpeg")) {
      throw new InvalidFileTypeException(
          "Type de fichier non supporté (" + fileName + "), PNG ou JPG uniquement");
    }

    Submission submission = new Submission();
    submission.setFileName(fileName);
    submission.setEmail(email);
    submission.setStatus(SubmissionStatus.PENDING);
    submission.setSubmissionDate(Instant.now());
    submission = submissionRepository.save(submission);

    File tempFile = File.createTempFile("original-", "-" + fileName);
    Files.write(tempFile.toPath(), imageBytes);
    String originalKey = "originals/" + submission.getId() + "-" + fileName;
    bucketComponent.upload(tempFile, originalKey);

    SubmissionCreatedEvent event =
        SubmissionCreatedEvent.builder()
            .submissionId(submission.getId())
            .email(email)
            .originalFileName(fileName)
            .originalBucketKey(originalKey)
            .build();

    return new SubmissionCreationResult(submission, event);
  }

  public List<Submission> getAll() {
    return submissionRepository.findAll();
  }
}
