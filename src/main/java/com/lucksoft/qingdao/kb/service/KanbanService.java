package com.lucksoft.qingdao.kb.service;

import com.lucksoft.qingdao.kb.dto.Task;
import com.lucksoft.qingdao.kb.dto.TaskQuery;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class KanbanService {

    private final Map<String, List<Task>> taskDatabase = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        // Initialize mock data
        taskDatabase.put("PRG 20#高速卷接机组/ZJ112", createMachineTasks());
        taskDatabase.put("GDX2 11#包装机(看板机)", createMachineTasks().subList(0,5));
        taskDatabase.put("TEST 11#包装机(看板机)", createMachineTasks().subList(0,5));
    }

    public List<String> getMachines() {
        return new ArrayList<>(taskDatabase.keySet());
    }

    public List<Task> getTasks(TaskQuery query) {
        List<Task> machineTasks = taskDatabase.getOrDefault(query.getMachine(), Collections.emptyList());
        String todayStr = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        return machineTasks.stream()
                .filter(task -> {
                    if ("pending".equals(query.getView())) {
                        boolean isTodayCompleted = "completed".equals(task.getStatus()) && todayStr.equals(formatDate(task.getCompleteDate()));
                        if ("pending".equals(query.getSubView())) return "pending".equals(task.getStatus());
                        if ("completed".equals(query.getSubView())) return isTodayCompleted;
                    } else if ("scoring".equals(query.getView())) {
                        boolean isTodayCompleted = "completed".equals(task.getStatus()) && todayStr.equals(formatDate(task.getCompleteDate()));
                        if (!isTodayCompleted) return false;
                        if ("unscored".equals(query.getSubView())) return task.getCheckedScore() == null;
                        if ("scored".equals(query.getSubView())) return task.getCheckedScore() != null;
                    }
                    return true; // For history view, filtering is done by date on all completed tasks
                })
                .collect(Collectors.toList());
    }

    public List<Task> getHistoryTasks(String machineName) {
        return taskDatabase.getOrDefault(machineName, Collections.emptyList())
                .stream()
                .filter(t -> "completed".equals(t.getStatus()))
                .collect(Collectors.toList());
    }

    public synchronized boolean completeTask(String machine, String taskId, boolean isAbnormal, String reason) {
        return findTask(machine, taskId).map(task -> {
            task.setStatus("completed");
            task.setCompleteDate(new Date());
            task.setIsAbnormal(isAbnormal);
            task.setAbnormalReason(reason);
            return true;
        }).orElse(false);
    }

    public synchronized long batchCompleteTasks(String machine, List<String> taskIds) {
        return taskIds.stream()
                .map(taskId -> completeTask(machine, taskId, false, null))
                .filter(Boolean::booleanValue)
                .count();
    }

    public synchronized long batchScoreTasks(String machine, Map<String, Integer> scores, String checker) {
        return scores.entrySet().stream()
                .map(entry -> findTask(machine, entry.getKey()).map(task -> {
                    task.setCheckedScore(entry.getValue());
                    task.setChecker(checker);
                    return true;
                }).orElse(false))
                .filter(Boolean::booleanValue)
                .count();
    }

    public synchronized boolean updateScore(String machine, String taskId, int newScore) {
        return findTask(machine, taskId).map(task -> {
            if(task.getCheckedScore() != null) {
                task.setCheckedScore(newScore);
                // Also update currentScore for consistency if needed
                task.setCurrentScore(newScore);
                return true;
            }
            return false;
        }).orElse(false);
    }

    private Optional<Task> findTask(String machine, String taskId) {
        return taskDatabase.getOrDefault(machine, Collections.emptyList())
                .stream()
                .filter(t -> t.getId().equals(taskId))
                .findFirst();
    }

    private String formatDate(Date date) {
        if (date == null) return null;
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }

    private List<Task> createMachineTasks() {
        List<Task> tasks = new ArrayList<>();
        tasks.add(new Task("task-1", "703喷胶部位", "用干净湿抹布清洁喷胶头...", "无积胶", 5, "李明"));
        tasks.add(new Task("task-2", "水箱和水循环系统", "检查水箱冷却液位...", "液位正常", 3, "李明"));
        tasks.add(new Task("task-3", "检查空压气源", "检查压力是否正常...", "压力正常", 2, "张三"));
        tasks.add(new Task("task-4", "烟包输送带", "清洁输送带表面...", "表面干净", 4, "陈七"));
        tasks.add(new Task("task-5", "操作面板清洁", "用软布擦拭操作面板...", "无灰尘", 2, "张三"));
        tasks.add(new Task("task-6", "安全门检查", "检查所有安全门传感器...", "开关灵敏", 4, "王五"));
        tasks.add(new Task("task-7", "主电机检查", "听主电机运行时有无异响...", "运行平稳", 5, "赵六"));
        tasks.add(new Task("task-8", "传送带张紧度", "检查各主要传送带...", "无松弛", 3, "王五"));
        tasks.add(new Task("task-9", "润滑油位检查", "检查主减速箱油位视窗", "油位在标线之间", 4, "赵六"));
        tasks.add(new Task("task-10", "滤芯清洁", "清洁空气过滤器滤芯...", "滤芯洁净", 3, "李明"));
        tasks.add(new Task("task-11", "废料收集箱", "清理所有废料、废丝...", "箱内清洁", 2, "张三"));
        tasks.add(new Task("task-12", "光电传感器", "用软布和清洁剂擦拭...", "探头洁净", 3, "王五"));

        // Simulate some completed tasks for today
        Task task3 = tasks.get(2);
        task3.setStatus("completed");
        task3.setCompleteDate(new Date());

        Task task4 = tasks.get(3);
        task4.setStatus("completed");
        task4.setCompleteDate(new Date());
        task4.setCheckedScore(3);
        task4.setChecker("王工");

        // Simulate some tasks for yesterday
        Date yesterday = Date.from(new Date().toInstant().minusSeconds(86400));
        Task task5 = tasks.get(4);
        task5.setStatus("completed");
        task5.setCompleteDate(yesterday);

        Task task6 = tasks.get(5);
        task6.setStatus("completed");
        task6.setCompleteDate(yesterday);
        task6.setCheckedScore(4);
        task6.setChecker("王工");

        Task task7 = tasks.get(6);
        task7.setStatus("completed");
        task7.setCompleteDate(yesterday);
        task7.setIsAbnormal(true);
        task7.setAbnormalReason("传感器轻微松动");

        return tasks;
    }
}
