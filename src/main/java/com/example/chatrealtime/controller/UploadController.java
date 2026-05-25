package com.example.chatrealtime.controller;

import java.io.IOException;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.chatrealtime.dto.response.UploadResponse;
import com.example.chatrealtime.service.S3Service;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class UploadController {
    private final S3Service s3Service;

    @PostMapping("/image")
    public UploadResponse uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        return s3Service.upload(file);
    }
}
