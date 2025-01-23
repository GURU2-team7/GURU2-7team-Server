package com.example.GURU2_7team_Server.service.gpt;

import com.example.GURU2_7team_Server.service.dto.recipe.RecipeRequestDto;
import com.example.GURU2_7team_Server.service.dto.recipe.RecipeResponseDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public interface GptService {

    RecipeResponseDto getAssistantMsg(RecipeRequestDto requestDto) throws JsonProcessingException;
}
