package com.educonnect.service;

import com.educonnect.config.AppProperties;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/** Stockage des fichiers sur disque. Remplaçable par S3/MinIO en production. */
@Service
public class StorageService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "pdf", "doc", "docx", "odt", "ppt", "pptx", "odp", "xls", "xlsx", "ods", "txt", "md", "csv",
            "png", "jpg", "jpeg", "gif", "webp", "svg",
            "mp4", "webm", "mov", "mkv", "mp3", "m4a", "ogg",
            "zip", "rar", "7z", "py", "java", "c", "cpp", "js", "ts", "sql", "ipynb", "tex");

    private final Path root;

    public StorageService(AppProperties props) throws IOException {
        this.root = Path.of(props.uploadDir()).toAbsolutePath().normalize();
        Files.createDirectories(root);
    }

    public String store(MultipartFile file) {
        String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "fichier" : file.getOriginalFilename());
        String ext = StringUtils.getFilenameExtension(original);
        if (ext == null || !ALLOWED_EXTENSIONS.contains(ext.toLowerCase(Locale.ROOT))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Type de fichier non autorisé : " + original);
        }
        String storedName = UUID.randomUUID() + "." + ext.toLowerCase(Locale.ROOT);
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, root.resolve(storedName), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Échec de l'enregistrement du fichier", e);
        }
        return storedName;
    }

    public Resource load(String storedName) {
        Path path = root.resolve(storedName).normalize();
        if (!path.startsWith(root) || !Files.isRegularFile(path)) {
            throw new NotFoundException("Fichier");
        }
        return new PathResource(path);
    }

    public void delete(String storedName) {
        try {
            Path path = root.resolve(storedName).normalize();
            if (path.startsWith(root)) {
                Files.deleteIfExists(path);
            }
        } catch (IOException ignored) {
            // un fichier orphelin n'est pas bloquant
        }
    }
}
