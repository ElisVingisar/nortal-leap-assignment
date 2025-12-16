package com.nortal.library.api.dto;

import jakarta.validation.constraints.NotBlank;

public record BorrowRequest(@NotBlank String bookId, @NotBlank String memberId) {}
