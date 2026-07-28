package io.github.DimaPO404.messenger_pet_project.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUserId(String userId);
    User findByUserId(String userId);
    List<User> findByUserIdContainingIgnoreCase(String query);
}
