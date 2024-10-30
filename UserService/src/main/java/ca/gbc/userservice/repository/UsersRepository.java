package ca.gbc.userservice.repository;

import ca.gbc.userservice.model.Roles;
import ca.gbc.userservice.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<Users, Long> {
    Optional<Users> findByEmail(String email);
    // Example repository method
    @Query("SELECT COUNT(u) FROM Users u WHERE u.role = :role")
    Long countByRole(@Param("role") Roles role);

}
