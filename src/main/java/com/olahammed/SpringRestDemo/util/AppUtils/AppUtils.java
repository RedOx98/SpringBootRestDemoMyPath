package com.olahammed.SpringRestDemo.util.AppUtils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

import org.imgscalr.Scalr;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.web.multipart.MultipartFile;

public class AppUtils {
    // public static String get_photo_upload_path(String fileName, long album_id)
    // throws IOException{
    // Files.createDirectories(Paths.get("src\\main\\resources\\static\\uploads\\"+album_id));
    // return new
    // File("src\\main\\resources\\static\\uploads\\"+album_id).getAbsolutePath() +
    // "\\" + fileName;

    // }

    public static String get_photo_upload_path(String fileName, String folderName, long album_id) throws IOException {
        String path = "src/main/resources/static/uploads/" + album_id + "/" + folderName;
        Files.createDirectories(Paths.get(path));
        // return new File(path + album_id).getAbsolutePath() + "/" + fileName;
        return new File(path).getAbsolutePath() + "/" + fileName;

    }

    public static BufferedImage getThumbnail(MultipartFile orginalFile, Integer width) throws IOException{  
        BufferedImage thumbImg = null;  
        BufferedImage img = ImageIO.read(orginalFile.getInputStream());  
        thumbImg = Scalr.resize(img, Scalr.Method.AUTOMATIC, Scalr.Mode.AUTOMATIC, width, Scalr.OP_ANTIALIAS);   
        return thumbImg;  
    }

    public static Resource getFileAsResource(Long albumId, String folderName, String fileName) throws IOException{
        String location = "src/main/resources/static/uploads/" + albumId + "/" + folderName + "/" + fileName;
        File file = new File(location);
        if (file.exists()) {
            Path path = Paths.get(file.getAbsolutePath());
            return new UrlResource(path.toUri());
        }else {
            return null;
        }
    }
}
