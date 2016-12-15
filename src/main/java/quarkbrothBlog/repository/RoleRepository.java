package quarkbrothBlog.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import quarkbrothBlog.entity.Role;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Role findByName(String name);
}