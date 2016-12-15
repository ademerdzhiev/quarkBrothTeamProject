package quarkbrothBlog.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import quarkbrothBlog.entity.Article;

public interface ArticleRepository extends JpaRepository<Article, Integer > {

}
