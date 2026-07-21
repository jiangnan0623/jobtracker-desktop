package com.jobtracker.controller;

import com.jobtracker.common.Result;
import com.jobtracker.entity.Reminder;
import com.jobtracker.service.ReminderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reminders")
public class ReminderController {
    private final ReminderService reminderService;

    @GetMapping
    public Result<List<Reminder>> list() {
        return Result.ok(reminderService.list());
    }

    @GetMapping("/today")
    public Result<List<Reminder>> today() {
        return Result.ok(reminderService.today());
    }

    @PostMapping
    public Result<Reminder> create(@RequestBody Reminder reminder) {
        if (reminder.getStatus() == null || reminder.getStatus().isBlank()) {
            reminder.setStatus("未完成");
        }
        reminderService.save(reminder);
        return Result.ok(reminder);
    }

    @PutMapping("/{id}")
    public Result<Reminder> update(@PathVariable Long id, @RequestBody Reminder reminder) {
        reminder.setId(id);
        reminderService.updateById(reminder);
        return Result.ok(reminder);
    }

    @PatchMapping("/{id}/complete")
    public Result<Void> complete(@PathVariable Long id) {
        reminderService.complete(id);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        reminderService.removeById(id);
        return Result.ok();
    }
}
