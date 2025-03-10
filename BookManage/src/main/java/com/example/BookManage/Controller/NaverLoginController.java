package com.example.BookManage.Controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

@Controller
public class NaverLoginController {

    @Value("${naver.client.id}")
    private String clientId;

    @Value("${naver.client.secret}")
    private String clientSecret;

    @Value("${naver.redirect.uri}")
    private String redirectUri;

    @GetMapping("/naver/login")
    public String naverLogin(HttpSession session) {
        String state = UUID.randomUUID().toString(); // 무작위 state 값 생성
        session.setAttribute("oauth_state", state); // 세션에 저장

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

        // 세션에 저장된 state와 비교
        String storedState = (String) session.getAttribute("oauth_state");
        if (storedState == null || !storedState.equals(state)) {
            System.out.println("잘못된 요청입니다." + storedState);
            return "redirect:/";  // 잘못된 요청인 경우, 리다이렉트 처리
        }

        // 타임아웃이 설정된 RestTemplate 사용
        RestTemplate restTemplate = restTemplateWithTimeout();

        // 토큰 요청 URL
        String tokenUrl = "https://nid.naver.com/oauth2.0/token";

        // 요청 파라미터 설정
        String params = "grant_type=authorization_code"
                + "&client_id=" + clientId
                + "&client_secret=" + clientSecret
                + "&code=" + code
                + "&state=" + state;

        // 헤더 설정 (Content-Type을 application/x-www-form-urlencoded로 설정)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // HttpEntity 객체에 요청 파라미터와 헤더 설정
        HttpEntity<String> entity = new HttpEntity<>(params, headers);

        int retryCount = 3; // 재시도 횟수
        while (retryCount > 0) {
            try {
                // POST 요청으로 토큰 받기
                Map<String, Object> tokenResponse = restTemplate.postForObject(tokenUrl, entity, Map.class);

                // 받은 토큰 출력 (디버깅용)
                System.out.println("tokenResponse: " + tokenResponse);

                // access_token 추출
                String accessToken = (String) tokenResponse.get("access_token");

                // 프로필 조회 URL
                String profileUrl = "https://openapi.naver.com/v1/nid/me";

                // Authorization 헤더에 accessToken 추가
                headers.add("Authorization", "Bearer " + accessToken);

                // 프로필 조회 요청 보내기
                HttpEntity<String> profileEntity = new HttpEntity<>("", headers);
                Map<String, Object> profileResponse = restTemplate.exchange(profileUrl, HttpMethod.GET, profileEntity, Map.class).getBody();

                // 프로필에서 닉네임 정보 추출
                Map<String, String> response = (Map<String, String>) profileResponse.get("response");
                String nickname = response.get("nickname");

                // 세션에 닉네임 저장
                session.setAttribute("nickname", nickname);

                // 홈으로 리다이렉트
                return "redirect:/";

            } catch (ResourceAccessException e) {
                retryCount--;
                if (retryCount == 0) {
                    System.out.println("최대 재시도 횟수에 도달했습니다.");
                    return "redirect:/"; // 재시도 실패 후 리다이렉트
                }
                System.out.println("재시도 중... 남은 시도 횟수: " + retryCount);
                try {
                    Thread.sleep(2000); // 2초 대기 후 재시도
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
                return "redirect:/";
            }
        }

        return "redirect:/"; // 재시도 후에도 실패한 경우 리다이렉트
    }



    @GetMapping("/logout")
    public String logout(HttpSession session, HttpServletRequest request){
        session.removeAttribute("nickname");
        String referer = request.getHeader("Referer");

        return "redirect:" + (referer != null ? referer : "/");
    }

    public RestTemplate restTemplateWithTimeout() {
        // 타임아웃 설정 (단위: 밀리초)
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);  // 연결 타임아웃 5초
        factory.setReadTimeout(5000);     // 읽기 타임아웃 5초

        return new RestTemplate(factory);
    }
}
