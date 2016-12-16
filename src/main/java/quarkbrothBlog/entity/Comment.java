package quarkbrothBlog.entity;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name="comments")
public class Comment {

    private Integer id;

    private String comment;

    private String date;

    private User user;

    private Article article;



    public Comment() {}

    public Comment(String comment, String date, User user, Article article) {
        this.comment = comment;
        this.date = new Date().toString();
        this.user = user;
        this.article = article;
    }




    @Transient
    public boolean isCommenter(User userId) {
        return Objects.equals(this.getUser().getId(), userId.getId());
    }





    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Column(columnDefinition = "text", nullable = false)
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @ManyToOne()
    @JoinColumn(nullable = false, name = "userId")
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Column(nullable = false)
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @ManyToOne()
    @JoinColumn(nullable = false, name = "articleId")
    public Article getArticle() {
        return article;
    }

    public void setArticle(Article article) {
        this.article = article;
    }
}
