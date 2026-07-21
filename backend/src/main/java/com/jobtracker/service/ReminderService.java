package com.jobtracker.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jobtracker.entity.Reminder;

import java.util.List;

public interface ReminderService extends IService<Reminder> {
    List<Reminder> today();
    void complete(Long id);
}
