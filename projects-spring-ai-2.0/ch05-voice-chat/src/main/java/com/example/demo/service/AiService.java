package com.example.demo.service;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.ai.audio.transcription.AudioTranscriptionOptions;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.audio.transcription.TranscriptionModel;
import org.springframework.ai.audio.tts.TextToSpeechModel;
import org.springframework.ai.audio.tts.TextToSpeechOptions;
import org.springframework.ai.audio.tts.TextToSpeechPrompt;
import org.springframework.ai.audio.tts.TextToSpeechResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.content.Media;
import org.springframework.ai.openai.OpenAiAudioSpeechOptions;
import org.springframework.ai.openai.OpenAiAudioTranscriptionOptions;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.OpenAiChatOptions.AudioParameters;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Service
@Slf4j
public class AiService {
  // ##### 필드 #####
  private ChatClient chatClient;
  private TranscriptionModel transcriptionModel;
  private TextToSpeechModel textToSpeechModel;

  // ##### 생성자 #####
  public AiService(ChatClient.Builder chatClientBuilder,
      TranscriptionModel transcriptionModel,
      TextToSpeechModel textToSpeechModel) {
    chatClient = chatClientBuilder.build();
    this.transcriptionModel = transcriptionModel;
    this.textToSpeechModel = textToSpeechModel;
  }

  // ##### 메소드 #####
  public String stt(String fileName, byte[] bytes) {
  	// 음성 데이터(byte[])를 ByteArrayResource로 생성
    Resource audioResource = new ByteArrayResource(bytes) {
      @Override
      public String getFilename() {
        return fileName; // 파일 확장명으로 오디오 형식을 전달. 확장자를 포함한 파일명 제공
      }
    };

    // 모델 옵션 설정
    AudioTranscriptionOptions transcriptionOptions = OpenAiAudioTranscriptionOptions.builder()
        .model("whisper-1") // 기본 STT 모델: whisper-1
        .language("ko") 		// 입력 음성 언어의 종류 설정, 출력 언어에도 영향을 미침
        .build();

    // 프롬프트 생성
    AudioTranscriptionPrompt prompt = new AudioTranscriptionPrompt(audioResource, transcriptionOptions);

    // 모델을 호출하고 응답받기
    AudioTranscriptionResponse response = transcriptionModel.call(prompt);
    String text = response.getResult().getOutput();

    return text;
  }

  public byte[] tts(String text) {
    // 모델 옵션 설정
  	TextToSpeechOptions textToSpeechOptions = OpenAiAudioSpeechOptions.builder()
        .model("gpt-4o-mini-tts") // 기본 TTS 모델: gpt-4o-mini-tts
        .voice(OpenAiAudioSpeechOptions.Voice.ALLOY)
        .responseFormat(OpenAiAudioSpeechOptions.AudioResponseFormat.MP3)
        .speed(1.0)
        .build();

    // 프롬프트 생성
    TextToSpeechPrompt prompt = new TextToSpeechPrompt(text, textToSpeechOptions);

    // 모델을 호출하고 응답받기
    TextToSpeechResponse response = textToSpeechModel.call(prompt);
    byte[] bytes = response.getResult().getOutput();

    return bytes;
  }    

  // 텍스트도 같이 출력되는 음성 대화
  public Map<String, String> chatText(String question) {
    // LLM로 요청하고, 텍스트 응답 얻기
    String textAnswer = chatClient.prompt()
        .system("50자 이내로 한국어로 답변해주세요.")
        .user(question)
        .call()
        .content();

    // TTS 모델로 요청하고 응답으로 받은 음성 데이터를 base64 문자열로 변환
    byte[] audio = tts(textAnswer);
    String base64Audio = Base64.getEncoder().encodeToString(audio);

    // 텍스트 답변과 음성 답변을 Map에 저장하고 반환
    Map<String, String> response = new HashMap<>();
    response.put("text", textAnswer);
    response.put("audio", base64Audio);
    return response;
  }
  
  public Flux<byte[]> ttsFlux(String text) {
    // 모델 옵션 설정
    TextToSpeechOptions options = OpenAiAudioSpeechOptions.builder()
        .model("gpt-4o-mini-tts")
        .voice(OpenAiAudioSpeechOptions.Voice.ALLOY)
        .responseFormat(OpenAiAudioSpeechOptions.AudioResponseFormat.MP3)
        .speed(1.0)
        .build();

    // 프롬프트 생성
    TextToSpeechPrompt prompt = new TextToSpeechPrompt(text, options);

    // 모델로 요청하고 응답받기
    Flux<TextToSpeechResponse> response = textToSpeechModel.stream(prompt);
    Flux<byte[]> flux = response.map(speechResponse -> speechResponse.getResult().getOutput());
    return flux;
  }   
  
  // 순수 음성 대화 구현 (방법1)
  public Flux<byte[]> chatVoiceSttLlmTts(byte[] audioBytes) {
    // STT를 이용해서 음성 질문을 텍스트 질문으로 변환
    String textQuestion = stt("speech.mp3", audioBytes);

    // 텍스트 질문으로 LLM에 요청하고, 텍스트 응답 얻기
    String textAnswer = chatClient.prompt()
        .system("100자 이내로 답변해주세요.")
        .user(textQuestion)
        .call()
        .content();

    // TTS를 이용해서 비동기 음성 데이터 얻기
    Flux<byte[]> flux = ttsFlux(textAnswer);
    return flux;
  }   

  //순수 음성 대화 구현 (방법2)
  public byte[] chatVoiceOneModel(byte[] audioBytes, String mimeType) throws Exception {
    // 음성 데이터를 Resource로 생성
    Resource resource = new ByteArrayResource(audioBytes);

    // 사용자 메시지 생성
    UserMessage userMessage = UserMessage.builder()
        // 빈문자열이라도 제공해야함
        .text("제공되는 음성에 맞는 자연스러운 대화로 이어주세요.")
        .media(new Media(MimeType.valueOf(mimeType), resource))
        .build();

    // 모델 옵션 설정
    OpenAiChatOptions.Builder chatOptionsBuilder = OpenAiChatOptions.builder()
        .model("gpt-audio-1.5") 
        .outputModalities(List.of("text", "audio"))
        .outputAudio(new AudioParameters(
        		OpenAiChatOptions.AudioParameters.Voice.ALLOY,
            OpenAiChatOptions.AudioParameters.AudioResponseFormat.MP3));

    // Spring AI에서는 대화 모델에서 오디오 스트림을 지원하지 않으므로 동기 방식 사용
    // 모델로 요청하고 응답 받기
    ChatResponse response = chatClient.prompt()
        .system("100자 이내로 답변해주세요.")
        .messages(userMessage)
        .options(chatOptionsBuilder)
        .call()
        .chatResponse();
    
    // AI 메시지 얻기
    AssistantMessage assistantMessage = response.getResult().getOutput();
    
    // 텍스트 답변 얻기
    String textAnswer = assistantMessage.getText();
    log.info("텍스트 응답: {}", textAnswer);

    // 오디오 답변 얻기
    byte[] audioAnswer = assistantMessage.getMedia().get(0).getDataAsByteArray();

    return audioAnswer;
  }  
}
