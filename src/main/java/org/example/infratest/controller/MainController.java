package org.example.infratest.controller;

import lombok.RequiredArgsConstructor;
import org.example.infratest.service.FileUploadService;
import org.example.infratest.service.impl.MemberServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/main")
public class MainController {
    private final MemberServiceImpl memberServiceImpl;
    private final FileUploadService fileUploadService;

    @PostMapping("/save")
    public ResponseEntity<?> save(@RequestBody String name) {
        return ResponseEntity.ok(memberServiceImpl.save(name));
    }

    @GetMapping("/list")
    public ResponseEntity<?> list() {
        return ResponseEntity.ok(memberServiceImpl.list());
    }

    @PostMapping(path = "/upload", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        String url = fileUploadService.uploadImage(file);
        return ResponseEntity.status(HttpStatus.CREATED).body(url);
    }
}
