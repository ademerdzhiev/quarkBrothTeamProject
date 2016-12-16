/*package quarkbrothBlog.controller;

import org.apache.tomcat.util.http.fileupload.FileUpload;
import org.springframework.validation.BindException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class FileController extends  ArticleController{

    public FileUploadController(){
        setCommandClass(FileUpload.class);
        setCommandName("fileUploadForm");
    }

    @Override
    protected ModelAndView onSubmit(HttpServletRequest request,
                                    HttpServletResponse response, Object command, BindException errors)
            throws Exception {

        FileUpload file = (FileUpload)command;

        MultipartFile multipartFile = file.getFile();

        String fileName="";

        if(multipartFile!=null){
            fileName = multipartFile.getOriginalFilename();
            //do whatever you want
        }

        return new ModelAndView("FileUploadSuccess","fileName",fileName);

    }*/