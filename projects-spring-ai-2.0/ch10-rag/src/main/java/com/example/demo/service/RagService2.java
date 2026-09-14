package com.example.demo.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.preretrieval.query.expansion.MultiQueryExpander;
import org.springframework.ai.rag.preretrieval.query.transformation.CompressionQueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.TranslationQueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RagService2 {
  // ##### 필드 #####
  private ChatClient chatClient;
  @Autowired
  private ChatModel chatModel;
  @Autowired
  private VectorStore vectorStore;
  @Autowired
  private ChatMemory chatMemory;

  // ##### 생성자 #####
  public RagService2(ChatClient.Builder chatClientBuilder) {
    this.chatClient = chatClientBuilder
      .defaultAdvisors(
          new SimpleLoggerAdvisor(Ordered.LOWEST_PRECEDENCE - 1)
      )
      .build();
  }
  
  // ##############################################################################
  // VectorStoreDocumentRetriever 생성
  private VectorStoreDocumentRetriever createVectorStoreDocumentRetriever(
    double score, String source) {
    VectorStoreDocumentRetriever vectorStoreDocumentRetriever = 
        VectorStoreDocumentRetriever.builder()
            .vectorStore(vectorStore)
            .similarityThreshold(score)
            .topK(3)
            .filterExpression(() -> {
                FilterExpressionBuilder builder = new FilterExpressionBuilder();
                if (StringUtils.hasText(source)) {
                  return builder.eq("source", source).build();
                } else {
                  return null;
                }
            })
            .build();
    return vectorStoreDocumentRetriever;
  }
  
  // ##############################################################################
  // CompressionQueryTransformer를 생성
  private CompressionQueryTransformer createCompressionQueryTransformer() {
    CompressionQueryTransformer compressionQueryTransformer = 
        CompressionQueryTransformer.builder()
            .chatClientBuilder(ChatClient.builder(chatModel)
                .defaultAdvisors(
                    new SimpleLoggerAdvisor(Ordered.LOWEST_PRECEDENCE)    
                )
            )
            .build();
    return compressionQueryTransformer;
  }


  //RAG 수행 메소드
	public String chatWithCompression(String question, double score, 
			String source, String conversationId) {
	  String answer = this.chatClient.prompt()
	      .user(question)
	      .advisors(
	      		MessageChatMemoryAdvisor.builder(chatMemory)
	        		.order(Ordered.HIGHEST_PRECEDENCE)
	        		.build(), 
	        
	        	RetrievalAugmentationAdvisor.builder()
	        		.queryTransformers(createCompressionQueryTransformer())
	        		.documentRetriever(createVectorStoreDocumentRetriever(score, source))
	        		.build()
	      )
	      .advisors(advisorSpec -> advisorSpec.param(
	          ChatMemory.CONVERSATION_ID, conversationId))
	      .call()
	      .content();
	  return answer;
	}

  // ##############################################################################
  // RewriteQueryTransformer 생성
	private RewriteQueryTransformer createRewriteQueryTransformer() {
	  RewriteQueryTransformer rewriteQueryTransformer = 
	      RewriteQueryTransformer.builder()
	          .chatClientBuilder(ChatClient.builder(chatModel)
	              .defaultAdvisors(
	                  new SimpleLoggerAdvisor(Ordered.LOWEST_PRECEDENCE)    
	              ))
	          .build();
	
	  return rewriteQueryTransformer;
	}

  // RAG 수행 메소드
	public String chatWithRewriteQuery(String question, double score, String source) {
	  String answer = this.chatClient.prompt()
	      .user(question)
	      .advisors(
	      		RetrievalAugmentationAdvisor.builder()
	            .queryTransformers(createRewriteQueryTransformer())
	            .documentRetriever(createVectorStoreDocumentRetriever(score, source))
	            .build()
				)
	      .call()
	      .content();
	  return answer;
	}  

  // ##############################################################################
  // TranslationQueryTransformer 생성
	private TranslationQueryTransformer createTranslationQueryTransformer() {
	  TranslationQueryTransformer translationQueryTransformer = 
	      TranslationQueryTransformer.builder()
	          .chatClientBuilder(ChatClient.builder(chatModel)
	              .defaultAdvisors(
	                  new SimpleLoggerAdvisor(Ordered.LOWEST_PRECEDENCE)
	              ))
	          .targetLanguage("korean")
	          .build();
	  return translationQueryTransformer;
	}

  // RAG 수행 메소드
	public String chatWithTranslation(String question, double score, String source) {
	  String answer = this.chatClient.prompt()
	      .user(question)
	      .advisors(
	      		RetrievalAugmentationAdvisor.builder()
	            .queryTransformers(createTranslationQueryTransformer())
	            .documentRetriever(createVectorStoreDocumentRetriever(score, source))
	            .build()
	      )
	      .call()
	      .content();
	  return answer;
	}

  //##############################################################################
  // MultiQueryExpander 생성
	private MultiQueryExpander createMultiQueryExpander() {
	  MultiQueryExpander multiQueryExpander = 
	      MultiQueryExpander.builder()
	          .chatClientBuilder(ChatClient.builder(chatModel)
	              .defaultAdvisors(
	                  new SimpleLoggerAdvisor(Ordered.LOWEST_PRECEDENCE)
	              ))
	          .includeOriginal(true)
	          .numberOfQueries(3)
	          .build();
	  return multiQueryExpander;
	}

  // ##### LLM과 대화하는 메소드 #####
	public String chatWithMultiQuery(String question, double score, String source) {
	  String answer = this.chatClient.prompt()
	      .user(question)
	      .advisors(
	      		RetrievalAugmentationAdvisor.builder()
	            .queryExpander(createMultiQueryExpander())
	            .documentRetriever(createVectorStoreDocumentRetriever(score, source))
	            .build()
	      )
	      .call()
	      .content();
	  return answer;
	}
}
