package com.example.demo.toolsearch;

import java.io.IOException;

import org.springframework.ai.openai.http.okhttp.OpenAiHttpClientBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;

@Configuration
public class HttpCaptureLogger {
  //////////////////////////////////////////////////////////////////////////
  // ToolSearchService 테스트할 때 true로 바꾸면 HTTP 요청/응답 로그를 콘솔에 출력
  public static boolean isPrint = false;
  //////////////////////////////////////////////////////////////////////////

  // Spring AI 2.0은 OkHttp 기반 OpenAI Java SDK를 사용하므로 OkHttp 인터셉터로 등록
  @Bean
  public OpenAiHttpClientBuilderCustomizer httpLoggingCustomizer() {
    return builder -> builder.interceptor(new LoggingInterceptor());
  }

  private class LoggingInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
      Request request = chain.request();
      logRequest(request);
      Response response = chain.proceed(request);
      return logResponse(response);
    }

    private void logRequest(Request request) throws IOException {
      if (!isPrint) return;
      System.out.println("\n\u001B[31m[요청 HTTP 시작]>>>>>>>>>>>>>>>>>>>>>>>\u001B[0m");
      System.out.println(request.method() + " " + request.url());
      System.out.println(request.headers());
      RequestBody body = request.body();
      if (body != null) {
        Buffer buffer = new Buffer();
        body.writeTo(buffer);
        System.out.println(buffer.readUtf8().replace("\\r\\n", System.lineSeparator()));
      }
      System.out.println("\u001B[31m[요청 HTTP 맨끝]>>>>>>>>>>>>>>>>>>>>>>>\u001B[0m\n");
    }

    private Response logResponse(Response response) throws IOException {
      if (!isPrint) return response;
      System.out.println("\n\u001B[34m[응답 HTTP 시작]<<<<<<<<<<<<<<<<<<<<<<<\u001B[0m");
      System.out.println(response.code());
      System.out.println(response.headers());
      ResponseBody peekBody = response.peekBody(Long.MAX_VALUE);
      String bodyStr = peekBody.string();
      if (bodyStr.length() > 600) {
        bodyStr = bodyStr.substring(0, 600) + "\n...";
      }
      System.out.println(bodyStr);
      System.out.println("\u001B[34m[응답 HTTP 맨끝]<<<<<<<<<<<<<<<<<<<<<<\u001B[0m\n");
      return response;
    }
  }
}
