package com.example.BookManage.Dto;

import lombok.Data;

import java.util.List;

@Data
public class MixBookDto {
    private List<BookDto> bookLists;
    private List<BookDto> google_bookLists;

    public MixBookDto(List<BookDto> bookLists, List<BookDto>google_bookLists){
        this.bookLists = bookLists;
        this.google_bookLists = google_bookLists;
    }
}
