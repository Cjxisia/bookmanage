package com.example.BookManage.Repository;

import com.example.BookManage.Entity.BookEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<BookEntity, Long> {
    List<BookEntity> findByUsername(String username);
    Optional<BookEntity> findByBookTitle(String bookTitle);
    Optional<BookEntity> findByUsernameAndBookTitle(String username, String bookTitle);
}
