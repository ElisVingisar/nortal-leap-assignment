package com.nortal.library.persistence.jpa;

import com.nortal.library.core.domain.Book;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaBookRepository extends JpaRepository<Book, String> {
  int countByLoanedTo(String loanedTo);

  @Query("SELECT b FROM Book b WHERE b.loanedTo IS NULL AND :memberId MEMBER OF b.reservationQueue")
  List<Book> findAvailableBooksWithMemberInQueue(@Param("memberId") String memberId);
}
