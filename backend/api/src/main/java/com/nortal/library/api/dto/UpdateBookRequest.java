package com.nortal.library.api.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateBookRequest(@NotBlank String id, @NotBlank String title) {}
