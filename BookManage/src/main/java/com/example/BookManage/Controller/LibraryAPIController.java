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
    public String getBooks(@RequestParam(name = "keyword") String title, Model model, HttpSession session) {
        session.setAttribute("classi", "title");
        session.setAttribute("search", title); //제목이냐 키워드냐 저자냐 출판사냐에 따라서 저장되는값이 다름
        List<BookDto> bookList = libraryapiService.getBookInfoByTitle(title);
        model.addAttribute("books", bookList);
        return "books_page";
    }
}
