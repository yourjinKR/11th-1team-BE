package org.example.knockin.service;

import java.io.IOException;
import java.util.List;
import org.example.knockin.entity.file.File;
import org.example.knockin.entity.file.FileType;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    File upload(MultipartFile multipartFile, FileType type) throws IOException;

    File save(MultipartFile multipartFile, FileType type) throws IOException;

    File findBySavedFileNameAndType(String savedFileName, FileType type);

    void deleteAll(List<File> files);
}
