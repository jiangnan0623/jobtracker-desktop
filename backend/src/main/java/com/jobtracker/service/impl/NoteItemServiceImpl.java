package com.jobtracker.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jobtracker.config.StorageProperties;
import com.jobtracker.entity.NoteItem;
import com.jobtracker.mapper.NoteItemMapper;
import com.jobtracker.service.NoteItemService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class NoteItemServiceImpl extends ServiceImpl<NoteItemMapper, NoteItem> implements NoteItemService {
    private static final String TYPE_FOLDER = "FOLDER";
    private static final String TYPE_NOTE = "NOTE";
    private static final Set<String> EDITABLE_EXTENSIONS = Set.of("md", "markdown", "txt");
    private static final Set<String> UPLOAD_EXTENSIONS = Set.of("md", "markdown", "txt", "pdf", "doc", "docx");

    private final StorageProperties storageProperties;

    @Override
    public List<NoteItem> listAll() {
        return list(new LambdaQueryWrapper<NoteItem>()
                .orderByAsc(NoteItem::getType)
                .orderByDesc(NoteItem::getUpdatedTime));
    }

    @Override
    public NoteItem createFolder(NoteItem item) {
        validateTitle(item);
        validateParent(null, item.getParentId());
        item.setType(TYPE_FOLDER);
        item.setContent(null);
        item.setFileName(null);
        item.setFilePath(resolveFolderPath(item).toString());
        createDirectory(item.getFilePath());
        save(item);
        return item;
    }

    @Override
    public NoteItem createNote(NoteItem item) {
        validateTitle(item);
        validateParent(null, item.getParentId());
        item.setType(TYPE_NOTE);
        item.setFileName(safeFileName(item.getTitle()) + ".md");
        writeEditableNote(item);
        save(item);
        return item;
    }

    @Override
    public NoteItem uploadNote(MultipartFile file, Long parentId) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请选择要上传的笔记文件");
        }
        validateParent(null, parentId);
        String originalName = file.getOriginalFilename() == null ? "note.md" : file.getOriginalFilename();
        String extension = extension(originalName);
        if (!UPLOAD_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("仅支持 md/markdown/txt/pdf/doc/docx 文件");
        }

        String title = stripExtension(originalName);
        NoteItem item = new NoteItem();
        item.setType(TYPE_NOTE);
        item.setTitle(title);
        item.setParentId(parentId);
        item.setFileName(safeFolderName(title) + "." + normalizeExtension(extension));

        try {
            if (isEditableExtension(extension)) {
                item.setContent(new String(file.getBytes(), StandardCharsets.UTF_8));
                writeEditableNote(item);
            } else {
                item.setContent(extractPreviewText(file, extension));
                writeBinaryNote(item, file);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("笔记上传失败：" + e.getMessage());
        }

        save(item);
        return item;
    }

    @Override
    public NoteItem saveItem(NoteItem item) {
        NoteItem saved = getById(item.getId());
        if (saved == null) {
            throw new IllegalArgumentException("笔记或文件夹不存在");
        }
        validateTitle(item);
        validateParent(saved.getId(), item.getParentId());
        saved.setTitle(item.getTitle());
        saved.setParentId(item.getParentId());

        if (TYPE_FOLDER.equals(saved.getType())) {
            saved.setFilePath(resolveFolderPath(item).toString());
            createDirectory(saved.getFilePath());
        } else if (isEditableNote(saved)) {
            saved.setContent(item.getContent());
            writeEditableNote(saved);
        } else {
            moveExistingNoteFile(saved);
        }
        updateById(saved);
        return saved;
    }

    @Override
    public void move(Long id, Long parentId) {
        if (Objects.equals(id, parentId)) {
            throw new IllegalArgumentException("不能移动到自己下面");
        }
        NoteItem item = getById(id);
        if (item == null) {
            throw new IllegalArgumentException("笔记或文件夹不存在");
        }
        validateParent(id, parentId);
        item.setParentId(parentId);
        if (TYPE_NOTE.equals(item.getType())) {
            if (isEditableNote(item)) {
                writeEditableNote(item);
            } else {
                moveExistingNoteFile(item);
            }
        }
        updateById(item);
    }

    @Override
    public void removeItem(Long id) {
        removeChildren(id);
        removeById(id);
    }

    @Override
    public Resource download(Long id) {
        NoteItem item = getById(id);
        if (item == null || !TYPE_NOTE.equals(item.getType())) {
            throw new IllegalArgumentException("笔记不存在");
        }
        return new FileSystemResource(item.getFilePath());
    }

    private void removeChildren(Long parentId) {
        List<NoteItem> children = list(new LambdaQueryWrapper<NoteItem>().eq(NoteItem::getParentId, parentId));
        for (NoteItem child : children) {
            removeChildren(child.getId());
            removeById(child.getId());
        }
    }

    private void validateParent(Long sourceId, Long parentId) {
        if (parentId == null) {
            return;
        }
        NoteItem parent = getById(parentId);
        if (parent == null || !TYPE_FOLDER.equals(parent.getType())) {
            throw new IllegalArgumentException("目标文件夹不存在");
        }
        if (sourceId != null) {
            ensureNotDescendant(sourceId, parentId);
        }
    }

    private void ensureNotDescendant(Long sourceId, Long targetParentId) {
        Long cursor = targetParentId;
        while (cursor != null) {
            if (Objects.equals(sourceId, cursor)) {
                throw new IllegalArgumentException("不能移动到自己的子文件夹下面");
            }
            NoteItem parent = getById(cursor);
            cursor = parent == null ? null : parent.getParentId();
        }
    }

    private void validateTitle(NoteItem item) {
        if (!StringUtils.hasText(item.getTitle())) {
            throw new IllegalArgumentException("名称不能为空");
        }
    }

    private Path resolveFolderPath(NoteItem item) {
        if (StringUtils.hasText(item.getFilePath())) {
            return Path.of(item.getFilePath()).toAbsolutePath().normalize();
        }
        return directoryForParent(item.getParentId()).resolve(safeFolderName(item.getTitle())).normalize();
    }

    private Path directoryForParent(Long parentId) {
        if (parentId == null) {
            return Path.of(storageProperties.getGeneralNoteDir()).toAbsolutePath().normalize();
        }
        NoteItem parent = getById(parentId);
        if (parent != null && TYPE_FOLDER.equals(parent.getType()) && StringUtils.hasText(parent.getFilePath())) {
            return Path.of(parent.getFilePath()).toAbsolutePath().normalize();
        }
        return Path.of(storageProperties.getGeneralNoteDir()).toAbsolutePath().normalize();
    }

    private void writeEditableNote(NoteItem item) {
        String fileName = StringUtils.hasText(item.getFileName()) ? item.getFileName() : safeFileName(item.getTitle()) + ".md";
        if (!fileName.endsWith(".md") && !fileName.endsWith(".markdown") && !fileName.endsWith(".txt")) {
            fileName = fileName + ".md";
        }
        Path path = targetPath(item.getParentId(), fileName);
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, item.getContent() == null ? "" : item.getContent(), StandardCharsets.UTF_8);
            deleteOldFileIfMoved(item, path);
        } catch (IOException e) {
            throw new IllegalArgumentException("笔记保存失败：" + e.getMessage());
        }
        item.setFileName(fileName);
        item.setFilePath(path.toString());
    }

    private void writeBinaryNote(NoteItem item, MultipartFile file) throws IOException {
        Path path = targetPath(item.getParentId(), item.getFileName());
        Files.createDirectories(path.getParent());
        file.transferTo(path);
        item.setFilePath(path.toString());
    }

    private void moveExistingNoteFile(NoteItem item) {
        if (!StringUtils.hasText(item.getFilePath()) || !StringUtils.hasText(item.getFileName())) {
            return;
        }
        Path target = targetPath(item.getParentId(), item.getFileName());
        Path source = Path.of(item.getFilePath()).toAbsolutePath().normalize();
        if (source.equals(target)) {
            return;
        }
        try {
            Files.createDirectories(target.getParent());
            if (Files.exists(source)) {
                Files.copy(source, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                Files.deleteIfExists(source);
            }
            item.setFilePath(target.toString());
        } catch (IOException e) {
            throw new IllegalArgumentException("文件移动失败：" + e.getMessage());
        }
    }

    private Path targetPath(Long parentId, String fileName) {
        Path dir = directoryForParent(parentId);
        Path path = dir.resolve(fileName).normalize();
        if (!path.startsWith(dir)) {
            throw new IllegalArgumentException("非法文件名");
        }
        return path;
    }

    private void deleteOldFileIfMoved(NoteItem item, Path path) throws IOException {
        if (StringUtils.hasText(item.getFilePath())) {
            Path oldPath = Path.of(item.getFilePath()).toAbsolutePath().normalize();
            if (!oldPath.equals(path)) {
                Files.deleteIfExists(oldPath);
            }
        }
    }

    private String extractPreviewText(MultipartFile file, String extension) throws IOException {
        if ("pdf".equals(extension)) {
            return "";
        }
        try (InputStream input = file.getInputStream()) {
            if ("docx".equals(extension)) {
                try (XWPFDocument doc = new XWPFDocument(input); XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
                    return extractor.getText();
                }
            }
            if ("doc".equals(extension)) {
                try (HWPFDocument doc = new HWPFDocument(input); WordExtractor extractor = new WordExtractor(doc)) {
                    return extractor.getText();
                }
            }
        }
        return "";
    }

    private void createDirectory(String value) {
        try {
            Files.createDirectories(Path.of(value).toAbsolutePath().normalize());
        } catch (IOException e) {
            throw new IllegalArgumentException("文件夹创建失败：" + e.getMessage());
        }
    }

    private boolean isEditableNote(NoteItem item) {
        return isEditableExtension(extension(item.getFileName()));
    }

    private boolean isEditableExtension(String extension) {
        return EDITABLE_EXTENSIONS.contains(extension);
    }

    private String safeFileName(String value) {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return safeFolderName(value) + "-" + date;
    }

    private String safeFolderName(String value) {
        return value.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    private String extension(String fileName) {
        int index = fileName == null ? -1 : fileName.lastIndexOf('.');
        return index < 0 ? "" : fileName.substring(index + 1).toLowerCase();
    }

    private String stripExtension(String fileName) {
        int index = fileName.lastIndexOf('.');
        return index < 0 ? fileName : fileName.substring(0, index);
    }

    private String normalizeExtension(String extension) {
        return "markdown".equals(extension) ? "md" : extension;
    }
}
