package quarkbrothBlog.entity;

import javax.persistence.*;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "articles")
public class Article {

    private Integer id;
    private String title;
    private String content;
    private String date;
    private User author;
    private Category category;
    //private Tag tag;
    private int viewCount;

    private Set<Comment> comments;
    private String imageName;

    public Article(){

    }

    public Article(String title, String content, String date, User author, Category category,
                   HashSet<Tag> tags, String imageName) {
        this.title = title;
        this.content = content;
        this.date = new Date().toString();
        this.author = author;
        this.category = category;
        this.tags = tags;
        this.imageName = imageName;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Column(nullable = false)
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Column(columnDefinition = "text", nullable = false)
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Column(nullable = false)
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @ManyToOne()
    @JoinColumn(nullable = false, name = "authorId")
    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    @Transient
    public String getSummary(){
        return this.getContent().substring(0, this.getContent().length()/2 )+ "...";
    }



    @ManyToOne()
    @JoinColumn(nullable = false, name = "categoryId")
    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    private Set<Tag> tags;

    @ManyToMany
    @JoinColumn(table = "article_tags")
    public Set<Tag> getTags() {
        return tags;
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }

    public int getViewCount(){return viewCount;}

    public void setViewCount(int viewCount){this.viewCount = viewCount;}

    @OneToMany(mappedBy = "article")
    public Set<Comment> getComments() {
        return comments;
    }

    public void setComments(Set<Comment> comments) {
        this.comments = comments;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }
}

