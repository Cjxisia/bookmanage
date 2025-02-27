package com.example.BookManage.Controller;

import com.example.BookManage.Dto.BookDto;
import com.example.BookManage.Dto.DetailBookDto;
import com.example.BookManage.Dto.MypageDto;
import com.example.BookManage.Service.BookService;
import com.example.BookManage.Service.LibraryAPIService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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

    @GetMapping("/books/detail/{title}")
    public String getBookDetail(@PathVariable("title") String title, Model model){
        DetailBookDto mixBook = libraryAPIService.getBookInfoDetail(title);

        model.addAttribute("books", mixBook.getBookLists());
        model.addAttribute("google_books", mixBook.getGoogle_bookLists());

        return "book_detail_page";
    }

    @GetMapping("/mypage/{name}")
    public String getMypage(@PathVariable("name") String name, Model model){
        MypageDto mypageDto = libraryAPIService.getMyInfo(name);

        model.addAttribute("books", mypageDto.getBookLists());
        model.addAttribute("aladin_books", mypageDto.getAladin_bookLists());
        model.addAttribute("google_books", mypageDto.getGoogle_bookLists());

        return "my_page";
    }
}
