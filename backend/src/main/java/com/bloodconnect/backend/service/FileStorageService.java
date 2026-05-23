package com.bloodconnect.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.Resource;

import java.nio.file.Path;
import java.nio.file.Paths;

import java.io.File;
import java.io.IOException;

@Service
public class FileStorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    // ================= SAVE FILE =================
    public String saveFile(MultipartFile file, String roleFolder, Long userId, String name, String fileType) throws IOException {

        String projectPath = System.getProperty("user.dir");

        String safeName = name.replaceAll("\\s+", "_");

        String userFolder = userId + "_" + safeName;

        String uploadPath = projectPath + File.separator +
                uploadDir + File.separator +
                roleFolder + File.separator +
                userFolder;

        File directory = new File(uploadPath);

        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (!created) {
                throw new RuntimeException("Failed to create directory");
            }
        }

        String original = file.getOriginalFilename();

        if (original == null || !original.contains(".")) {
            throw new RuntimeException("Invalid file name");
        }

        String extension = original.substring(original.lastIndexOf("."));

        String fileName = fileType + extension;

        File destination = new File(directory, fileName);

        file.transferTo(destination);

        return uploadDir + "/" + roleFolder + "/" + userFolder + "/" + fileName;
    }

    // ================= DELETE USER FOLDER =================
    public void deleteUserFolder(String roleFolder, Long userId, String name) {

        String projectPath = System.getProperty("user.dir");

        String safeName = name.replaceAll("\\s+", "_");

        String folderName = userId + "_" + safeName;

        String folderPath = projectPath + File.separator +
                uploadDir + File.separator +
                roleFolder + File.separator +
                folderName;

        File folder = new File(folderPath);

        if (folder.exists()) {

            File[] files = folder.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.exists()) {
                        boolean deleted = file.delete();
                        if (!deleted) {
                            System.out.println("Failed to delete file: " + file.getName());
                        }
                    }
                }
            }

            boolean deletedFolder = folder.delete();
            if (!deletedFolder) {
                System.out.println("Failed to delete folder: " + folder.getName());
            }
        }
    }

    // ================= GET FILE =================
    public Resource getFile(String filePath) throws Exception {

        String projectPath = System.getProperty("user.dir");

        // ✅ FULL CORRECT PATH
        Path path = Paths.get(projectPath).resolve(filePath).normalize();

        Resource resource = new UrlResource(path.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            throw new RuntimeException("File not found: " + filePath);
        }

        return resource;
    }
}