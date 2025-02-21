package com.example.BookManage.Dto;

import lombok.Data;

import java.util.List;

@Data
public class MainBookDto {
    private List<BookDto> newbook;
    private List<BookDto> bestbook;

    public MainBookDto(List<BookDto> newbook, List<BookDto>bestbook){
        this.newbook = newbook;
        this.bestbook = bestbook;
    }
}
