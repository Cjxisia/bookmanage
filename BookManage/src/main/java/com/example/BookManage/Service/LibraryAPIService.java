package com.example.BookManage.Service;

import com.example.BookManage.Config.ApiKeyProperties;
import com.example.BookManage.Dto.*;
import com.example.BookManage.Entity.BookEntity;
import com.example.BookManage.Repository.BookRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openkoreantext.processor.OpenKoreanTextProcessor;
import org.openkoreantext.processor.tokenizer.KoreanTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import scala.collection.JavaConverters;
import scala.collection.Seq;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LibraryAPIService {
    private final RestTemplate restTemplate;
    private final ApiKeyProperties apiKeyProperties;
    private final ObjectMapper objectMapper;
    private final BookRepository bookRepository;
    private Set<String> stopwords = new HashSet<>();

    @Autowired
    public LibraryAPIService(RestTemplate restTemplate, ApiKeyProperties apiKeyProperties, ObjectMapper objectMapper, BookRepository bookRepository) {
        this.restTemplate = restTemplate;
        this.apiKeyProperties = apiKeyProperties;
        this.objectMapper = objectMapper;
        this.bookRepository = bookRepository;
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();

        String apiKey = apiKeyProperties.getKeys().get("naver"); // 네이버 API 키로 변경
        String apiSecret = apiKeyProperties.getKeys().get("naversecret");

        headers.set("X-Naver-Client-Id", apiKey); // 네이버 클라이언트 ID
        headers.set("X-Naver-Client-Secret", apiSecret); // 네이버 클라이언트 비밀
        return headers;
    }

    public void loadStopwords() {
        try {
            List<String> lines = Files.readAllLines(Paths.get(getClass().getClassLoader().getResource("stopword.txt").toURI()));
            stopwords = lines.stream()
                    .map(String::trim)
                    .collect(Collectors.toSet());
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public List<String> extraction_keyword(String text, int maxSize) {
        loadStopwords();

        CharSequence normalized = OpenKoreanTextProcessor.normalize(text);
        Seq<KoreanTokenizer.KoreanToken> tokens = OpenKoreanTextProcessor.tokenize(normalized);

        List<KoreanTokenizer.KoreanToken> tokenList = JavaConverters.seqAsJavaList(tokens);

        List<String> nouns = tokenList.stream()
                .filter(token -> token.pos().toString().equals("Noun") && !token.pos().toString().equals("NNP"))
                .map(KoreanTokenizer.KoreanToken::text) // 텍스트 추출
                .filter(noun -> !stopwords.contains(noun))
                .collect(Collectors.toList());

        System.out.println("text: " + text);
        System.out.println("nouns: " + nouns);

        Map<String, Long> frequencyMap = nouns.stream()
                .collect(Collectors.groupingBy(noun -> noun, Collectors.counting()));

        Map<String, Integer> firstOccurrence = new HashMap<>();
        for (int i = 0; i < nouns.size(); i++) {
            firstOccurrence.putIfAbsent(nouns.get(i), i); // 처음 등장한 위치만 저장
        }

        List<String> topKeywords = frequencyMap.entrySet().stream()
                .sorted((entry1, entry2) -> {
                    int freqCompare = Long.compare(entry2.getValue(), entry1.getValue()); // 빈도수 내림차순 정렬
                    return (freqCompare != 0) ? freqCompare : Integer.compare(firstOccurrence.get(entry1.getKey()), firstOccurrence.get(entry2.getKey())); // 동일한 빈도수일 경우 등장 순서 유지
                })
                .limit(maxSize)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        System.out.println("topKeywords: " + topKeywords);

        return topKeywords;
    }


    public List<BookDto> getGoogleBook(String keyword){
        String apikey = apiKeyProperties.getKeys().get("google");
        String googleBooksUrl = "https://www.googleapis.com/books/v1/volumes?q=" + keyword + "&key=" + apikey;
        List<BookDto> google_bookLists = new ArrayList<>();

        try {
            RestTemplate restTemplate = new RestTemplate();
            String googleBooksResponse = restTemplate.getForObject(googleBooksUrl, String.class);

            JSONObject googleResponse = new JSONObject(googleBooksResponse);
            JSONArray items = googleResponse.optJSONArray("items");

            System.out.println("구글북스:" + googleResponse);

            if (items != null) {
                int limit = Math.min(items.length(), 10); // 최대 10개까지 처리
                for (int i = 0; i < limit; i++) {
                    JSONObject volumeInfo = items.getJSONObject(i).optJSONObject("volumeInfo");

                    if (volumeInfo != null) {
                        BookDto bookDto = new BookDto();

                        String bookTitle = volumeInfo.optString("title", "제목 없음");
                        JSONArray authorsArray = volumeInfo.optJSONArray("authors");
                        String bookAuth = (authorsArray != null && authorsArray.length() > 0) ? authorsArray.getString(0) : "저자 정보 없음";
                        String bookPub = volumeInfo.optString("publisher", "출판사 정보 없음");
                        String bookPubYear = volumeInfo.optString("publishedDate", "출판 연도 없음");
                        String isbn = "";
                        JSONArray industryIdentifiers = volumeInfo.optJSONArray("industryIdentifiers");
                        if (industryIdentifiers != null) {
                            for (int j = 0; j < industryIdentifiers.length(); j++) {
                                JSONObject identifier = industryIdentifiers.getJSONObject(j);
                                if ("ISBN_13".equals(identifier.optString("type"))) {
                                    isbn = identifier.optString("identifier", "");
                                    break;
                                }
                            }
                        }
                        String bookImageUrl = "";
                        if (volumeInfo.has("imageLinks")) {
                            bookImageUrl = volumeInfo.getJSONObject("imageLinks").optString("thumbnail", "");
                        }
                        String discount = "0";
                        JSONObject saleInfo = items.getJSONObject(i).optJSONObject("saleInfo");
                        if (saleInfo != null && saleInfo.has("retailPrice")) {
                            JSONObject retailPrice = saleInfo.getJSONObject("retailPrice");
                            if (retailPrice.has("amount")) {
                                double amount = retailPrice.getDouble("amount");
                                String currencyCode = retailPrice.optString("currencyCode", "통화 정보 없음");
                                discount = String.format("%s %.2f", currencyCode, amount);
                            }
                        }

                        String bookDescription = volumeInfo.optString("description", "No description available");
                        String bookLink = volumeInfo.optString("infoLink", "링크 없음");

                        bookDto.setBookTitle(bookTitle);
                        bookDto.setBookAuth(bookAuth);
                        bookDto.setBookPub(bookPub);
                        bookDto.setBookPubYear(bookPubYear);
                        bookDto.setISBN(isbn);
                        bookDto.setImg(bookImageUrl);
                        bookDto.setDiscount(discount);
                        bookDto.setDes(bookDescription);
                        bookDto.setLink(bookLink);

                        google_bookLists.add(bookDto);
                    }
                }
            } else {
                System.out.println("Google Books에서 책을 찾을 수 없음: " + keyword);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return google_bookLists;
    }

    public List<BookDto> getAladinBook(String url){
        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.getForObject(url, String.class);
        response = response.replace("\\'", "'");


        List<BookDto> bookLists = new ArrayList<>();

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(response);

            System.out.println("aladin:"+rootNode);

            JsonNode itemsNode = rootNode.path("item");
            for (JsonNode bookNode : itemsNode) {
                BookDto bookDto = new BookDto();
                bookDto.setBookTitle(bookNode.path("title").asText());
                bookDto.setBookAuth(bookNode.path("author").asText());
                bookDto.setBookPub(bookNode.path("publisher").asText());
                bookDto.setBookPubYear(bookNode.path("pubDate").asText());
                bookDto.setISBN(bookNode.path("isbn").asText());
                bookDto.setImg(bookNode.path("cover").asText());
                bookDto.setDiscount(bookNode.path("priceSales").asText());
                bookDto.setDes(bookNode.path("description").asText());
                bookDto.setLink(bookNode.path("link").asText());
                bookDto.setCategory(bookNode.path("categoryName").asText());

                bookLists.add(bookDto);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("aladinbook:" + bookLists);

        return bookLists;
    }

    public BookResponseDto getBookInfo(String searchtext, int start, int display) {
        String url = "https://openapi.naver.com/v1/search/book.json?query=" + searchtext + "&display="+ display +"&start=" + start;

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

                if (totalResults > 0) {
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
                    bookLists = getGoogleBook(searchtext);
                }
                return new BookResponseDto(totalResults, bookLists);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new BookResponseDto(0, new ArrayList<>());
    }


    public DetailBookDto getBookInfoDetail(String title) {
        String regex = "\\s*\\(.*";
        String replace_title = title.replaceAll(regex, "");
        String processtitle = replace_title.replaceAll("\\s", "");
        System.out.println("processtitle:" + processtitle);
        String description = "";
        List<BookDto> google_bookLists = new ArrayList<>();
        List<BookDto> bookLists = new ArrayList<>();

        bookLists.add(getBookInfo(processtitle, 1, 1).getBookLists().get(0));
        if (!bookLists.isEmpty()) {
            if (bookLists.get(0).getBookTitle().replace(" ", "").equals(title.replace(" ", ""))) {
                description = bookLists.get(0).getDes();
            }else{
                String aladinapikey = apiKeyProperties.getKeys().get("aladin");
                String aladinurl  = "https://www.aladin.co.kr/ttb/api/ItemSearch.aspx?ttbkey=" + aladinapikey + "&Query=" + title + "&QueryType=keyword&MaxResults=1&Start=1&SearchTarget=Book&output=js";
                bookLists = getAladinBook(aladinurl);
                if(!bookLists.isEmpty()) {
                    if (bookLists.get(0).getBookTitle().replace(" ", "").equals(title.replace(" ", ""))) {
                        description = bookLists.get(0).getDes();
                    } else {
                        bookLists = new ArrayList<>();
                        bookLists.add(getGoogleBook(title).get(0));
                        description = bookLists.get(0).getDes();
                    }
                }else{
                    bookLists = new ArrayList<>();
                    bookLists.add(getGoogleBook(title).get(0));
                    description = bookLists.get(0).getDes();
                }
            }
        }else{
            String aladinapikey = apiKeyProperties.getKeys().get("aladin");
            String aladinurl  = "https://www.aladin.co.kr/ttb/api/ItemSearch.aspx?ttbkey=" + aladinapikey + "&Query=" + title + "&QueryType=keyword&MaxResults=1&Start=1&SearchTarget=Book&output=js";
            System.out.println("aladinrul=" + aladinurl);
            bookLists = getAladinBook(aladinurl);
            description = bookLists.get(0).getDes();
        }

        String googleapikey = apiKeyProperties.getKeys().get("google");
        String googleBooksUrl = "https://www.googleapis.com/books/v1/volumes?q=" + processtitle + "&key=" + googleapikey;

        try {
            RestTemplate restTemplate = new RestTemplate();
            String googleBooksResponse = restTemplate.getForObject(googleBooksUrl, String.class);

            JSONObject googleResponse = new JSONObject(googleBooksResponse);
            JSONArray items = googleResponse.optJSONArray("items");

            if (items != null && items.length() > 0) {
                JSONObject volumeInfo = items.getJSONObject(0).getJSONObject("volumeInfo");
                String google_title = volumeInfo.optString("title");
                System.out.println("google_title:" + google_title);
                if(google_title.contains(title)) {
                    if(!description.contains(volumeInfo.optString("description"))) {
                        System.out.println("google_des:" + volumeInfo.optString("description"));
                        System.out.println("items:" + items);
                        description = description + volumeInfo.optString("description");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String aladinapikey = apiKeyProperties.getKeys().get("aladin");
        String aladinurl  = "https://www.aladin.co.kr/ttb/api/ItemSearch.aspx?ttbkey=" + aladinapikey + "&Query=" + processtitle + "&QueryType=keyword&MaxResults=1&Start=1&output=js";
        List<BookDto> aladinLists = getAladinBook(aladinurl);
        if (!aladinLists.isEmpty()) {
            if(aladinLists.get(0).getBookTitle().contains(title)) {
                if(!description.contains(aladinLists.get(0).getDes())) {
                    description = description + aladinLists.get(0).getDes();
                    System.out.println("aladin_des:" + description);
                }
            }
        }

        List<String>keyword = extraction_keyword(description, 3);
        List<String>titleword = extraction_keyword(title, 2);
        keyword.removeAll(titleword);
        keyword.addAll(titleword);
        System.out.println(keyword);
        String keywordQuery = String.join("+", keyword);
        google_bookLists = getGoogleBook(keywordQuery);

        return new DetailBookDto(bookLists, google_bookLists);
    }

    public MainBookDto getBookMain() {
        String apikey = apiKeyProperties.getKeys().get("aladin");
        String newBookurl = "http://www.aladin.co.kr/ttb/api/ItemList.aspx?ttbkey=" +
                apikey +
                "&QueryType=ItemNewSpecial&MaxResults=30&start=1&SearchTarget=Book&output=js&Version=20131101";
        String bestBookurl = "http://www.aladin.co.kr/ttb/api/ItemList.aspx?ttbkey=" +
                apikey +
                "&QueryType=Bestseller&MaxResults=30&start=1&SearchTarget=Book&output=js&Version=20131101";

        System.out.println(newBookurl);

        List<BookDto>newBook = getAladinBook(newBookurl);
        List<BookDto>bestBook = getAladinBook(bestBookurl);

        return new MainBookDto(newBook, bestBook);
    }

    public MypageDto getMyInfo(String username){
        String description = "";
        List<BookDto> mybookDto = new ArrayList<>();
        List<BookEntity>mybookEntity = bookRepository.findByUsername(username);
        Map<String, Integer> categoryCount = new HashMap<>();

        for(BookEntity entity : mybookEntity){
            BookDto bookDto = new BookDto();

            bookDto.setBookTitle(entity.getBookTitle());
            bookDto.setBookAuth(entity.getBookAuth());
            bookDto.setBookPub(entity.getBookPub());
            bookDto.setBookPubYear(entity.getBookPubYear());
            bookDto.setISBN(entity.getISBN());
            bookDto.setImg(entity.getImg());
            bookDto.setDiscount(entity.getDiscount());
            bookDto.setDes(entity.getDes());
            bookDto.setLink(entity.getLink());

            mybookDto.add(bookDto);

            String category = entity.getCategory();
            categoryCount.put(category, categoryCount.getOrDefault(category, 0) + 1);
            description = description + entity.getDes();
        }

        String mostCommonCategory = categoryCount.entrySet().stream()
                .filter(entry -> entry.getValue().equals(Collections.max(categoryCount.values())))
                .map(Map.Entry::getKey)
                .sorted()
                .findFirst()
                .orElse(null);

        System.out.println(mostCommonCategory);

        String aladinapikey = apiKeyProperties.getKeys().get("aladin");
        String aladinurl = "https://www.aladin.co.kr/ttb/api/ItemList.aspx?ttbkey="
                + aladinapikey
                + "&QueryType=Bestseller&MaxResults=30&start=1&SearchTarget=Book&output=js&Version=20131101&CategoryID="
                + mostCommonCategory;

        System.out.println(description);
        List<String>keyword = extraction_keyword(description,5);
        String keywordQuery = String.join("+", keyword);
        List<BookDto> google_bookLists = getGoogleBook(keywordQuery);
        List<BookDto>aladinBook = getAladinBook(aladinurl);

        return new MypageDto(mybookDto, aladinBook, google_bookLists);
    }
}
