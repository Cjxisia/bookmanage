package com.example.BookManage.Dto;

import lombok.Data;

import java.util.List;

@Data
public class MypageDto {
    private List<BookDto> bookLists;
    private List<BookDto> aladin_bookLists;
    private List<BookDto> google_bookLists;

    public MypageDto(List<BookDto> bookLists,  List<BookDto> aladin_bookLists, List<BookDto>google_bookLists){
        this.bookLists = bookLists;
        this.aladin_bookLists = aladin_bookLists;
        this.google_bookLists = google_bookLists;
    }
}
