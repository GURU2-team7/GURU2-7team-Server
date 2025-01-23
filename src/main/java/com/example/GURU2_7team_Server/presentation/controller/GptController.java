package com.example.GURU2_7team_Server.presentation.controller;
import com.example.GURU2_7team_Server.service.dto.recipe.RecipeRequestDto;
import com.example.GURU2_7team_Server.service.dto.recipe.RecipeResponseDto;
import com.example.GURU2_7team_Server.service.gpt.GptService;
import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class GptController {
    private final GptService gptService;

    // 레시피 요청
    @PostMapping("/askRecipe")
    public ResponseEntity<Object> getAssistantMsg(RecipeRequestDto requestDto) {
        try {
            RecipeResponseDto answerDto = gptService.getAssistantMsg(requestDto);
            return ResponseEntity.ok(answerDto);

        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }
}