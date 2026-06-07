package com.example.homestaymanager.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.homestaymanager.dto.response.UploadImageResponse;
import com.example.homestaymanager.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CloudinaryServiceImpl implements CloudinaryService {

    private static final Set<String> ALLOWED_FOLDERS = Set.of(
            "rooms",
            "room-types",
            "room-photos",
            "branches",
            "customers",
            "employees",
            "amenities",
            "categories"
    );

    private final Cloudinary cloudinary;

    @Override
    public UploadImageResponse uploadImage(MultipartFile file, String folder) {
        validateCloudinaryConfig();

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File ảnh không được để trống");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("Chỉ cho phép upload file ảnh");
        }

        String safeFolder = normalizeFolder(folder);

        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "homestay/" + safeFolder,
                            "resource_type", "image"
                    )
            );

            return UploadImageResponse.builder()
                    .url((String) result.get("secure_url"))
                    .publicId((String) result.get("public_id"))
                    .folder(safeFolder)
                    .build();
        } catch (IOException exception) {
            throw new RuntimeException("Không thể đọc hoặc upload file ảnh", exception);
        }
    }

    @Override
    public void deleteImage(String publicId) {
        validateCloudinaryConfig();

        if (publicId == null || publicId.isBlank()) {
            throw new RuntimeException("publicId không được để trống");
        }

        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException exception) {
            throw new RuntimeException("Không thể xóa ảnh trên Cloudinary", exception);
        }
    }

    private String normalizeFolder(String folder) {
        String normalized = folder == null || folder.isBlank() ? "misc" : folder.trim();

        if (!ALLOWED_FOLDERS.contains(normalized)) {
            throw new RuntimeException("Folder upload không hợp lệ");
        }

        return normalized;
    }

    private void validateCloudinaryConfig() {
        if (cloudinary.config.cloudName == null || cloudinary.config.cloudName.isBlank()
                || cloudinary.config.apiKey == null || cloudinary.config.apiKey.isBlank()
                || cloudinary.config.apiSecret == null || cloudinary.config.apiSecret.isBlank()) {
            throw new RuntimeException("Thiếu cấu hình Cloudinary");
        }
    }
}
