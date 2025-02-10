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
    public String getBooks(
            @RequestParam(name = "searchType", defaultValue = "title") String searchType,
            @RequestParam(name = "searchText") String searchText,
            Model model,
            HttpSession session) {

        session.setAttribute("searchType", searchType);
        session.setAttribute("searchText", searchText);

        System.out.println("검색타입"+searchType);
        System.out.println("검색어"+ searchText);

        List<BookDto> bookList = libraryapiService.getBookInfoByTitle(searchText);
        model.addAttribute("books", bookList);
        return "books_page";
    }
}
