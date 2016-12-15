package quarkbrothBlog.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import quarkbrothBlog.entity.Tag;

public interface TagRepository extends JpaRepository<Tag, Integer>{

    Tag findByName(String name);
}
