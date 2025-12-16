package com.nortal.library.api.controller;

import com.nortal.library.api.dto.BookResponse;
import com.nortal.library.api.dto.BooksResponse;
import com.nortal.library.api.dto.CreateBookRequest;
import com.nortal.library.api.dto.DeleteBookRequest;
import com.nortal.library.api.dto.ResultResponse;
import com.nortal.library.api.dto.UpdateBookRequest;
import com.nortal.library.core.LibraryService;
import com.nortal.library.core.domain.Book;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/books")
public class BookController {

  private final LibraryService libraryService;

  public BookController(LibraryService libraryService) {
    this.libraryService = libraryService;
  }

  @GetMapping
  public BooksResponse list() {
    return new BooksResponse(libraryService.allBooks().stream().map(this::toResponse).toList());
  }

  @GetMapping("/search")
  public BooksResponse search(
      @RequestParam(value = "titleContains", required = false) String titleContains,
      @RequestParam(value = "available", required = false) Boolean available,
      @RequestParam(value = "loanedTo", required = false) String loanedTo) {
    return new BooksResponse(
        libraryService.searchBooks(titleContains, available, loanedTo).stream()
            .map(this::toResponse)
            .toList());
  }

  @PostMapping
  public ResultResponse create(@RequestBody @Valid CreateBookRequest request) {
    LibraryService.Result result = libraryService.createBook(request.id(), request.title());
    return new ResultResponse(result.ok(), result.reason());
  }

  @PutMapping
  public ResultResponse update(@RequestBody @Valid UpdateBookRequest request) {
    LibraryService.Result result = libraryService.updateBook(request.id(), request.title());
    return new ResultResponse(result.ok(), result.reason());
  }

  @DeleteMapping
  public ResultResponse delete(@RequestBody @Valid DeleteBookRequest request) {
    LibraryService.Result result = libraryService.deleteBook(request.id());
    return new ResultResponse(result.ok(), result.reason());
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
