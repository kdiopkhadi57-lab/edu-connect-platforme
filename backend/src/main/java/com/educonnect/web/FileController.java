package com.educonnect.web;

import com.educonnect.service.PostService;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final PostService postService;

    public FileController(PostService postService) {
        this.postService = postService;
    }

    /**
     * Lecture en ligne (aperçu PDF, lecteur vidéo avec support des requêtes Range)
     * ou téléchargement avec {@code ?download=true}.
     */
    @GetMapping("/{storedName:.+}")
    public ResponseEntity<Resource> get(@PathVariable String storedName,
                                        @RequestParam(defaultValue = "false") boolean download) {
        PostService.FileDownload file = postService.download(storedName, download);
        ContentDisposition disposition = (download ? ContentDisposition.attachment() : ContentDisposition.inline())
                .filename(file.originalName(), StandardCharsets.UTF_8).build();
        MediaType type = file.contentType() == null ? MediaType.APPLICATION_OCTET_STREAM : MediaType.parseMediaType(file.contentType());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(type)
                .body(file.resource());
    }
}
