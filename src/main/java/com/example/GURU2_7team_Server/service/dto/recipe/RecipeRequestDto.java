package com.example.GURU2_7team_Server.service.dto.recipe;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RecipeRequestDto {

    String allergy;
    String typeOfCooking;
    String ingredients;
    String cookingMethod;
    String cookingTime;
}
