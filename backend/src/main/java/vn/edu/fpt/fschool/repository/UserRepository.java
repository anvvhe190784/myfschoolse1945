package vn.edu.fpt.fschool.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.fschool.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByEmail(String email);

  Optional<User> findByRollNumber(String rollNumber);

  Optional<User> findByEmailIgnoreCase(String email);

  boolean existsByEmail(String email);

  boolean existsByRollNumber(String rollNumber);

  List<User> findByRole(String role);
}
