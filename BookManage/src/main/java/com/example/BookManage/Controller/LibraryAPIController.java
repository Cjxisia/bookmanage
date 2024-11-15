package com.example.BookManage.Controller;

import com.example.BookManage.Service.LibraryAPIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LibraryAPIController {
    private final LibraryAPIService libraryapiService;

    @Autowired
    public LibraryAPIController(LibraryAPIService libraryapiService) {
        this.libraryapiService = libraryapiService;
    }

    @GetMapping("/books")
    public ResponseEntity<String> getBooks(@RequestParam(name = "keyword") String keyword) {
        String result = libraryapiService.getBookList(keyword);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
