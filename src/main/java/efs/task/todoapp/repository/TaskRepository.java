package efs.task.todoapp.repository;

import efs.task.todoapp.ToDoApplication;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.logging.Logger;

public class TaskRepository implements Repository<UUID, TaskEntity> {

    private static final Logger LOGGER = Logger.getLogger(TaskRepository.class.getName());

    Map<UUID, TaskEntity> tasksMap = new TreeMap<>();

    @Override
    public UUID save(TaskEntity taskEntity) {
        if (null == taskEntity) {
            return null;
        }
        UUID id = UUID.randomUUID();
        taskEntity.setId(id);
        tasksMap.put(id, taskEntity);
        return id;
    }

    @Override
    public TaskEntity query(UUID uuid) {
        return tasksMap.get(uuid);
    }

    @Override
    public List<TaskEntity> query(Predicate<TaskEntity> condition) {
        List<TaskEntity> result = new ArrayList<>();

        for (TaskEntity task : tasksMap.values()) {
            if (condition.test(task)) {
                result.add(task);
            }
        }

        return result;
    }

    @Override
    public TaskEntity update(UUID uuid, TaskEntity updatedTask) {
        TaskEntity task = tasksMap.get(uuid);
        LOGGER.info("TaskRepository::update  `" + updatedTask.toJson() + "`");

        String desc = updatedTask.getDescription();
        if (null != desc && !desc.isBlank()) {
            task.setDescription(desc);
        }

        String due = updatedTask.getDue();
        if (null != due && !due.isBlank()) {
            task.setDue(due);
        }

        return task;
    }

    @Override
    public boolean delete(UUID uuid) {
        return (null != tasksMap.remove(uuid));
    }
}
