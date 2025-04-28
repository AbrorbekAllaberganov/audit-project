package com.example.Documentation.service;

import com.example.Documentation.dto.ApiResponse;
import com.example.Documentation.entity.Attachment;
import com.example.Documentation.repository.AttachmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttachmentService {
    private final AttachmentRepository attachmentRepository;

    public ApiResponse saveAttachment(MultipartFile multipartFile) {
        Attachment attachment = new Attachment();

        attachment.setContentType(multipartFile.getContentType());
        attachment.setFileSize(multipartFile.getSize());
        attachment.setName(multipartFile.getOriginalFilename());
        attachment.setExtension(getExtension(attachment.getName()).toLowerCase());
        attachment.setHashId(UUID.randomUUID().toString());


        LocalDate date = LocalDate.now();

        // change value downloadPath
        String downloadPath = "downloads";
        String localPath = downloadPath + String.format(
                "/%d/%d/%d/%s",
                date.getYear(),
                date.getMonthValue(),
                date.getDayOfMonth(),
                attachment.getExtension().toLowerCase());

        attachment.setUploadPath(localPath);


        // downloadPath / year / month / day / extension
        File file = new File(localPath);

        // " downloadPath / year / month / day / extension "   crate directory
        file.mkdirs();

        // save MyFile into base
        attachment.setLink(file.getAbsolutePath() + "/" + String.format("%s.%s", attachment.getHashId(), attachment.getExtension()));


        try {
            attachmentRepository.save(attachment);
            // copy bytes into new file or saving into storage
            File fileNew = new File(file.getAbsolutePath() + "/" + String.format("%s.%s", attachment.getHashId(), attachment.getExtension()));
            multipartFile.transferTo(fileNew);
            return new ApiResponse("file saved", true, attachment);

        } catch (IOException e) {
            return new ApiResponse(e.getMessage(), false);
        }
    }

    private String getExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }


    public ApiResponse getBase64OfFile(String hashId) {
        Optional<Attachment> attachmentOptional = attachmentRepository.findByHashId(hashId);
        if (attachmentOptional.isEmpty())
            return new ApiResponse("file not found", false);
        Attachment attachment = attachmentOptional.get();
        File file = new File(attachment.getLink());


        if (!file.exists() || !file.isFile()) {
            return new ApiResponse("file not found", false);
        }

        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            byte[] fileBytes = new byte[(int) file.length()];
            fileInputStream.read(fileBytes);
            String base64String = Base64.getEncoder().encodeToString(fileBytes);

            return new ApiResponse("base64 file", true, base64String);
        } catch (IOException e) {
            return new ApiResponse("base64 not created", false);
        }
    }
}
