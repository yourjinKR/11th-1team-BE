package org.example.knockin.controller;

import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.knockin.entity.member.Member;
import org.example.knockin.global.api.CommonResponse;
import org.example.knockin.service.FileUploadService;
import org.example.knockin.service.impl.MemberServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequiredArgsConstructor
@RequestMapping("/main")
public class MainController {
    private final MemberServiceImpl memberServiceImpl;
    private final FileUploadService fileUploadService;

    @PostMapping("/save")
    public CommonResponse<?> save(@RequestBody String name) {
        Member member = memberServiceImpl.save(name);
        return CommonResponse.status(HttpStatus.CREATED).body(member);
    }

    @GetMapping("/list")
    public CommonResponse<?> list() {
        List<Member> members = memberServiceImpl.list();
        return CommonResponse.status(HttpStatus.OK).body(members);
    }

    @PostMapping(path = "/upload", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        String url = fileUploadService.uploadImage(file);
        return ResponseEntity.status(HttpStatus.CREATED).body(url);
    }
}
