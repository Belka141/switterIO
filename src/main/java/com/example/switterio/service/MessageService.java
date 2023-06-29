package com.example.switterio.service;

import com.example.switterio.domain.Message;
import com.example.switterio.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
public class MessageService {
    @Value("${upload.path}")
    private String uploadPath;

    private final MessageRepository messageRepository;

    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }


    public void saveFile(Message message, MultipartFile file) throws IOException {
        if (file != null && !file.getOriginalFilename().isEmpty()) {
            File uploadeDir = new File(uploadPath);
            if (!uploadeDir.exists()) {
                uploadeDir.mkdir();
            }
            String uuidFile = UUID.randomUUID().toString();
            String resultFileName = uuidFile + "." + file.getOriginalFilename();
            file.transferTo(new File(uploadPath + "/" + resultFileName));
            message.setFilename(resultFileName);
        }
        messageRepository.save(message);
    }
}