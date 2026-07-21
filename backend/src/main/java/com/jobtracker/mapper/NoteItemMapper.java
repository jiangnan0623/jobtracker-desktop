package com.jobtracker.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jobtracker.entity.NoteItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NoteItemMapper extends BaseMapper<NoteItem> {
}
