package quarkbrothBlog.bindingModel;

import org.springframework.web.multipart.MultipartFile;

/**
 * Created by Angel on 13-Dec-16.
 */
public class FileBindingModel {

    private MultipartFile picture;

    public MultipartFile getPicture() {return picture;}
    public void setPicture(MultipartFile picture) {this.picture = picture; }
}
