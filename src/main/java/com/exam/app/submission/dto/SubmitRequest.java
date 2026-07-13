package com.exam.app.submission.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubmitRequest {

  @NotBlank(message = "email est obligatoire")
  @Email(message = "email invalide")
  private String email;

  @NotBlank(message = "fileName est obligatoire")
  private String fileName;

  @NotBlank(message = "imageBase64 est obligatoire")
  private String imageBase64;
}
