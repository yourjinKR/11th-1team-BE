package org.example.knockin.service;

import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;


public interface FileUploadService {
    String uploadImage(MultipartFile file) throws IOException;
}
