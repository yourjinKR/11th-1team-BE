package org.example.knockin.service.impl;

import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.knockin.entity.file.File;
import org.example.knockin.entity.file.FileType;
import org.example.knockin.exception.BusinessException;
import org.example.knockin.exception.FileErrorCode;
import org.example.knockin.service.FileService;
import org.example.knockin.service.FileUploadService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final FileUploadService fileUploadService;

    @Override
    public File upload(MultipartFile multipartFile, FileType type) throws IOException {
        String extension = getExtension(multipartFile);

        String savedFileName = fileUploadService.uploadImage(multipartFile);

        return File.builder()
                .type(type)
                .originalFileName(multipartFile.getOriginalFilename())
                .savedFileName(savedFileName)
                .fileExt(extension)
                .build();
    }

    @Override
    public void deleteAll(List<File> files) {
        for (File file : files) {
            try {
                fileUploadService.deleteImage(file.getSavedFileName());
            } catch (IOException | RuntimeException ignored) {
                // 보상 삭제 실패는 원래 예외를 가리지 않도록 삼킨다.
            }
        }
    }

    private String getExtension(MultipartFile multipartFile) {
        String fileName = multipartFile.getOriginalFilename();

        if (fileName == null || !fileName.contains(".")) {
            throw new BusinessException(FileErrorCode.FILE_EXTENSION_MISSING);
        }

        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }
}
