package com.example.GURU2_7team_Server.service.dto.recipe;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RecipeResponseDto {

    String nameOfDish;
    String ingredients;
    String recipe;
    String calorie;
    String nutrient;
    String cookingTime;
}
