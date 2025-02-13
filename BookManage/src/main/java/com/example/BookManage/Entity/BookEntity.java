package com.example.BookManage.Entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.boot.autoconfigure.web.WebProperties;

@Entity
@Table(name = "book")
@Data
public class BookEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bookTitle")
    private String bookTitle;

    @Column(name = "username")
    private String username;

    @Column(name = "bookAuth")
    private String bookAuth;

    @Column(name = "bookPub")
    private String bookPub;

    @Column(name = "bookPubYear")
    private String bookPubYear;

    @Column(name = "discount")
    private String discount;

    @Column(name = "des", length = 500)
    private String des;

    @Column(name = "ISBN")
    private String ISBN;

    @Column(name = "img")
    private String img;

}
