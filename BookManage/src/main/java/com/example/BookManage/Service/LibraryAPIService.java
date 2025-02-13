package com.example.BookManage.Service;

import com.example.BookManage.Config.ApiKeyProperties;
import com.example.BookManage.Dto.BookDto;
import com.example.BookManage.Dto.BookResponseDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
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

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();

        String apiKey = apiKeyProperties.getKeys().get("naver"); // 네이버 API 키로 변경
        String apiSecret = apiKeyProperties.getKeys().get("naversecret");

        headers.set("X-Naver-Client-Id", apiKey); // 네이버 클라이언트 ID
        headers.set("X-Naver-Client-Secret", apiSecret); // 네이버 클라이언트 비밀
        return headers;
    }

    public BookResponseDto getBookInfo(String searchtext, int Start) {
        String url = "https://openapi.naver.com/v1/search/book.json?query=" + searchtext + "&display=10&start=" + Start;

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(createHeaders()),
                String.class
        );

        List<BookDto> bookLists = new ArrayList<>();

        if (response.getStatusCode() == HttpStatus.OK) {
            try {
                String jsonResponse = response.getBody();

                // JSON 파싱
                JsonNode root = objectMapper.readTree(jsonResponse);
                JsonNode totalResultsNode = root.path("total");
                int totalResults = totalResultsNode.asInt();

                System.out.println("Total Results: " + totalResults);

                JsonNode itemNode = root.path("items");

                if (itemNode.isArray()) {
                    for (JsonNode bookNode : itemNode) {
                        BookDto bookDto = new BookDto();
                        bookDto.setBookTitle(bookNode.path("title").asText());
                        bookDto.setBookAuth(bookNode.path("author").asText());
                        bookDto.setBookPub(bookNode.path("publisher").asText());
                        bookDto.setBookPubYear(bookNode.path("pubdate").asText());
                        bookDto.setISBN(bookNode.path("isbn").asText());
                        bookDto.setImg(bookNode.path("image").asText());
                        bookDto.setGenre(bookNode.path("category").asText());
                        bookDto.setDes(bookNode.path("description").asText());

                        bookLists.add(bookDto);
                    }
                } else {
                    System.out.println("No books found for the given text.");
                }
                return new BookResponseDto(totalResults, bookLists);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new BookResponseDto(0, new ArrayList<>());
    }

    public List<BookDto> getBookInfoByIsbn(String isbn) {
        String apiKey = apiKeyProperties.getKeys().get("library");
        String url = "http://data4library.kr/api/usageAnalysisList?authKey=" + apiKey + "&isbn13=" + isbn + "&format=json";

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        List<BookDto> bookLists = new ArrayList<>();
        if (response.getStatusCode() == HttpStatus.OK) {
            try {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode bookNode = root.path("response").path("book");

                if (!bookNode.isMissingNode()) {
                    BookDto bookDto = new BookDto();
                    bookDto.setBookTitle(bookNode.path("bookname").asText());
                    bookDto.setBookAuth(bookNode.path("authors").asText());
                    bookDto.setBookPub(bookNode.path("publisher").asText());
                    bookDto.setBookPubYear(bookNode.path("publication_year").asText());
                    bookDto.setISBN(bookNode.path("isbn13").asText());
                    bookDto.setImg(bookNode.path("bookImageURL").asText());
                    bookDto.setGenre(bookNode.path("class_nm").asText());
                    bookDto.setDes(bookNode.path("description").asText());

                    bookLists.add(bookDto);
                } else {
                    System.out.println("No book information found for the given ISBN.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return bookLists;
    }

    public List<BookDto> getBookInfoByKeyword(String keyword) {
        String apiKey = apiKeyProperties.getKeys().get("library");
        String url = "https://www.data4library.kr/api/srchBooks?authKey=" + apiKey + "&keyword=" + keyword + "&format=json";

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        if (response.getStatusCode() == HttpStatus.OK) {
            try {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode docs = root.path("response").path("docs");

                List<BookDto> bookList = new ArrayList<>();
                if (docs.isArray()) {
                    for (JsonNode doc : docs) {
                        JsonNode book = doc.path("doc");
                        String isbn = book.path("isbn13").asText();
                    }
                }
                System.out.println("bookList:" + bookList);
                return bookList;
            } catch (Exception e) {
                e.printStackTrace();
                return Collections.emptyList();
            }
        }else {
            return Collections.emptyList();
        }
    }
}
