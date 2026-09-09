package vn.iotstar.controller;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import vn.iotstar.util.UploadUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Controller
public class ImageController {

    @GetMapping("/image")
    public ResponseEntity<Resource> serveImage(@RequestParam("fname") String fname) {
        try {
            Path file = UploadUtil.safeResolve(fname);
            if (!Files.isRegularFile(file)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy file ảnh");
            }
            Resource resource = new UrlResource(file.toUri());
            String contentType = Files.probeContentType(file);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                    .header("X-Content-Type-Options", "nosniff")
                    .body(resource);
        } catch (IllegalArgumentException | IOException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tên file không hợp lệ");
        }
    }
}