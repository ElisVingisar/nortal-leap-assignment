package com.nortal.library.api.controller;

import com.nortal.library.api.dto.BookResponse;
import com.nortal.library.api.dto.BooksResponse;
import com.nortal.library.api.dto.BorrowRequest;
import com.nortal.library.api.dto.CancelReservationRequest;
import com.nortal.library.api.dto.LoanExtensionRequest;
import com.nortal.library.api.dto.ReserveRequest;
import com.nortal.library.api.dto.ResultResponse;
import com.nortal.library.api.dto.ResultWithNextResponse;
import com.nortal.library.api.dto.ReturnRequest;
import com.nortal.library.core.LibraryService;
import com.nortal.library.core.domain.Book;
import jakarta.validation.Valid;
import java.time.LocalDate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class LoanController {

  private final LibraryService libraryService;

  public LoanController(LibraryService libraryService) {
    this.libraryService = libraryService;
  }

  @PostMapping("/borrow")
  public ResultResponse borrow(@RequestBody @Valid BorrowRequest request) {
    LibraryService.Result result = libraryService.borrowBook(request.bookId(), request.memberId());
    return new ResultResponse(result.ok(), result.reason());
  }

  @PostMapping("/reserve")
  public ResultResponse reserve(@RequestBody @Valid ReserveRequest request) {
    LibraryService.Result result = libraryService.reserveBook(request.bookId(), request.memberId());
    return new ResultResponse(result.ok(), result.reason());
  }

  @PostMapping("/cancel-reservation")
  public ResultResponse cancelReservation(@RequestBody @Valid CancelReservationRequest request) {
    LibraryService.Result result =
        libraryService.cancelReservation(request.bookId(), request.memberId());
    return new ResultResponse(result.ok(), result.reason());
  }

  @PostMapping("/return")
  public ResultWithNextResponse returnBook(@RequestBody @Valid ReturnRequest request) {
    LibraryService.ResultWithNext result = libraryService.returnBook(request.bookId());
    return new ResultWithNextResponse(result.ok(), result.nextMemberId());
  }

  @PostMapping("/extend")
  public ResultResponse extend(@RequestBody @Valid LoanExtensionRequest request) {
    LibraryService.Result result = libraryService.extendLoan(request.bookId(), request.days());
    return new ResultResponse(result.ok(), result.reason());
  }

  @GetMapping("/overdue")
  public BooksResponse overdue() {
    return new BooksResponse(
        libraryService.overdueBooks(LocalDate.now()).stream().map(this::toResponse).toList());
  }

  private BookResponse toResponse(Book book) {
    return new BookResponse(
        book.getId(),
        book.getTitle(),
        book.getLoanedTo(),
        book.getDueDate(),
        book.getReservationQueue());
  }
}
