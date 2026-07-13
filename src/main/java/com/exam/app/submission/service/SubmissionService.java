package com.exam.app.submission.service;

import com.exam.app.endpoint.event.model.SubmissionCreatedEvent;
import com.exam.app.file.bucket.BucketComponent;
import com.exam.app.submission.entity.Submission;
import com.exam.app.submission.entity.SubmissionStatus;
import com.exam.app.submission.exception.EmptyFileException;
import com.exam.app.submission.exception.InvalidFileTypeException;
import com.exam.app.submission.repository.SubmissionRepository;
import java.io.File;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@AllArgsConstructor
public class SubmissionService {

  private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/png", "image/jpeg");

  private final SubmissionRepository submissionRepository;
  private final BucketComponent bucketComponent;

  @Transactional
  @SneakyThrows
  public SubmissionCreationResult submit(String email, MultipartFile image) {

    if (image == null || image.isEmpty()) {
      throw new EmptyFileException("Le fichier image est vide ou absent");
    }
    if (!ALLOWED_CONTENT_TYPES.contains(image.getContentType())) {
      throw new InvalidFileTypeException(
          "Type de fichier non supporté (" + image.getContentType() + "), PNG ou JPG uniquement");
    }

    Submission submission = new Submission();
    submission.setFileName(image.getOriginalFilename());
    submission.setEmail(email);
    submission.setStatus(SubmissionStatus.PENDING);
    submission.setSubmissionDate(Instant.now());
    submission = submissionRepository.save(submission);

    // Upload synchrone de l'original (l'event ne doit pas transporter les octets de l'image)
    File tempFile = File.createTempFile("original-", "-" + image.getOriginalFilename());
    image.transferTo(tempFile);
    String originalKey = "originals/" + submission.getId() + "-" + image.getOriginalFilename();
    bucketComponent.upload(tempFile, originalKey);

    SubmissionCreatedEvent event =
        SubmissionCreatedEvent.builder()
            .submissionId(submission.getId())
            .email(email)
            .originalFileName(image.getOriginalFilename())
            .originalBucketKey(originalKey)
            .build();

    return new SubmissionCreationResult(submission, event);
  }

  public List<Submission> getAll() {
    return submissionRepository.findAll();
  }
}
