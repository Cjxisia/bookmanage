package com.example.BookManage.Controller;

import com.example.BookManage.Dto.BookDto;
import com.example.BookManage.Service.BookService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class BooksController {
    private final BookService bookService;

    @Autowired
    public BooksController(BookService bookService){
        this.bookService = bookService;
    }


    @PostMapping("/bookmark")
    public String addBookmark(@RequestParam("book_title") String book_title, HttpSession session, Model model){
        String username = (String) session.getAttribute("nickname");

        BookDto bookDto= new BookDto();

        bookDto.setBook_title(book_title);
        bookDto.setUsername(username);

        bookService.savebooks(bookDto);
        model.addAttribute("message",  "북마크에 추가되었습니다.");
        return "redirect:/books";
    }
}
