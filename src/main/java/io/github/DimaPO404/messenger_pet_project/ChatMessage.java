package io.github.DimaPO404.messenger_pet_project;

import java.time.Instant;
import java.time.LocalDateTime;

public record ChatMessage(
        Long id,
        String sender,
        String recipient,
        String content,
        Long chatId, //чтобы не создавать topic для каждого чата отдельно, а передавать идентификатор чата
        LocalDateTime timeStamp,
        MessageStatus status
){
}
