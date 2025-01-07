package com.example.BookManage.Service;

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
    private final String apiKey = "c365c8c0beefd22c516b37a3b4d56ab1b73d0a7e3668542040f762c7e9f033d0"; // 발급받은 API 키
    private final ObjectMapper objectMapper;

    @Autowired
    public LibraryAPIService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public List<String> getBookList(String keyword) {
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
