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
public class AiService1 {
  // ##### 필드 #####
  @Autowired
  private EmbeddingModel embeddingModel;

  // ##### 메소드 #####
  public void textEmbedding(String question) {
    // 임베딩하기
    EmbeddingResponse response = embeddingModel.embedForResponse(List.of(question));

    // 임베딩 결과 얻기
    Embedding embedding = response.getResults().get(0);
    log.info("벡터: {}", embedding.getOutput());  
    log.info("벡터 차원: {}", embedding.getOutput().length);
    
    // 메타데이터 정보 얻기
    EmbeddingResponseMetadata metadata = response.getMetadata();
    log.info("임베딩 모델명: {}", metadata.getModel());
    log.info("사용된 토큰수: {}", metadata.getUsage().getTotalTokens());
  }

  // public void textEmbedding(String question) {
  //   float[] vector = embeddingModel.embed(question);
  //   log.info("벡터: {}", vector);
  // }
}
