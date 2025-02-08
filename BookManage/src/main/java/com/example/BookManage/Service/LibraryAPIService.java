package com.example.BookManage.Service;

import com.example.BookManage.Config.ApiKeyProperties;
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

    public List<String> getBookList(String keyword) {
        String apiKey = apiKeyProperties.getKeys().get("library");
        String url = "https://www.data4library.kr/api/srchBooks?authKey=" + apiKey + "&keyword=" + keyword + "&format=json";

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            try {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode docs = root.path("response").path("docs");

                List<String> bookTitles = new ArrayList<>();
                if(docs.isArray()){
                    for (JsonNode doc : docs){
                        JsonNode book = doc.path("doc");
                        String bookTitle = book.path("bookname").asText();
                        String bookAuth = book.path("authors").asText();
                        String bookPub = book.path("publisher").asText();
                        String bookPub_year = book.path("publication_year").asText();
                        String genre = book.path("class_no").asText();
                        String ISBN = book.path("isbn13").asText();
                        String img = book.path("bookImageURL").asText();
                        bookTitles.add(bookTitle);
                    }
                }
                return bookTitles;
            } catch (Exception e) {
                e.printStackTrace();
                return List.of("Json 파싱 실패");
            }
        } else {
            return List.of("API 호출 실패" + response.getStatusCode());
        }
    }
}
