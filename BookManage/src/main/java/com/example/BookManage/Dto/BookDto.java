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
    private String genre;
    private String ISBN;
    private String img;
    private String des;

    private int loanCount;  // 대출 횟수
    private List<String> recommendList;  // 추천 도서 목록
    private List<String> keywordList;  // 키워드 목록
    private List<String> loanRecommendList;  // 함께 대출된 도서 목록
}
