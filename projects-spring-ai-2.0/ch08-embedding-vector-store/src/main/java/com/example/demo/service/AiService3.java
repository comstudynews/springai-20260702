package com.example.demo.service;

import java.util.List;
import java.util.Map;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.embedding.EmbeddingResponseMetadata;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AiService3 {
  // ##### 필드 #####
  @Autowired
  private EmbeddingModel embeddingModel;

  @Autowired
  private VectorStore vectorStore;

  // ##### 메소드 #####
  public List<Document> searchDocument1(String question) {
    List<Document> documents = vectorStore.similaritySearch(question);
    return documents;
  }

  public List<Document> searchDocument2(String question) {
    List<Document> documents = vectorStore.similaritySearch(
        SearchRequest.builder()
            .query(question)
            .topK(1)
            .similarityThreshold(0.3)
            .filterExpression("source == '헌법' && year >= 1987")
            .build());
    return documents;
  }

  // public List<Document> searchDocument2(String question) {
  //   FilterExpressionBuilder feb = new FilterExpressionBuilder();

  //   List<Document> documents = vectorStore.similaritySearch(
  //       SearchRequest.builder()
  //           .query(question)
  //           .topK(1)
  //           .similarityThreshold(0.4)
  //           .filterExpression(feb
  //               .and(
  //                   feb.eq("source", "헌법"),
  //                   feb.gte("year", 1987))
  //               .build())
  //           .build());
  //   return documents;
  // }
}
