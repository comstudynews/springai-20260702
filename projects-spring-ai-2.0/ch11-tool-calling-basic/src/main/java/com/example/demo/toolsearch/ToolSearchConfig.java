package com.example.demo.toolsearch;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.tool.toolsearch.ToolIndex;
import org.springframework.ai.tool.toolsearch.index.vectorstore.VectorToolIndex;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgIndexType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import lombok.extern.slf4j.Slf4j;


@Configuration
@Slf4j
public class ToolSearchConfig {
	@Bean
	@Qualifier("toolSearchVectorStore")
	VectorStore toolSearchVectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel embeddingModel) {
		VectorStore vectorStore = PgVectorStore.builder(jdbcTemplate, embeddingModel)
			// 테이블 자동 생성
			.initializeSchema(true)
			// 테이블 생성 위치 및 이름
			.schemaName("public")
			.vectorTableName("spring_ai_tool_search_vector_store")
			// text-embedding-3-large 모델 사용하므로 벡터 차원수를 3072로 설정
			.dimensions(3072)
			// other/sql/ch11-tool_search_vector_store.sql로 인덱스 수동 생성
			.indexType(PgIndexType.NONE)
			.build();
		return vectorStore;		
	}

	@Bean
	VectorToolIndex toolIndex(@Qualifier("toolSearchVectorStore") VectorStore vectorStore) {
		VectorToolIndex toolIndex = new VectorToolIndex(vectorStore);
		return toolIndex;
	}
}
