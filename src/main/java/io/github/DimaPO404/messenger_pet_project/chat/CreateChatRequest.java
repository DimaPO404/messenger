package io.github.DimaPO404.messenger_pet_project.chat;

import java.util.List;

public class CreateChatRequest {
    private String name;
    private ChatType type;
    private List<String> userIds;

    public String getName() {
        return name;
    }

    public ChatType getType() {
        return type;
    }

    public List<String> getUserIds() {
        return userIds;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(ChatType type) {
        this.type = type;
    }

    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
    }
}
