package com.example.BookManage.Service;

import com.example.BookManage.Config.ApiKeyProperties;
import com.example.BookManage.Dto.BookDto;
import com.example.BookManage.Dto.BookResponseDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openkoreantext.processor.OpenKoreanTextProcessor;
import org.openkoreantext.processor.tokenizer.KoreanTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import scala.collection.JavaConverters;
import scala.collection.Seq;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

                JsonNode root = objectMapper.readTree(jsonResponse);
                JsonNode totalResultsNode = root.path("total");
                int totalResults = totalResultsNode.asInt();

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
                        bookDto.setDiscount(bookNode.path("discount").asText());
                        bookDto.setDes(bookNode.path("description").asText());
                        bookDto.setLink(bookNode.path("link").asText());
                        System.out.println(bookNode.toString());

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


    public List<BookDto> getBookInfoDetail(String isbn) {
        String apiKey = apiKeyProperties.getKeys().get("library");
        String url = "http://data4library.kr/api/usageAnalysisList?authKey=" + apiKey + "&isbn13=" + isbn + "&format=json";
        String description = "";

        List<BookDto> bookLists = getBookInfo(isbn, 1).getBookLists();
        if (!bookLists.isEmpty()) {
            description = bookLists.get(0).getDes();
        }

        CharSequence normalized = OpenKoreanTextProcessor.normalize(description);
        Seq<KoreanTokenizer.KoreanToken> tokens = OpenKoreanTextProcessor.tokenize(normalized);

        List<KoreanTokenizer.KoreanToken> tokenList = JavaConverters.seqAsJavaList(tokens);

        List<String> nouns = tokenList.stream()
                .filter(token -> token.pos().toString().equals("Noun")) // 명사만 선택
                .map(token -> token.text()) // 텍스트 추출
                .collect(Collectors.toList());

        Map<String, Long> frequencyMap = nouns.stream()
                .collect(Collectors.groupingBy(noun -> noun, Collectors.counting()));

        // 빈도수 기준으로 내림차순 정렬 후 상위 2개 키워드 선택
        List<String> topKeywords = frequencyMap.entrySet().stream()
                .sorted((entry1, entry2) -> Long.compare(entry2.getValue(), entry1.getValue()))
                .limit(2)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        System.out.println(topKeywords);


        return bookLists;
    }
}
