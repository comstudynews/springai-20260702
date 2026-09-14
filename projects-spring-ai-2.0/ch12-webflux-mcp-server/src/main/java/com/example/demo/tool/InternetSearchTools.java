package com.example.demo.tool;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
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
  @McpTool(description = "인터넷 검색을 합니다. 결과는 제목과 링크를 반환합니다.")
  public Mono<String> search(String query) {
    return webClient.get()
        .uri("https://serpapi.com/search?engine=google&q=%s&api_key=%s"
                .formatted(query, apiKey))
        .header("Accept", "application/json")
        .retrieve()
        .bodyToMono(String.class)
        .map(json -> {
          try {
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
            return sb.toString().trim();
          } catch (Exception e) {
            return "인터넷 검색 중 오류 발생: " + e.getMessage();
          }
        })
        .onErrorReturn("인터넷 검색 중 오류 발생");
  }

  @McpTool(description = "웹 페이지의 본문 텍스트를 반환합니다.")
  public Mono<String> fetch(String url) {
    return webClient.get()
        .uri(url)
        .retrieve()
        .bodyToMono(String.class)
        .map(html -> {
          if (html == null || html.isBlank()) {
            return "페이지 내용을 가져올 수 없습니다.";
          }
          Document doc = Jsoup.parse(html);
          String bodyText = doc.body().text();
          return bodyText.isBlank() ? "본문 텍스트가 비어 있습니다." : bodyText;
        })
        .onErrorReturn("페이지 로딩 중 오류 발생");
  }
}
