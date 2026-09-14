package com.example.demo.internetsearch;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

//@Component
@Slf4j
public class InternetSearchTools {
  // ##### 필드 #####
  private String apiKey;
  private WebClient webClient;
  private ObjectMapper objectMapper = new ObjectMapper();

  // ##### 생성자 #####
  public InternetSearchTools(      
      @Value("${serpapi.api-key:my-key}") String apiKey,
      WebClient.Builder webClientBuilder
  ) {
    this.apiKey = apiKey;
    this.webClient = webClientBuilder.build();
  }

  // ##### 도구 #####
  @Tool(description = "인터넷 검색을 합니다. 제목, 링크, 요약을 문자열로 반환합니다.")
  public String search(String query) {
    log.info("search() 호출됨. query={}", query);
    try {
      String json = webClient.get()
          .uri("https://serpapi.com/search?engine=google&q=%s&api_key=%s"
                  .formatted(query, apiKey))
          .header("Accept", "application/json")
          .retrieve()
          .bodyToMono(String.class)
          .block();
      // log.info("응답본문: {}", json);

      JsonNode root = objectMapper.readTree(json);
      JsonNode organicResults = root.path("organic_results");

      if (!organicResults.isArray() || organicResults.isEmpty()) {
        return "검색 결과가 없습니다.";
      }

      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < Math.min(3, organicResults.size()); i++) {
        JsonNode result = organicResults.get(i);
        String title = result.path("title").asString();
        String link = result.path("link").asString();
        String snippet = result.path("snippet").asString();
        sb.append(String.format("%d. %s\n%s\n%s\n\n", i + 1, title, link, snippet));
      }
      // log.info(sb.toString().trim());
      return sb.toString().trim();

    } catch (Exception e) {
      return "인터넷 검색 중 오류 발생: " + e.getMessage();
    }
  }

  @Tool(description = "URL의 웹 페이지에 접속하여 HTML을 파싱한 후 본문 텍스트만 추출하여 반환합니다.")
  public String fetch(String url) {
    log.info("fetch() 호출됨. url={}", url);
    try {
      String html = webClient.get()
          .uri(url)
          .retrieve()
          .bodyToMono(String.class)
          .block();

      if (html == null || html.isBlank()) {
        return "페이지 내용을 가져올 수 없습니다.";
      }

      // Jsoup으로 파싱하고 <body> 내부 텍스트 추출
      Document doc = Jsoup.parse(html);
      String bodyText = doc.body().text();

      return bodyText.isBlank() ? "본문 텍스트가 비어 있습니다." : bodyText;

    } catch (Exception e) {
      return "페이지 로딩 중 오류 발생: " + e.getMessage();
    }
  }  
}
