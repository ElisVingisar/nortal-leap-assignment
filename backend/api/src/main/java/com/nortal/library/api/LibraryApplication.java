package com.nortal.library.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableCaching
@SpringBootApplication(scanBasePackages = "com.nortal.library")
@EntityScan(basePackages = "com.nortal.library.core")
@EnableJpaRepositories(basePackages = "com.nortal.library.persistence.jpa")
public class LibraryApplication {
  public static void main(String[] args) {
    SpringApplication.run(LibraryApplication.class, args);
  }
}
