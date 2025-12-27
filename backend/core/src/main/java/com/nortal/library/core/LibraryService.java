package com.nortal.library.core;

import com.nortal.library.core.domain.Book;
import com.nortal.library.core.domain.Member;
import com.nortal.library.core.port.BookRepository;
import com.nortal.library.core.port.MemberRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LibraryService {
  private static final int MAX_LOANS = 5;
  private static final int DEFAULT_LOAN_DAYS = 14;

  private final BookRepository bookRepository;
  private final MemberRepository memberRepository;

  public LibraryService(BookRepository bookRepository, MemberRepository memberRepository) {
    this.bookRepository = bookRepository;
    this.memberRepository = memberRepository;
  }

  // Borrow a book - checks queue order to prevent line-jumping
  public Result borrowBook(String bookId, String memberId) {
    Optional<Book> book = bookRepository.findById(bookId);
    if (book.isEmpty()) {
      return Result.failure("BOOK_NOT_FOUND");
    }
    if (!memberRepository.existsById(memberId)) {
      return Result.failure("MEMBER_NOT_FOUND");
    }

    Book entity = book.get();

    // Check book-specific issues before checking member's borrow limit
    if (memberId.equals(entity.getLoanedTo())) {
      return Result.failure("ALREADY_LOANED");
    }
    if (entity.getLoanedTo() != null) {
      return Result.failure("BOOK_UNAVAILABLE");
    }
    if (!canMemberBorrow(memberId)) {
      return Result.failure("BORROW_LIMIT");
    }

    // If a reservation queue exists, check if there are eligible members
    if (!entity.getReservationQueue().isEmpty()) {
      // Find first eligible member in queue
      String firstEligible = null;
      for (String queuedMember : entity.getReservationQueue()) {
        if (memberRepository.existsById(queuedMember) && canMemberBorrow(queuedMember)) {
          firstEligible = queuedMember;
          break;
        }
      }

      // If there's an eligible member in queue, only they can borrow
      if (firstEligible != null) {
        if (!memberId.equals(firstEligible)) {
          return Result.failure("QUEUE_EXISTS");
        }
        // Remove the member from queue since they're now borrowing
        entity.getReservationQueue().remove(firstEligible);
      }
      // If no eligible members in queue, allow anyone to borrow
    }

    entity.setLoanedTo(memberId);
    entity.setDueDate(LocalDate.now().plusDays(DEFAULT_LOAN_DAYS));
    bookRepository.save(entity);
    return Result.success();
  }

  // Return a book - only the borrower can return it, then pass it to the next eligible member in queue or let the returner receive their reserved books
  public ResultWithNext returnBook(String bookId, String memberId) {
    Optional<Book> book = bookRepository.findById(bookId);
    if (book.isEmpty()) {
      return ResultWithNext.failure();
    }

    Book entity = book.get();

    if (entity.getLoanedTo() == null) {
      return ResultWithNext.failure();
    }

    if (memberId == null || !entity.getLoanedTo().equals(memberId)) {
      return ResultWithNext.failure();
    }

    entity.setLoanedTo(null);
    entity.setDueDate(null);

    // Try to hand off this book to the next eligible reserver in its queue
    String nextMember = tryHandoffBook(entity);
    bookRepository.save(entity);

    // After returning, check if the returning member can now receive any books they've reserved
    processReservationsForMember(memberId);

    return ResultWithNext.success(nextMember);
  }

  // Find the first eligible member in the queue and give them the book, skipping those at their limit
  private String tryHandoffBook(Book book) {
    int queueIndex = 0;
    while (queueIndex < book.getReservationQueue().size()) {
      String candidateMember = book.getReservationQueue().get(queueIndex);

      // Check if member still exists
      if (!memberRepository.existsById(candidateMember)) {
        // Member was deleted - permanently remove from queue
        book.getReservationQueue().remove(queueIndex);
        continue;
      }

      // Check if member can borrow (not at limit)
      if (canMemberBorrow(candidateMember)) {
        // This member can receive the book
        book.setLoanedTo(candidateMember);
        book.setDueDate(LocalDate.now().plusDays(DEFAULT_LOAN_DAYS));
        // Remove from queue since they now have the book
        book.getReservationQueue().remove(queueIndex);
        return candidateMember;
      } else {
        // Member is at their limit - keep them in queue but check next
        queueIndex++;
      }
    }
    return null;
  }

  // Check if a member can now receive any books they have reserved
  private void processReservationsForMember(String memberId) {
    // Skip if member is no longer eligible
    if (!canMemberBorrow(memberId)) {
      return;
    }

    // Get only available books where this member is in the queue
    List<Book> candidateBooks = bookRepository.findAvailableBooksWithMemberInQueue(memberId);

    for (Book book : candidateBooks) {
      // Check if this member is first eligible in queue
      String firstEligible = null;
      for (String queuedMember : book.getReservationQueue()) {
        if (memberRepository.existsById(queuedMember) && canMemberBorrow(queuedMember)) {
          firstEligible = queuedMember;
          break;
        }
      }

      // If this member is first eligible, give them the book
      if (memberId.equals(firstEligible)) {
        book.setLoanedTo(memberId);
        book.setDueDate(LocalDate.now().plusDays(DEFAULT_LOAN_DAYS));
        book.getReservationQueue().remove(memberId);
        bookRepository.save(book);
        break; // Member received a book, stop processing
      }
    }
  }

  // Reserve a book - if it's available and member can borrow, give it to them immediately, otherwise add to queue
  public Result reserveBook(String bookId, String memberId) {
    Optional<Book> book = bookRepository.findById(bookId);
    if (book.isEmpty()) {
      return Result.failure("BOOK_NOT_FOUND");
    }
    if (!memberRepository.existsById(memberId)) {
      return Result.failure("MEMBER_NOT_FOUND");
    }

    Book entity = book.get();

    if (entity.getReservationQueue().contains(memberId)) {
      return Result.failure("ALREADY_RESERVED");
    }

    if (memberId.equals(entity.getLoanedTo())) {
      return Result.failure("ALREADY_LOANED");
    }

    // If book is available and member is eligible, immediately loan it
    if (entity.getLoanedTo() == null && canMemberBorrow(memberId)) {
      entity.setLoanedTo(memberId);
      entity.setDueDate(LocalDate.now().plusDays(DEFAULT_LOAN_DAYS));
      bookRepository.save(entity);
      return Result.success();
    }

    // Book is loaned OR member is at limit - add to reservation queue
    entity.getReservationQueue().add(memberId);
    bookRepository.save(entity);
    return Result.success();
  }

  public Result cancelReservation(String bookId, String memberId) {
    Optional<Book> book = bookRepository.findById(bookId);
    if (book.isEmpty()) {
      return Result.failure("BOOK_NOT_FOUND");
    }
    if (!memberRepository.existsById(memberId)) {
      return Result.failure("MEMBER_NOT_FOUND");
    }

    Book entity = book.get();
    boolean removed = entity.getReservationQueue().remove(memberId);
    if (!removed) {
      return Result.failure("NOT_RESERVED");
    }
    bookRepository.save(entity);
    return Result.success();
  }

  // Check if a member can borrow more books - uses optimized count query instead of loading all books
  public boolean canMemberBorrow(String memberId) {
    if (!memberRepository.existsById(memberId)) {
      return false;
    }
    return bookRepository.countByLoanedTo(memberId) < MAX_LOANS;
  }

  public List<Book> searchBooks(String titleContains, Boolean availableOnly, String loanedTo) {
    return bookRepository.findAll().stream()
        .filter(
            b ->
                titleContains == null
                    || b.getTitle().toLowerCase().contains(titleContains.toLowerCase()))
        .filter(b -> loanedTo == null || loanedTo.equals(b.getLoanedTo()))
        .filter(
            b ->
                availableOnly == null
                    || (availableOnly ? b.getLoanedTo() == null : b.getLoanedTo() != null))
        .toList();
  }

  public List<Book> overdueBooks(LocalDate today) {
    return bookRepository.findAll().stream()
        .filter(b -> b.getLoanedTo() != null)
        .filter(b -> b.getDueDate() != null && b.getDueDate().isBefore(today))
        .toList();
  }

  public Result extendLoan(String bookId, int days) {
    if (days == 0) {
      return Result.failure("INVALID_EXTENSION");
    }
    Optional<Book> book = bookRepository.findById(bookId);
    if (book.isEmpty()) {
      return Result.failure("BOOK_NOT_FOUND");
    }
    Book entity = book.get();
    if (entity.getLoanedTo() == null) {
      return Result.failure("NOT_LOANED");
    }
    LocalDate baseDate =
        entity.getDueDate() == null
            ? LocalDate.now().plusDays(DEFAULT_LOAN_DAYS)
            : entity.getDueDate();
    entity.setDueDate(baseDate.plusDays(days));
    bookRepository.save(entity);
    return Result.success();
  }

  public MemberSummary memberSummary(String memberId) {
    if (!memberRepository.existsById(memberId)) {
      return new MemberSummary(false, "MEMBER_NOT_FOUND", List.of(), List.of());
    }
    List<Book> books = bookRepository.findAll();
    List<Book> loans = new ArrayList<>();
    List<ReservationPosition> reservations = new ArrayList<>();
    for (Book book : books) {
      if (memberId.equals(book.getLoanedTo())) {
        loans.add(book);
      }
      int idx = book.getReservationQueue().indexOf(memberId);
      if (idx >= 0) {
        reservations.add(new ReservationPosition(book.getId(), idx));
      }
    }
    return new MemberSummary(true, null, loans, reservations);
  }

  public Optional<Book> findBook(String id) {
    return bookRepository.findById(id);
  }

  public List<Book> allBooks() {
    return bookRepository.findAll();
  }

  public List<Member> allMembers() {
    return memberRepository.findAll();
  }

  public Result createBook(String id, String title) {
    if (id == null || title == null) {
      return Result.failure("INVALID_REQUEST");
    }
    bookRepository.save(new Book(id, title));
    return Result.success();
  }

  public Result updateBook(String id, String title) {
    Optional<Book> existing = bookRepository.findById(id);
    if (existing.isEmpty()) {
      return Result.failure("BOOK_NOT_FOUND");
    }
    if (title == null) {
      return Result.failure("INVALID_REQUEST");
    }
    Book book = existing.get();
    book.setTitle(title);
    bookRepository.save(book);
    return Result.success();
  }

  public Result deleteBook(String id) {
    Optional<Book> existing = bookRepository.findById(id);
    if (existing.isEmpty()) {
      return Result.failure("BOOK_NOT_FOUND");
    }
    Book book = existing.get();
    bookRepository.delete(book);
    return Result.success();
  }

  public Result createMember(String id, String name) {
    if (id == null || name == null) {
      return Result.failure("INVALID_REQUEST");
    }
    memberRepository.save(new Member(id, name));
    return Result.success();
  }

  public Result updateMember(String id, String name) {
    Optional<Member> existing = memberRepository.findById(id);
    if (existing.isEmpty()) {
      return Result.failure("MEMBER_NOT_FOUND");
    }
    if (name == null) {
      return Result.failure("INVALID_REQUEST");
    }
    Member member = existing.get();
    member.setName(name);
    memberRepository.save(member);
    return Result.success();
  }

  public Result deleteMember(String id) {
    Optional<Member> existing = memberRepository.findById(id);
    if (existing.isEmpty()) {
      return Result.failure("MEMBER_NOT_FOUND");
    }
    memberRepository.delete(existing.get());
    return Result.success();
  }

  public record Result(boolean ok, String reason) {
    public static Result success() {
      return new Result(true, null);
    }

    public static Result failure(String reason) {
      return new Result(false, reason);
    }
  }

  public record ResultWithNext(boolean ok, String nextMemberId) {
    public static ResultWithNext success(String nextMemberId) {
      return new ResultWithNext(true, nextMemberId);
    }

    public static ResultWithNext failure() {
      return new ResultWithNext(false, null);
    }
  }

  public record MemberSummary(
      boolean ok, String reason, List<Book> loans, List<ReservationPosition> reservations) {}

  public record ReservationPosition(String bookId, int position) {}
}
