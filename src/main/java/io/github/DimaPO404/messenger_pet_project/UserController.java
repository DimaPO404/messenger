package io.github.DimaPO404.messenger_pet_project;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private UserService userService;

    @Autowired
    public UserController( UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser (@RequestBody User user) {
            try {
                User created = userService.createUser(user);
                return ResponseEntity.status(HttpStatus.OK).body(created);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(" Failed to create an account" + e.getMessage());
            }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser (@RequestBody LoginRequest request) {
        User loginUser = userService.loginUser(request.getUserId(), request.getPassword());
        return ResponseEntity.ok(loginUser);
    }
}
