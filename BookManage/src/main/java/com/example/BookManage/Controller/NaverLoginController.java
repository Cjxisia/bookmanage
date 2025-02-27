package com.example.BookManage.Controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Controller
public class NaverLoginController {

    @Value("${naver.client.id}")
    private String clientId;

    @Value("${naver.client.secret}")
    private String clientSecret;

    @Value("${naver.redirect.uri}")
    private String redirectUri;

    private final String state = "RANDOM_STATE"; // CSRF 방지용 state

    @GetMapping("/naver/login")
    public String naverLogin() {
        String naverAuthUrl = "https://nid.naver.com/oauth2.0/authorize?response_type=code"
                + "&client_id=" + clientId
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                + "&state=" + state;
        return "redirect:" + naverAuthUrl;
    }

    @GetMapping("/naver/callback")
    public String naverCallback(@RequestParam("code") String code,
                                @RequestParam("state") String state,
                                HttpSession session) {  // 세션 추가

        String tokenUrl = "https://nid.naver.com/oauth2.0/token?grant_type=authorization_code"
                + "&client_id=" + clientId
                + "&client_secret=" + clientSecret
                + "&code=" + code
                + "&state=" + state;

        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> tokenResponse = restTemplate.getForObject(tokenUrl, Map.class);
        String accessToken = (String) tokenResponse.get("access_token");

        String profileUrl = "https://openapi.naver.com/v1/nid/me";
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);

        org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>("", headers);
        Map<String, Object> profileResponse = restTemplate.postForObject(profileUrl, entity, Map.class);

        Map<String, String> response = (Map<String, String>) profileResponse.get("response");
        String nickname = response.get("nickname");

        session.setAttribute("nickname", nickname);

        return "redirect:/";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, HttpServletRequest request){
        session.removeAttribute("nickname");
        String referer = request.getHeader("Referer");

        return "redirect:" + (referer != null ? referer : "/");
    }
}
