package com.kkas.kkas.Service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    /**
     * Uygulamanın çalışma dizini altında "uploads" klasörü adı.
     * İstersen application.properties üzerinden configurable hale de getirebilirsin.
     */
    private final Path uploadDirectory;

    public FileStorageServiceImpl(
            @Value("${file.upload-dir:uploads}") String uploadDir
    ) {
        // Eğer application.properties içinde file.upload-dir tanımlıysa orayı kullan,
        // yoksa default "uploads" klasörünü kullan.
        this.uploadDirectory = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    /**
     * Bean oluşturulduktan sonra (PostConstruct) uploadDirectory’in varlığını kontrol et,
     * yoksa klasörü oluştur.
     */
    @PostConstruct
    public void init() {
        try {
            if (!Files.exists(uploadDirectory)) {
                Files.createDirectories(uploadDirectory);
            }
        } catch (IOException ex) {
            throw new RuntimeException("Dosya yükleme klasörü oluşturulamadı: " + uploadDirectory, ex);
        }
    }

    @Override
    public String saveFile(MultipartFile file) throws IOException {
        // Dosyanın orijinal adını al ve temizle (clean)
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        // Dosya adı boşsa hata fırlat
        if (originalFilename.contains("..")) {
            // Güvensiz path testi
            throw new IOException("Dosya adı geçersiz: " + originalFilename);
        }

        // UUID oluştur ve extension'ı koru
        String fileExtension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            fileExtension = originalFilename.substring(dotIndex); // .mp3, .wav vs.
        }
        String newFileName = UUID.randomUUID().toString() + fileExtension;

        // Hedef yol: uploads/newFileName
        Path targetLocation = uploadDirectory.resolve(newFileName);

        // Gelen dosyayı kopyala (eğer aynı ada bir dosya varsa üzerine yaz)
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        // Mutlak ya da proje içinden erişilebilecek URL/path dönebiliriz
        // Örneğin: return targetLocation.toString();
        return targetLocation.toString();
    }

    @Override
    public boolean deleteFile(String filePath) {
        try {
            Path pathToDelete = Paths.get(filePath).toAbsolutePath().normalize();
            if (Files.exists(pathToDelete)) {
                return Files.deleteIfExists(pathToDelete);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public Resource loadFileAsResource(String fileName) throws IOException {
        try {
            Path filePath = uploadDirectory.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new IOException("Dosya bulunamadı: " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new IOException("Dosya URL'si oluşturulamadı: " + fileName, ex);
        }
    }
}
