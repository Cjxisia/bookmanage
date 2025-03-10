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
        session.setAttribute("Start", String.valueOf(Start));

        int book_start = (Start-1) * 10 +1;

        System.out.println("검색어" + searchText);
        System.out.println("시작값" + book_start);


        BookResponseDto bookResponse = libraryapiService.getBookInfo(searchText, book_start, 10);
        int totalResults = bookResponse.getTotalResults();
        int totalPages = (int) Math.ceil((double) totalResults / 10);
        int pageStart = ((Start - 1) / 10) * 10 + 1;
        int pageEnd = Math.min(pageStart + 9, totalPages);
        int prevStart = pageStart - 10;
        int nextStart = pageStart + 10;

        model.addAttribute("books", bookResponse.getBookLists());
        model.addAttribute("searchText", searchText);
        model.addAttribute("totalResults", bookResponse.getTotalResults());
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("currentPageStart", pageStart);
        model.addAttribute("currentPageEnd", pageEnd);
        model.addAttribute("prevStart", prevStart);
        model.addAttribute("nextStart", nextStart);

        return "books_page";
    }
}
