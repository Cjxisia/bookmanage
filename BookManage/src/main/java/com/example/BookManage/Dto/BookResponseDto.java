package com.example.BookManage.Dto;

import lombok.Data;

import java.util.List;

@Data
public class BookResponseDto {
    private int totalResults;
    private List<BookDto> bookLists;

    public BookResponseDto(int totalResults, List<BookDto> bookLists) {
        this.totalResults = totalResults;
        this.bookLists = bookLists;
    }
}
