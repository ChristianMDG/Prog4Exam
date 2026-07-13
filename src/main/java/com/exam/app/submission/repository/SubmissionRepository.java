package com.exam.app.submission.repository;


import com.exam.app.submission.entity.Submission;

import java.util.UUID;

public interface SubmissionRepository extends JpaRepository<Submission, UUID> {
}