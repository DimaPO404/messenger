package io.github.DimaPO404.messenger_pet_project;

import java.time.LocalDateTime;

public record User (
        Long id,
        String name,
        String password,
        String phone,
        String userId,
        String description,
        LocalDateTime createAt
){
}
