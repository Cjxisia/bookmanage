package com.example.BookManage.Controller;

import com.example.BookManage.Dto.BookDto;
import com.example.BookManage.Service.BookService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
    public String addBookmark(@RequestParam("bookTitle") String bookTitle,
                              @RequestParam("bookAuth") String bookAuth,
                              @RequestParam("bookPub") String bookPub,
                              @RequestParam("bookPubYear") String bookPubYear,
                              @RequestParam("ISBN") String ISBN,
                              @RequestParam("img") String img,
                              HttpSession session, Model model) {

        String username = (String) session.getAttribute("nickname");
        String searchType= (String) session.getAttribute("searchType");
        String searchText = (String) session.getAttribute("searchText");

        System.out.println("booktile = " + bookTitle);
        BookDto bookDto = new BookDto();
        bookDto.setBookTitle(bookTitle);
        bookDto.setBookAuth(bookAuth);
        bookDto.setBookPub(bookPub);
        bookDto.setBookPubYear(bookPubYear);
        bookDto.setISBN(ISBN);
        bookDto.setImg(img);
        bookDto.setUsername(username);

        bookService.savebooks(bookDto);

        return "redirect:/books?keyword=" + URLEncoder.encode(searchText, StandardCharsets.UTF_8);
    }
}
