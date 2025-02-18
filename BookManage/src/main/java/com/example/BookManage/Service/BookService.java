package com.example.BookManage.Service;

import com.example.BookManage.Config.ApiKeyProperties;
import com.example.BookManage.Dto.BookDto;
import com.example.BookManage.Entity.BookEntity;
import com.example.BookManage.Repository.BookRepository;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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
                    .getString("categoryName");
        } catch (Exception e) {
            e.printStackTrace();
            return "Unknown Category";  // 오류 발생 시 기본값 반환
        }
    }

    public void savebooks(BookDto bookDto){
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
}
