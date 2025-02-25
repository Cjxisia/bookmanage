package com.example.BookManage.Dto;

import lombok.Data;

import java.util.List;

@Data
public class BookDto {
    private String bookTitle;
    private String username;
    private String bookAuth;
    private String bookPub;
    private String bookPubYear;
    private String link;
    private String discount;
    private String ISBN;
    private String img;
    private String des;
    private String category;
}
