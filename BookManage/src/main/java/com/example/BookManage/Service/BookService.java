package com.example.BookManage.Service;

import com.example.BookManage.Config.ApiKeyProperties;
import com.example.BookManage.Dto.BookDto;
import com.example.BookManage.Entity.BookEntity;
import com.example.BookManage.Repository.BookRepository;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.sound.midi.SysexMessage;
import java.util.List;
import java.util.Optional;

@Service
public class BookService {
    private final BookRepository bookRepository;
    private final RestTemplate restTemplate;
    private final ApiKeyProperties apiKeyProperties;

    public BookService(BookRepository bookRepository, RestTemplate restTemplate, ApiKeyProperties apiKeyProperties){
        this.bookRepository = bookRepository;
        this.restTemplate = restTemplate;
        this.apiKeyProperties = apiKeyProperties;
    }

    private String fetchCategoryFromAladin(String isbn) {
        String apikey = apiKeyProperties.getKeys().get("aladin");
        String url = "https://www.aladin.co.kr/ttb/api/ItemLookUp.aspx?"
                + "ttbkey="
                + apikey
                + "&itemIdType=ISBN"
                + "&ItemId=" + isbn
                + "&output=js"
                + "&Version=20131101";

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JSONObject jsonResponse = new JSONObject(response.getBody());

            return jsonResponse.getJSONArray("item")
                    .getJSONObject(0)
                    .getString("categoryId");
        } catch (Exception e) {
            e.printStackTrace();
            return "Unknown Category";
        }
    }

    public void savebooks(BookDto bookDto){
        Optional<BookEntity> existingBook = bookRepository.findByBookTitle(bookDto.getBookTitle());

        if (existingBook.isPresent()) {
            System.out.println("북마크할 책이 이미 존재합니다!");
            return;
        }

        BookEntity bookEntity = new BookEntity();

        bookEntity.setBookTitle(bookDto.getBookTitle());
        bookEntity.setUsername(bookDto.getUsername());
        bookEntity.setBookPub(bookDto.getBookPub());
        bookEntity.setBookPubYear(bookDto.getBookPubYear());
        bookEntity.setLink(bookDto.getLink());
        bookEntity.setDiscount(bookDto.getDiscount());
        bookEntity.setDes(bookDto.getDes());
        bookEntity.setBookAuth(bookDto.getBookAuth());
        bookEntity.setISBN(bookDto.getISBN());
        bookEntity.setImg(bookDto.getImg());

        String category = fetchCategoryFromAladin(bookDto.getISBN());
        bookEntity.setCategory(category);

        bookRepository.save(bookEntity);
    }

    public void removeBookmark(String username, String bookTitle) {
        Optional<BookEntity> bookmark = bookRepository.findByUsernameAndBookTitle(username, bookTitle);

        System.out.println("username, bookTitle:" + username + bookTitle);

        if (bookmark.isPresent()) {
            BookEntity bookEntity = bookmark.get();
            bookRepository.delete(bookEntity);
        } else {
            throw new RuntimeException("북마크가 존재하지 않습니다.");
        }
    }
}
