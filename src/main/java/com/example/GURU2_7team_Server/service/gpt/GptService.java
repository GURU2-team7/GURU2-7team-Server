package com.example.GURU2_7team_Server.service.gpt;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public interface GptService {

    ResponseEntity<?> getAssistantMsg(String question) throws JsonProcessingException;
}
