package quarkbrothBlog.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import quarkbrothBlog.entity.Category;

/**
 * Created by Angel on 01-Dec-16.
 */
public interface CategoryRepository extends JpaRepository<Category, Integer>{
}
