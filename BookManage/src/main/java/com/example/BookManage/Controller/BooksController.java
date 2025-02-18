package com.example.BookManage.Controller;

import com.example.BookManage.Dto.BookDto;
import com.example.BookManage.Service.BookService;
import com.example.BookManage.Service.LibraryAPIService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
public class BooksController {
    private final BookService bookService;
    private final LibraryAPIService libraryAPIService;

    @Autowired
    public BooksController(BookService bookService, LibraryAPIService libraryAPIService){
        this.bookService = bookService;
        this.libraryAPIService = libraryAPIService;
    }


    @PostMapping("/bookmark")
    public String addBookmark(@ModelAttribute BookDto bookDto, HttpSession session) {

        String username = (String) session.getAttribute("nickname");
        String searchText = (String) session.getAttribute("searchText");

        bookDto.setUsername(username);

        bookService.savebooks(bookDto);

        return "redirect:/books?&searchText=" + URLEncoder.encode(searchText, StandardCharsets.UTF_8);
    }

    @GetMapping("/books/detail/{ISBN}")
    public String getBookDetail(@PathVariable("ISBN") String isbn, Model model){
        List<BookDto> bookList = libraryAPIService.getBookInfoDetail(isbn);
        model.addAttribute("books", bookList);
        return "book_detail_page";
    }
}
