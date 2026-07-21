package com.jobtracker.controller;

import com.jobtracker.common.Result;
import com.jobtracker.entity.NoteItem;
import com.jobtracker.service.NoteItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/note-items")
public class NoteItemController {
    private final NoteItemService noteItemService;

    @GetMapping
    public Result<List<NoteItem>> list() {
        return Result.ok(noteItemService.listAll());
    }

    @PostMapping("/folder")
    public Result<NoteItem> createFolder(@RequestBody NoteItem item) {
        return Result.ok(noteItemService.createFolder(item));
    }

    @PostMapping("/note")
    public Result<NoteItem> createNote(@RequestBody NoteItem item) {
        return Result.ok(noteItemService.createNote(item));
    }

    @PostMapping("/upload")
    public Result<NoteItem> uploadNote(@RequestParam("file") MultipartFile file, @RequestParam(required = false) Long parentId) {
        return Result.ok(noteItemService.uploadNote(file, parentId));
    }

    @PutMapping("/{id}")
    public Result<NoteItem> update(@PathVariable Long id, @RequestBody NoteItem item) {
        item.setId(id);
        return Result.ok(noteItemService.saveItem(item));
    }

    @PatchMapping("/{id}/move")
    public Result<Void> move(@PathVariable Long id, @RequestParam(required = false) String parentId) {
        noteItemService.move(id, parseParentId(parentId));
        return Result.ok();
    }

    @PatchMapping("/{id}/move-root")
    public Result<Void> moveRoot(@PathVariable Long id) {
        noteItemService.move(id, null);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        noteItemService.removeItem(id);
        return Result.ok();
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        NoteItem note = noteItemService.getById(id);
        Resource resource = noteItemService.download(id);
        String fileName = URLEncoder.encode(note.getFileName(), StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
                .contentType(downloadType(note.getFileName()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + fileName)
                .body(resource);
    }

    @GetMapping("/{id}/preview")
    public ResponseEntity<Resource> preview(@PathVariable Long id) {
        NoteItem note = noteItemService.getById(id);
        Resource resource = noteItemService.download(id);
        String fileName = URLEncoder.encode(note.getFileName(), StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
                .contentType(previewType(note.getFileName()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename*=UTF-8''" + fileName)
                .body(resource);
    }

    private Long parseParentId(String parentId) {
        if (parentId == null || parentId.isBlank() || "root".equalsIgnoreCase(parentId) || "null".equalsIgnoreCase(parentId)) {
            return null;
        }
        return Long.parseLong(parentId);
    }

    private MediaType downloadType(String fileName) {
        String lower = fileName == null ? "" : fileName.toLowerCase();
        if (lower.endsWith(".pdf")) {
            return MediaType.APPLICATION_PDF;
        }
        if (lower.endsWith(".txt") || lower.endsWith(".md") || lower.endsWith(".markdown")) {
            return MediaType.parseMediaType("text/plain;charset=UTF-8");
        }
        if (lower.endsWith(".docx")) {
            return MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        }
        if (lower.endsWith(".doc")) {
            return MediaType.parseMediaType("application/msword");
        }
        return MediaType.APPLICATION_OCTET_STREAM;
    }

    private MediaType previewType(String fileName) {
        String lower = fileName == null ? "" : fileName.toLowerCase();
        if (lower.endsWith(".pdf")) {
            return MediaType.APPLICATION_PDF;
        }
        if (lower.endsWith(".txt") || lower.endsWith(".md") || lower.endsWith(".markdown")) {
            return MediaType.parseMediaType("text/plain;charset=UTF-8");
        }
        return MediaType.APPLICATION_OCTET_STREAM;
    }
}
