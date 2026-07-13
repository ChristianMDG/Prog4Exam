package com.exam.app.service.event;

import com.exam.app.endpoint.event.model.SubmissionCreatedEvent;
import com.exam.app.file.bucket.BucketComponent;
import com.exam.app.mail.Email;
import com.exam.app.mail.Mailer;
import com.exam.app.submission.entity.Submission;
import com.exam.app.submission.entity.SubmissionStatus;
import com.exam.app.submission.repository.SubmissionRepository;
import com.exam.app.submission.service.GrayscaleImageService;
import jakarta.mail.internet.InternetAddress;
import java.io.File;
import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SubmissionCreatedEventService implements Consumer<SubmissionCreatedEvent> {

  private final GrayscaleImageService grayscaleImageService;
  private final BucketComponent bucketComponent;
  private final Mailer mailer;
  private final SubmissionRepository submissionRepository;

  @SneakyThrows
  @Override
  public void accept(SubmissionCreatedEvent event) {

    // 1. Télécharge l'original
    File originalFile = bucketComponent.download(event.getOriginalBucketKey());

    // 2. Convertit en noir et blanc
    String format = extractFormat(event.getOriginalFileName());
    File processedFile = grayscaleImageService.convertToGrayscale(originalFile, format);

    // 3. Upload la version traitée
    String processedKey = "processed/" + event.getSubmissionId() + "." + format;
    bucketComponent.upload(processedFile, processedKey);

    // 4. URL présignée
    String downloadUrl = bucketComponent.presign(processedKey, Duration.ofDays(7)).toString();

    // 5. Email de confirmation
    mailer.accept(
        new Email(
            new InternetAddress(event.getEmail()),
            List.of(),
            List.of(),
            "Votre image a été traitée",
            "Bonjour,\n\nVotre image \""
                + event.getOriginalFileName()
                + "\" a été convertie en noir et blanc.\n\nTéléchargez-la ici : "
                + downloadUrl
                + "\n(Lien valable 7 jours)",
            List.of()));

    // 6. Mise à jour du statut
    Submission submission =
        submissionRepository
            .findById(event.getSubmissionId())
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "Submission introuvable: " + event.getSubmissionId()));
    submission.setStatus(SubmissionStatus.CONFIRMED);
    submission.setProcessedImageUrl(downloadUrl);
    submissionRepository.save(submission);
  }

  private String extractFormat(String fileName) {
    String lower = fileName.toLowerCase();
    if (lower.endsWith(".png")) return "png";
    if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "jpg";
    return "png"; // fallback raisonnable
  }
}
