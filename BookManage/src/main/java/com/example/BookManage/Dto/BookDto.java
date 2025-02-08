package com.example.BookManage.Dto;

import lombok.Data;

@Data
public class BookDto {
    private String bookTitle;
    private String username;
    private String bookAuth;
    private String bookPub;
    private String bookPubYear;
    private String genre;
    private String ISBN;
    private String img;
}
