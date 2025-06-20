package com.kkas.kkas.Service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

public interface FileStorageService {

    String saveFile(MultipartFile file) throws IOException;

    boolean deleteFile(String filePath);

    Resource loadFileAsResource(String fileName) throws IOException;
}
