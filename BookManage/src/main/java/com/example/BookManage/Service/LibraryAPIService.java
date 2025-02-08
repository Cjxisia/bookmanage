package com.example.BookManage.Service;

import com.example.BookManage.Config.ApiKeyProperties;
import com.example.BookManage.Dto.BookDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class LibraryAPIService {
    private final RestTemplate restTemplate;
    private final ApiKeyProperties apiKeyProperties;
    private final ObjectMapper objectMapper;

    @Autowired
    public LibraryAPIService(RestTemplate restTemplate, ApiKeyProperties apiKeyProperties, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.apiKeyProperties = apiKeyProperties;
        this.objectMapper = objectMapper;
    }

    public List<BookDto> getBookList(String keyword) {
        String apiKey = apiKeyProperties.getKeys().get("library");
        String url = "https://www.data4library.kr/api/srchBooks?authKey=" + apiKey + "&keyword=" + keyword + "&format=json";

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            try {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode docs = root.path("response").path("docs");

                List<BookDto> bookList = new ArrayList<>();
                if(docs.isArray()){
                    for (JsonNode doc : docs){
                        JsonNode book = doc.path("doc");
                        BookDto bookDto = new BookDto();
                        bookDto.setBookTitle(book.path("bookname").asText());
                        bookDto.setBookAuth(book.path("authors").asText());
                        bookDto.setBookPub(book.path("publisher").asText());
                        bookDto.setBookPubYear(book.path("publication_year").asText());
                        bookDto.setGenre(book.path("class_no").asText());
                        bookDto.setISBN(book.path("isbn13").asText());
                        bookDto.setImg(book.path("bookImageURL").asText());
                        bookList.add(bookDto);
                    }
                }
                return bookList;
            } catch (Exception e) {
                e.printStackTrace();
                return List.of(new BookDto());
            }
        } else {
            return List.of(new BookDto());
        }
    }
}
