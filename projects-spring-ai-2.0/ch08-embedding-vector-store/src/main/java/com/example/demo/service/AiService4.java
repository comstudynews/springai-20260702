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
public class AiService4 {
  // ##### 필드 #####
  @Autowired
  private EmbeddingModel embeddingModel;

  @Autowired
  private VectorStore vectorStore;

  // ##### 메소드 #####
  public void deleteDocument() {
    vectorStore.delete("source == '헌법' && year < 1987");
  }
}
