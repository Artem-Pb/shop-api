package com.polybezev.telegrambot.service;

import com.polybezev.telegrambot.model.Task;
import com.polybezev.telegrambot.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;

    public Task addTask(Long chatId, String description) {
        Task task = Task.builder()
                .chatId(chatId)
                .description(description)
                .done(false)
                .build();
        return taskRepository.save(task);
    }

    public List<Task> getTasks(Long chatId) {
        return taskRepository.findByChatId(chatId);
    }

    public boolean completeTask(Long chatId, Long taskId) {
        return taskRepository.findById(taskId)
                .filter(task -> task.getChatId().equals(chatId))
                .map(task -> {
                    task.setDone(true);
                    taskRepository.save(task);
                    return true;
                })
                .orElse(false);
    }

    public boolean deleteTask(Long chatId, Long taskId) {
        return taskRepository.findById(taskId)
                .filter(task -> task.getChatId().equals(chatId))
                .map(task -> {
                    taskRepository.delete(task);
                    return true;
                })
                .orElse(false);
    }
}
