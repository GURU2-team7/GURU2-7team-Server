package com.example.GURU2_7team_Server.service.gpt;

import com.example.GURU2_7team_Server.service.dto.recipe.RecipeRequestDto;
import com.example.GURU2_7team_Server.service.dto.recipe.RecipeResponseDto;
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
            "제공한 정보를 바탕으로 요리 레시피를 추천해주는 상황";

    public JsonNode callChatGpt(RecipeRequestDto requestDto) throws JsonProcessingException {
        final String url = "https://api.openai.com/v1/chat/completions";

        // 사용자 요청 정보 불러오기
        String allergy = requestDto.getAllergy();
        String typeOfCooking = requestDto.getTypeOfCooking();
        String ingredients = requestDto.getIngredients();
        String cookingMethod = requestDto.getCookingMethod();
        String cookingTime = requestDto.getCookingTime();

        // 요청 스크립트 작성
        String script =
                ingredients + "만을 재료로 " + cookingMethod + "을 사용하여 " +
                cookingTime + "이내의 1~2인분 " + typeOfCooking + "레시피를" +
                "해당 요리를 먹을 사람들은 " + allergy + " 알러지가 있으니 참고해서 추천해줘.\n" +
                "출력 형식: " +
                "요리명, 사용될 재료 및 재료의 양, 레시피 순서(인덱스 적용), 해당 요리의 칼로리, 해당 요리의 영양성분명, 소요 시간\n" +
                "이 항목들의 제목은 쓰지말고 내용만 \"#\"이 문자를 통해 구분해줘. 이 항목외에 다른 설명은 출력하지 말아줘." +
                "영양성분은 영양성분명만을 \",\" 이문자를 써서 구분해줘.";

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
    public RecipeResponseDto getAssistantMsg(RecipeRequestDto requestDto) throws JsonProcessingException {
        JsonNode jsonNode = callChatGpt(requestDto);
        String response = jsonNode.path("choices").get(0).path("message").path("content").asText();

        String[] answers = response.split("#");

        RecipeResponseDto answerDto = RecipeResponseDto.builder()
                .nameOfDish(answers[0])
                .recipe(answers[1])
                .calorie(answers[2])
                .nutrient(answers[3])
                .cookingTime(answers[4])
                .build();

        System.out.println("Response from GPT: " + response);

        return answerDto;
    }
}
