package com.nortal.library.api.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateMemberRequest(@NotBlank String id, @NotBlank String name) {}
