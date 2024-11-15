package com.example.BookManage.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class LibraryAPIService {
    private final RestTemplate restTemplate;
    private final String apiKey = "c365c8c0beefd22c516b37a3b4d56ab1b73d0a7e3668542040f762c7e9f033d0"; // 발급받은 API 키

    @Autowired
    public LibraryAPIService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getBookList(String keyword) {
        String url = "https://www.data4library.kr/api/srchBooks?authKey=" + apiKey + "&keyword=" + keyword + "&format=json";

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            return "API 호출 실패: " + response.getStatusCode();
        }
    }
}
