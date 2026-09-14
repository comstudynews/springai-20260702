package com.example.demo.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgIndexType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class VectorStoreConfig {

    @Bean
    public VectorStore chatMemoryVectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel embeddingModel) {
        return PgVectorStore.builder(jdbcTemplate, embeddingModel)
            // 테이블 자동 생성
            .initializeSchema(true)
            // 테이블 생성 위치 및 이름
            .schemaName("public")
            .vectorTableName("spring_ai_chat_memory_vector_store")
            // text-embedding-3-large 모델 사용하므로 벡터 차원수를 3072로 설정
            .dimensions(3072)
            // other/sql/ch09-chat_memory_vector_store.sql로 인덱스 수동 생성
            .indexType(PgIndexType.NONE)
            .build();
    }
}
