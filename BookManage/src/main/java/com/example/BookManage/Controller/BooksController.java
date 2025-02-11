package com.example.BookManage.Controller;

import com.example.BookManage.Dto.BookDto;
import com.example.BookManage.Service.BookService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
public class BooksController {
    private final BookService bookService;

    @Autowired
    public BooksController(BookService bookService){
        this.bookService = bookService;
    }


    @PostMapping("/bookmark")
    public String addBookmark(@ModelAttribute BookDto bookDto, HttpSession session, Model model) {

        String username = (String) session.getAttribute("nickname");
        String searchType= (String) session.getAttribute("searchType");
        String searchText = (String) session.getAttribute("searchText");

        bookDto.setUsername(username);

        bookService.savebooks(bookDto);

        return "redirect:/books?searchType="+ URLEncoder.encode(searchType, StandardCharsets.UTF_8) + "&searchText=" + URLEncoder.encode(searchText, StandardCharsets.UTF_8);
    }
}
