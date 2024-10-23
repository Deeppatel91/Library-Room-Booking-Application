package ca.gbc.userservice.repository;

import ca.gbc.userservice.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<Users, Long> {
    // Custom query to find a user by email
    Optional<Users> findByEmail(String email);
}
