package com.nortal.library.api.config;

import com.nortal.library.core.LibraryService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataLoader {

  @Bean
  CommandLineRunner seedData(LibraryService libraryService) {
    return args -> {
      libraryService.createMember("m1", "Kertu");
      libraryService.createMember("m2", "Rasmus");
      libraryService.createMember("m3", "Liis");
      libraryService.createMember("m4", "Markus");

      libraryService.createBook("b1", "Clean Code");
      libraryService.createBook("b2", "Domain-Driven Design");
      libraryService.createBook("b3", "Refactoring");
      libraryService.createBook("b4", "Effective Java");
      libraryService.createBook("b5", "Design Patterns");
      libraryService.createBook("b6", "The Pragmatic Programmer");
    };
  }
}
