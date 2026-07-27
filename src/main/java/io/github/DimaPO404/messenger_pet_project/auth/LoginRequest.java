package io.github.DimaPO404.messenger_pet_project.auth;

public class LoginRequest {
    private String userId;
    private String password;

    public String getUserId() {
        return userId;
    }

    public String getPassword() {
        return password;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
