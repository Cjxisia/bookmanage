package com.example.BookManage.Service;

import com.example.BookManage.Dto.BookDto;
import com.example.BookManage.Entity.BookEntity;
import com.example.BookManage.Repository.BookRepository;
import org.springframework.stereotype.Service;

@Service
public class BookService {
    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository){
        this.bookRepository = bookRepository;
    }

    public void savebooks(BookDto bookDto){
        BookEntity bookEntity = new BookEntity();

        bookEntity.setBook_title(bookDto.getBook_title());
        bookEntity.setUsername(bookDto.getUsername());

        bookRepository.save(bookEntity);
    }
}
