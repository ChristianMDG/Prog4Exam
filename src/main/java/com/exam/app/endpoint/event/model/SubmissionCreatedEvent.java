package com.exam.app.endpoint.event.model;

import java.time.Duration;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Data
@EqualsAndHashCode(callSuper = false)
public class SubmissionCreatedEvent extends PojaEvent {

    private UUID submissionId;
    private String email;
    private String originalFileName;
    private String originalBucketKey;

    @Override
    public Duration maxConsumerDuration() {
        return Duration.ofSeconds(60);
    }

    @Override
    public Duration maxConsumerBackoffBetweenRetries() {
        return Duration.ofSeconds(30);
    }
}