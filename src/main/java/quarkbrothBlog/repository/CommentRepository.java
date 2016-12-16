package quarkbrothBlog.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import quarkbrothBlog.entity.Article;
import quarkbrothBlog.entity.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Integer> {
    List<Comment> findByArticle(Article article);
}
