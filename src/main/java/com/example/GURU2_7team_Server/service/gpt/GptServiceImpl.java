package com.example.GURU2_7team_Server.service.gpt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
public class GptServiceImpl implements GptService {

    @Value("${openai.api.key}")
    private String apiKey;

    // gpt 사용 상황 설정
    private String systemMsg =
            "I'm going to use it as a translator to interpret sentences containing new words or memes used by the younger generation called the Mz generation these days.";

    public JsonNode callChatGpt(String question) throws JsonProcessingException {
        final String url = "https://api.openai.com/v1/chat/completions";

        String script = question
                    + " 뭐야?"
                    + "위의 문장은 요즘 mz세대 / 알파세대라고 불리는 젊은 세대들이 사용하는 신조어, 밈 등이 포함된 문장 또는 단어입니다. "
                    + "각 단어별로 끊어서 해석 후 이를 참고하여 150자 내로 요약해서 제시해 주세요. 답변은 단어별 해석을 제외한 요약한 문장만을 출력해 주세요. "
                    + "최종 문장에 큰따옴표가 있다면 작은따옴표로 변경해 주세요.";

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        ObjectMapper objectMapper = new ObjectMapper();

        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("model", "gpt-4o");

        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", script);
        messages.add(userMessage);

        Map<String, String> assistantMessage = new HashMap<>();
        assistantMessage.put("role", "system");
        assistantMessage.put("content", systemMsg);
        messages.add(assistantMessage);

        bodyMap.put("messages", messages);

        String body = objectMapper.writeValueAsString(bodyMap);

        HttpEntity<String> request = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

        return objectMapper.readTree(response.getBody());
    }


    // callChatGpt()가 반환한 JsonNode 객체에서 ChatGPT의 응답 값만 Body 값으로 출력
    @Override
    public ResponseEntity<?> getAssistantMsg(String userMsg) throws JsonProcessingException {
        JsonNode jsonNode = callChatGpt(userMsg);
        String content = jsonNode.path("choices").get(0).path("message").path("content").asText();

        return ResponseEntity.status(HttpStatus.OK).body(content);
    }
}
