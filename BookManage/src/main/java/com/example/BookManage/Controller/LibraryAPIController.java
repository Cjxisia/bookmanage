package com.example.BookManage.Controller;

import com.example.BookManage.Dto.BookDto;
import com.example.BookManage.Dto.BookResponseDto;
import com.example.BookManage.Dto.MainBookDto;
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
    public String viewload(Model model){

        MainBookDto mainBookDto = libraryapiService.getBookMain();
        model.addAttribute("newbook", mainBookDto.getNewbook());
        model.addAttribute("bestbook", mainBookDto.getBestbook());

        return "Main_page";
    }

    @GetMapping("/books")
    public String getBooks(
            @RequestParam(name = "searchText") String searchText,
            @RequestParam(name = "Start", defaultValue = "1") int Start,
            Model model,
            HttpSession session) {

        session.setAttribute("searchText", searchText);

        System.out.println("검색어" + searchText);
        System.out.println("시작값" + Start);

        BookResponseDto bookResponse = libraryapiService.getBookInfo(searchText, Start, 10);
        model.addAttribute("books", bookResponse.getBookLists());
        model.addAttribute("searchText", searchText);
        model.addAttribute("totalResults", bookResponse.getTotalResults());

        return "books_page";
    }
}
