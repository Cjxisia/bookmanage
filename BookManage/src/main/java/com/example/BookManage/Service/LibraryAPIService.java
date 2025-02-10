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

    public List<BookDto> getBookInfoByTitle(String title) {
        String apiKey = apiKeyProperties.getKeys().get("aladin");
        String url = "http://www.aladin.co.kr/ttb/api/ItemSearch.aspx?ttbkey=" + apiKey + "&Query=" + title + "&MaxResults=10&Start=1&SearchTarget=Book&output=JS";

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        List<BookDto> bookLists = new ArrayList<>();

        if (response.getStatusCode() == HttpStatus.OK) {
            try {
                String jsonResponse = response.getBody();

                jsonResponse = jsonResponse.replace("\\'", "'");

                JsonNode root = objectMapper.readTree(jsonResponse);
                JsonNode itemNode = root.path("item");

                if (itemNode.isArray()) {
                    for (JsonNode bookNode : itemNode) {
                        BookDto bookDto = new BookDto();
                        bookDto.setBookTitle(bookNode.path("title").asText());
                        bookDto.setBookAuth(bookNode.path("author").asText());
                        bookDto.setBookPub(bookNode.path("publisher").asText());
                        bookDto.setBookPubYear(bookNode.path("pubDate").asText());
                        bookDto.setISBN(bookNode.path("isbn").asText());
                        bookDto.setImg(bookNode.path("cover").asText());
                        bookDto.setGenre(bookNode.path("categoryName").asText());
                        bookDto.setDes(bookNode.path("description").asText());

                        bookLists.add(bookDto);
                    }
                } else {
                    System.out.println("No books found for the given title.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return bookLists;
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
