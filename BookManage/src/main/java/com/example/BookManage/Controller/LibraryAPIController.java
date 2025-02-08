package com.example.BookManage.Controller;

import com.example.BookManage.Dto.BookDto;
import com.example.BookManage.Service.LibraryAPIService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class LibraryAPIController {
    private final LibraryAPIService libraryapiService;

    @Autowired
    public LibraryAPIController(LibraryAPIService libraryapiService) {
        this.libraryapiService = libraryapiService;
    }

    @GetMapping("/")
    public String viewload(){
        return "Main_page";
    }

    @GetMapping("/books")
    public String getBooks(@RequestParam(name = "keyword") String keyword, Model model, HttpSession session) {
        session.setAttribute("keyword", keyword);
        List<BookDto> bookList = libraryapiService.getBookList(keyword);
        model.addAttribute("books", bookList); // 데이터를 Thymeleaf로 전달
        return "books_page"; // templates/books_page.html 파일로 이동
    }
}
