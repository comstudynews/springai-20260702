package com.example.demo.tool;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.ai.mcp.annotation.context.McpSyncRequestContext;
import org.springframework.ai.mcp.annotation.context.StructuredElicitResult;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class FileSystemTools {
  // ##### 필드 #####
  private final Path rootDirectory;

  // ##### 생성자 #####
  public FileSystemTools() {
    Path home = Paths.get(System.getProperty("user.home"));
    this.rootDirectory = home.resolve("Documents/ch11-tool-calling");
    try {
      Files.createDirectories(rootDirectory);
    } catch (IOException e) {
      throw new RuntimeException("루트 디렉토리 생성 실패: " + rootDirectory, e);
    }
  }

  // ##### 메소드 #####
  private Path resolve(String relativePath) {
    Path path = null;
    if (!StringUtils.hasText(relativePath)) {
      path = rootDirectory;
    }

    path = rootDirectory.resolve(relativePath).normalize();

    if (!path.startsWith(rootDirectory)) {
      path = rootDirectory;
    }

    return path;
  }

  // ##### 도구 #####
  @McpTool(description = "디렉토리 항목 조회")
  public List<Item> listFiles(String relativePath) {
    Path path = resolve(relativePath);
    try {
      Stream<Path> stream = Files.list(path);
      List<Item> list = stream.map(p -> {
        return new Item(p, Files.isDirectory(p));
      })
      .toList();
      stream.close();
      return list;
    } catch(Exception e) {
      log.info(e.toString());
      return new ArrayList<>();
    }
  }

  public record Item(Path path, boolean isDirectory) {
  } 

  @McpTool(description = "디렉토리 생성")
  public String createDir(String relativePath) {
    Path path = resolve(relativePath);
    try {
      Files.createDirectories(path);
      return "디렉토리를 생성했습니다.";
    } catch(Exception e) {
      log.info(e.toString());
      return "디렉토리를 생성할 수 없습니다.";
    }
  }

  @McpTool(description = "파일 생성")
  public String createFile(
    @McpToolParam(description = "부모 디렉토리") String parentPath, 
    @McpToolParam(description = "파일 이름") String fileName, 
    @McpToolParam(description = "확장 이름") String extName, 
    @McpToolParam(description = "파일 내용") String content
  ) {
    if(!StringUtils.hasText(fileName) ||
      !StringUtils.hasText(extName)) {
      return "디렉토리 또는 파일명이 없습니다.";
    }
    if(!StringUtils.hasText(content)) {
      content = "";
    }
    Path path = resolve(parentPath);
    if(!fileName.endsWith("." + extName)) {
      path = path.resolve(fileName + "." + extName);
    } else {
      path = path.resolve(fileName);
    }
    try {
      Files.writeString(path, content, StandardCharsets.UTF_8);
      return "파일을 생성했습니다.";
    } catch(Exception e) {
      log.info(e.toString());
      return "파일 생성에 실패했습니다.";
    }
  }

  @McpTool(description = "파일 내용 읽기")
  public String readFile(String relativePath) {
    Path path = resolve(relativePath);
    if (Files.notExists(path)) {
      log.info(path.toString());
      return "파일이 존재하지 않습니다.";
    }
    try {
      String content = Files.readString(path, StandardCharsets.UTF_8);
      return content;
    } catch(Exception e) {
      log.info(e.toString());
      return "파일 내용을 읽을 수 없습니다.";
    }
  }

  @McpTool(description = "파일 및 디렉토리 삭제")
  public String deletePath(McpSyncRequestContext context, 
    @McpToolParam(description = "삭제할 파일 또는 디렉토리의 상대 경로") String relativePath) {
    // 상대 경로를 절대 경로로 변환하고 존재 여부 확인
    Path path = resolve(relativePath);
    if (Files.notExists(path))
      return "파일 또는 디렉토리가 존재하지 않습니다.";

    // @McpElicitation 사용 예제: 삭제 여부를 사용자에게 확인하는 Elicitation 요청 #################
    // MCP Client가 Elicitation 기능을 지원하지 않으면 삭제하지 못하도록 함
    if (!context.elicitEnabled()) {
      return "MCP Client가 Elicitation 기능을 지원하지 않아 삭제할 수 없습니다.";
    }

    // ToolContext로 taskId가 전달되었는지 확인
    Object taskIdValue = context.requestMeta().get("taskId");
    log.info("taskId: {}", taskIdValue);
    if (!(taskIdValue instanceof String taskId) || taskId.isBlank()) {
      return "브라우저 작업 식별 ID가 없어 삭제할 수 없습니다.";
    }

    // 삭제 승인 결과를 담을 record 정의
    record DeleteApproval(boolean approved) {}

    // 사용자에게 삭제 승인을 요청하고 응답을 기다림
    StructuredElicitResult<DeleteApproval> structuredElicitResult = context.elicit(
      elicitationSpec -> elicitationSpec
          .message(
              "'%s' 파일 또는 디렉토리를 삭제하시겠습니까?".formatted(relativePath)
          )
          .meta("taskId", taskId),
      DeleteApproval.class
    );

    // 사용자가 삭제를 거부하면 삭제하지 않음
    DeleteApproval approval = structuredElicitResult.structuredContent();
    // ElicitResult(Action.DECLINE, null)이면 structuredContent()는 null을 반환
    if (approval == null || !approval.approved()) {
      return "사용자가 파일 삭제를 거부했습니다.";
    }
    // #######################################################################################

    // 파일 또는 디렉토리 삭제
    try {
      Stream<Path> stream = Files.walk(path);
      stream
        .sorted(Comparator.reverseOrder()) // 자식 → 부모 순서로 삭제
        .forEach(p -> {
          try {
            Files.delete(p);
          } catch (IOException ignored) {
          }
        });
      stream.close();
      return "성공적으로 파일을 삭제했습니다.";
    } catch(Exception e) {
      log.info(e.toString());
      return "파일 또는 디렉토리를 삭제하지 못했습니다.";
    }
  }

  @McpTool(description = "파일 이동 또는 이름 변경")
  public String moveFile(String sourceRelativePath, String targetRelativePath) {
    Path source = resolve(sourceRelativePath);
    Path target = resolve(targetRelativePath);
    try {
      Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
      return "파일 이동 또는 이름 변경을 했습니다.";
    } catch(Exception e) {
      log.info(e.toString());
      return "파일 이동 또는 이름 변경을 못했습니다.";
    }
  }

}
