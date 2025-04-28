package com.example.Documentation.controller;

import com.example.Documentation.dto.ApiResponse;
import com.example.Documentation.dto.DocumentDto;
import com.example.Documentation.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/document")
@RequiredArgsConstructor
//@CrossOrigin(origins = "*", maxAge = 3600)
public class DocumentController {
    private final DocumentService documentService;

    @PostMapping
    public ResponseEntity<ApiResponse> saveDocument(@RequestBody DocumentDto documentDto) {
        ApiResponse response = documentService.saveDocument(documentDto);
        return ResponseEntity.status(response.isSuccess() ? 200 : 409).body(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getDocuments(@RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "10") int size,
                                                    @RequestParam(required = false) String fromDate,
                                                    @RequestParam(required = false) String toDate
    ) {
        ApiResponse response = documentService.getAllDocuments(fromDate, toDate, page, size);
        return ResponseEntity.status(response.isSuccess() ? 200 : 409).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateDocument(@PathVariable Long id, @RequestBody DocumentDto documentDto) {
        ApiResponse response = documentService.updateDocument(id,documentDto);
        return ResponseEntity.status(response.isSuccess() ? 200 : 409).body(response);
    }

    @GetMapping("/file")
    public ResponseEntity<ApiResponse> getDocumentsFile(@RequestParam(required = false) String fromDate,
                                                        @RequestParam(required = false) String toDate
    ) {
        ApiResponse response = documentService.getFileOfDocuments(fromDate, toDate);
        return ResponseEntity.status(response.isSuccess() ? 200 : 409).body(response);
    }
}
