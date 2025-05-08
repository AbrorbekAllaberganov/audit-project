package com.example.Documentation.controller;

import com.example.Documentation.dto.ApiResponse;
import com.example.Documentation.service.AttachmentService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/attachment")
@RequiredArgsConstructor
//@CrossOrigin(origins = "*")
public class AttachmentController {
    private final AttachmentService attachmentService;

    @Operation(summary = "upload file", description = "upload file to the server")
    @PostMapping(value = "/save", consumes = "multipart/form-data")
    public ResponseEntity<?> saveFile(@RequestParam(name = "file") MultipartFile multipartFile) {
        ApiResponse response=attachmentService.saveAttachment(multipartFile);
        return ResponseEntity.status(response.isSuccess()?201:400).body(response);
    }

    @Operation(summary = "download a file", description = "download a file from the server by hashID")
    @GetMapping("/download/{hashId}")
    public ResponseEntity<?> download(@PathVariable String hashId) {
        ApiResponse response = attachmentService.getBase64OfFile(hashId);
        return ResponseEntity.status(response.isSuccess()?200:404).body(response);
    }

}
