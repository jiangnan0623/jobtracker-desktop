package com.jobtracker.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jobtracker.entity.Reminder;
import com.jobtracker.mapper.ReminderMapper;
import com.jobtracker.service.ReminderService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class ReminderServiceImpl extends ServiceImpl<ReminderMapper, Reminder> implements ReminderService {
    @Override
    public List<Reminder> today() {
        return list(new LambdaQueryWrapper<Reminder>()
                .between(Reminder::getRemindTime, LocalDate.now().atStartOfDay(), LocalDate.now().atTime(LocalTime.MAX))
                .ne(Reminder::getStatus, "已完成")
                .orderByAsc(Reminder::getRemindTime));
    }

    @Override
    public void complete(Long id) {
        Reminder reminder = getById(id);
        if (reminder == null) {
            throw new IllegalArgumentException("提醒不存在");
        }
        reminder.setStatus("已完成");
        updateById(reminder);
    }
}
