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

        bookEntity.setBookTitle(bookDto.getBookTitle());
        bookEntity.setUsername(bookDto.getUsername());
        bookEntity.setBookPub(bookDto.getBookPub());
        bookEntity.setBookPubYear(bookDto.getBookPubYear());
        bookEntity.setLink(bookDto.getLink());
        bookEntity.setDiscount(bookDto.getDiscount());
        bookEntity.setDes(bookDto.getDes());
        bookEntity.setBookAuth(bookDto.getBookAuth());
        bookEntity.setISBN(bookDto.getISBN());
        bookEntity.setImg(bookDto.getImg());

        bookRepository.save(bookEntity);
    }
}
