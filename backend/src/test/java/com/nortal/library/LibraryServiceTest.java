package com.nortal.library;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LibraryServiceTest {

    private LibraryService service;

    @BeforeEach
    void setUp() {
        service = new LibraryService();
        service.registerMember(new Member("m1", "Kertu"));
        service.registerMember(new Member("m2", "Rasmus"));
        service.registerMember(new Member("m3", "Liis"));

        service.registerBook(new Book("b1", "Clean Code"));
        service.registerBook(new Book("b2", "Domain-Driven Design"));
        service.registerBook(new Book("b3", "Refactoring"));
        service.registerBook(new Book("b4", "Effective Java"));
        service.registerBook(new Book("b5", "Design Patterns"));
        service.registerBook(new Book("b6", "The Pragmatic Programmer"));
    }

    @Test
    void listsAllBooks() {
        int bookCount = 0;
        var bookIterator = service.allBooks().iterator();
        while (bookIterator.hasNext()) {
            bookIterator.next();
            bookCount++;
        }
        assertEquals(6, bookCount);
        assertTrue(service.findBook("b1").isPresent());
    }

    @Test
    void listsAllMembers() {
        int memberCount = 0;
        var memberIterator = service.allMembers().iterator();
        while (memberIterator.hasNext()) {
            memberIterator.next();
            memberCount++;
        }
        assertEquals(3, memberCount);
        assertTrue(service.allMembers().iterator().hasNext());
    }
}

