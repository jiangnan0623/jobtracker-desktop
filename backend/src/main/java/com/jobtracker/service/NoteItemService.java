package com.jobtracker.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jobtracker.entity.NoteItem;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface NoteItemService extends IService<NoteItem> {
    List<NoteItem> listAll();

    NoteItem createFolder(NoteItem item);

    NoteItem createNote(NoteItem item);

    NoteItem uploadNote(MultipartFile file, Long parentId);

    NoteItem saveItem(NoteItem item);

    void move(Long id, Long parentId);

    void removeItem(Long id);

    Resource download(Long id);
}
