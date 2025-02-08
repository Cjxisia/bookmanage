package com.example.BookManage.Dto;

import lombok.Data;

@Data
public class BookDto {
    private String bookTitle;
    private String username;
    private String author;
    private String publisher;
    private String publicationYear;
    private String genre;
    private String isbn;
    private String bookImageUrl;
}
