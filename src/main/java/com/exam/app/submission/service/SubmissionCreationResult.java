package com.exam.app.submission.service;

import com.exam.app.endpoint.event.model.SubmissionCreatedEvent;
import com.exam.app.submission.entity.Submission;

public record SubmissionCreationResult(Submission submission, SubmissionCreatedEvent event) {}
