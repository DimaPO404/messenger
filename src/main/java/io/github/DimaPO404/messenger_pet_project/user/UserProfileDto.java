package io.github.DimaPO404.messenger_pet_project.user;

public class UserProfileDto {
    private String userId;
    private String name;
    private String phone;
    private String description;

    public UserProfileDto(String userId, String name, String phone, String description) {
        this.userId = userId;
        this.name = name;
        this.phone = phone;
        this.description = description;
    }

    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getDescription() { return description; }
}
