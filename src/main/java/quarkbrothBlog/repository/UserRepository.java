package quarkbrothBlog.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import quarkbrothBlog.entity.User;

public interface UserRepository extends JpaRepository<User, Integer> {
    User findByEmail(String email);
}