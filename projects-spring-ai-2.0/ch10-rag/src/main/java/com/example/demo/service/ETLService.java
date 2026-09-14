package com.example.demo.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.document.DocumentTransformer;
import org.springframework.ai.model.transformer.KeywordMetadataEnricher;
import org.springframework.ai.reader.JsonMetadataGenerator;
import org.springframework.ai.reader.JsonReader;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.reader.jsoup.JsoupDocumentReader;
import org.springframework.ai.reader.jsoup.config.JsoupDocumentReaderConfig;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.knuddels.jtokkit.api.EncodingType;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ETLService {
  // ##### 필드 #####
  private VectorStore vectorStore;

  // ##### 생성자 #####
  public ETLService(VectorStore vectorStore) {
    this.vectorStore = vectorStore;
  }
  
  // ##### 업로드된 파일을 가지고 ETL 과정을 처리하는 메소드 #####
  public String etlFromFile(String title, String author,
      MultipartFile attach) throws IOException {

    // 추출하기
    List<Document> documents = extractFromFile(attach);
    if (documents == null) {
      return ".txt, .pdf, .doc, .docx 파일 중에 하나를 올려주세요.";
    }
    log.info("추출된 Document 수: {} 개", documents.size());

    // 메타데이터에 공통 정보 추가하기
    for (Document doc : documents) {
      Map<String, Object> metadata = doc.getMetadata();
      metadata.putAll(Map.of(
          "title", title,
          "author", author,
          "source", attach.getOriginalFilename()));
    }

    // T: 작은 사이즈로 분할하기
    documents = transform(documents);
    log.info("변환된 Document 수: {} 개", documents.size());

    // L: 적재하기
    vectorStore.add(documents);

    return "올린 문서를 추출-변환-적재 완료 했습니다.";
  }  
  
	// ##### 업로드된 파일로부터 텍스트를 추출하는 메소드 #####
	private List<Document> extractFromFile(MultipartFile attach) throws IOException {
	  // 바이트 배열을 Resource로 생성
	  Resource resource = new ByteArrayResource(attach.getBytes());
	  
	  // 파일 확장자 추출
	  String originalFilename = attach.getOriginalFilename();
	  String ext = "";
	  if (originalFilename != null && originalFilename.contains(".")) {
	      ext = originalFilename
	      				.substring(originalFilename.lastIndexOf('.') + 1)
	      				.toLowerCase();
	  }
	
	  // 확장자에 따라 적절한 DocumentReader를 사용하여 텍스트 추출
	  List<Document> documents = null;
	  if (ext.equals("txt")) {
	    // Text(.txt) 파일일 경우
	    DocumentReader reader = new TextReader(resource);
	    documents = reader.read();
	  } else if (ext.equals("pdf")) {
	    // PDF(.pdf) 파일일 경우
	    DocumentReader reader = new PagePdfDocumentReader(resource);
	    documents = reader.read();
	  } else if (ext.equals("doc") || ext.equals("docx")) {
	    // Word(.doc, .docx) 파일일 경우
	    DocumentReader reader = new TikaDocumentReader(resource);
	    documents = reader.read();
	  }
	
	  return documents;
	}

  // ##### 작은 크기로 분할하고 키워드 메타데이터를 추가하는 메소드 #####
	private List<Document> transform(List<Document> documents) {
	  List<Document> transformedDocuments = null;
	
	  // TokenTextSplitter 생성
		TokenTextSplitter tokenTextSplitter = TokenTextSplitter.builder()
				.withEncodingType(EncodingType.CL100K_BASE)
				.withChunkSize(800)
				.withMinChunkSizeChars(350)
				.withMinChunkLengthToEmbed(5)
				.withMaxNumChunks(10000)
				.withKeepSeparator(true)
				.withPunctuationMarks(List.of('.', '!', '?', '\n'))
				.build();
	  
	  // 청크로 분할된 List<Document>를 반환
	  transformedDocuments = tokenTextSplitter.apply(documents);
	  return transformedDocuments;
	}  

  // ##### HTML의 ETL 과정을 처리하는 메소드 #####
  public String etlFromHtml(String title, String author, String url) throws Exception {
    // URL로부터 Resource 얻기
    Resource resource = new UrlResource(url);

    // E: 추출하기
    JsoupDocumentReader reader = new JsoupDocumentReader(
        resource,
        JsoupDocumentReaderConfig.builder()
            .charset("UTF-8")
            //쉼표로 구분해서 여러개의 셀렉터 추가 가능
            .selector("#content")
            // <title> 태그의 내용은 자동 메타데이터로 추가됨
            // <meta name="author" content="홍길동"> 태그에서 자동 추출해서 메타데이터로 추가
            .metadataTag("author")
            // 직접 메타데이터로 추가
            .additionalMetadata(Map.of(
            		// "title", title,
            		// "author", author,
            		"url", url))
            .build());
    List<Document> documents = reader.read();
    log.info("추출된 Document 수: {} 개", documents.size());

    // T: 변환하기
    List<Document> transformedDocuments = transform(documents);
    log.info("변환된 Document 수: {} 개", transformedDocuments.size());

    // L: 적재하기
    vectorStore.add(transformedDocuments);

    return "HTML에서 추출-변환-적재 완료 했습니다.";
  }

  // ##### JSON의 ETL 과정을 처리하는 메소드 #####
  public String etlFromJson(String url) throws Exception {
    // URL로부터 Resource 얻기
    Resource resource = new UrlResource(url);

    // E: 추출하기
    JsonReader reader = new JsonReader(
        resource,
        
        // JsonMetadataGenerator: JSON에서 추출한 데이터를 가지고 메타데이터를 생성
        jsonMap -> Map.of(
            "title", jsonMap.get("title"),
            "author", jsonMap.get("author"),
            "url", "http://localhost:8080/document/constitution(19880225).json"
        ),

        // 텍스트로 합칠 필드 지정
        "date", "content"
    );
    
    List<Document> documents = reader.read();
    log.info("추출된 Document 수: {} 개", documents.size());

    // T: 변환하기
    List<Document> transformedDocuments = transform(documents);
    log.info("변환된 Document 수: {} 개", transformedDocuments.size());

    // L: 적재하기
    vectorStore.add(transformedDocuments);

    return "JSON에서 추출-변환-적재 완료 했습니다.";
  }
}
