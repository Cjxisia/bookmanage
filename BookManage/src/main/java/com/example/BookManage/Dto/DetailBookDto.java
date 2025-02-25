package com.example.BookManage.Dto;

import lombok.Data;

import java.util.List;

@Data
public class DetailBookDto {
    private List<BookDto> bookLists;
    private List<BookDto> google_bookLists;

    public DetailBookDto(List<BookDto> bookLists, List<BookDto>google_bookLists){
        this.bookLists = bookLists;
        this.google_bookLists = google_bookLists;
    }
}
