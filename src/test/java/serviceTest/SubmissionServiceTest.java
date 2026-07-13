package serviceTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.exam.app.endpoint.event.model.SubmissionCreatedEvent;
import com.exam.app.file.bucket.BucketComponent;
import com.exam.app.submission.entity.Submission;
import com.exam.app.submission.entity.SubmissionStatus;
import com.exam.app.submission.exception.EmptyFileException;
import com.exam.app.submission.exception.InvalidFileTypeException;
import com.exam.app.submission.repository.SubmissionRepository;
import com.exam.app.submission.service.SubmissionCreationResult;
import com.exam.app.submission.service.SubmissionService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;

class SubmissionServiceTest {

  @Mock private SubmissionRepository submissionRepository;

  @Mock private BucketComponent bucketComponent;

  private SubmissionService submissionService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    submissionService = new SubmissionService(submissionRepository, bucketComponent);
  }

  @Test
  void submit_shouldCreatePendingSubmission_whenImageIsValidPng() {
    // given
    MockMultipartFile image =
        new MockMultipartFile("image", "photo.png", "image/png", "fake-png-bytes".getBytes());

    when(submissionRepository.save(any(Submission.class)))
        .thenAnswer(
            invocation -> {
              Submission s = invocation.getArgument(0);
              s.setId(UUID.randomUUID());
              return s;
            });

    // when
    SubmissionCreationResult result = submissionService.submit("test@example.com", image);

    // then
    assertThat(result.submission().getStatus()).isEqualTo(SubmissionStatus.PENDING);
    assertThat(result.submission().getEmail()).isEqualTo("test@example.com");
    assertThat(result.submission().getFileName()).isEqualTo("photo.png");

    SubmissionCreatedEvent event = result.event();
    assertThat(event.getSubmissionId()).isEqualTo(result.submission().getId());
    assertThat(event.getEmail()).isEqualTo("test@example.com");
    assertThat(event.getOriginalFileName()).isEqualTo("photo.png");
    assertThat(event.getOriginalBucketKey()).contains(result.submission().getId().toString());

    verify(bucketComponent, times(1)).upload(any(), anyString());
    verify(submissionRepository, times(1)).save(any(Submission.class));
  }

  @Test
  void submit_shouldAcceptJpegContentType() {
    MockMultipartFile image =
        new MockMultipartFile("image", "photo.jpg", "image/jpeg", "fake-jpg-bytes".getBytes());

    when(submissionRepository.save(any(Submission.class)))
        .thenAnswer(
            invocation -> {
              Submission s = invocation.getArgument(0);
              s.setId(UUID.randomUUID());
              return s;
            });

    SubmissionCreationResult result = submissionService.submit("test@example.com", image);

    assertThat(result.submission().getFileName()).isEqualTo("photo.jpg");
  }

  @Test
  void submit_shouldThrowInvalidFileTypeException_whenContentTypeIsNotImage() {
    MockMultipartFile file =
        new MockMultipartFile(
            "image", "document.pdf", "application/pdf", "fake-pdf-bytes".getBytes());

    assertThatThrownBy(() -> submissionService.submit("test@example.com", file))
        .isInstanceOf(InvalidFileTypeException.class)
        .hasMessageContaining("application/pdf");

    verifyNoInteractions(bucketComponent);
    verify(submissionRepository, never()).save(any());
  }

  @Test
  void submit_shouldThrowEmptyFileException_whenFileIsEmpty() {
    MockMultipartFile emptyFile =
        new MockMultipartFile("image", "empty.png", "image/png", new byte[0]);

    assertThatThrownBy(() -> submissionService.submit("test@example.com", emptyFile))
        .isInstanceOf(EmptyFileException.class);

    verifyNoInteractions(bucketComponent);
    verify(submissionRepository, never()).save(any());
  }

  @Test
  void submit_shouldThrowEmptyFileException_whenFileIsNull() {
    assertThatThrownBy(() -> submissionService.submit("test@example.com", null))
        .isInstanceOf(EmptyFileException.class);
  }

  @Test
  void getAll_shouldReturnAllSubmissionsFromRepository() {
    Submission s1 = new Submission();
    Submission s2 = new Submission();
    when(submissionRepository.findAll()).thenReturn(List.of(s1, s2));

    List<Submission> result = submissionService.getAll();

    assertThat(result).hasSize(2).containsExactly(s1, s2);
  }
}
