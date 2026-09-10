package baron.core.task;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the ordered collection of tasks in Baron.
 */
public class TaskList {
    private final List<Task> tasks;

    /**
     * Creates an empty task list.
     */
    public TaskList() {
        tasks = new ArrayList<>();
    }

    /**
     * Returns the number of tasks in this list.
     *
     * @return The task count.
     */
    public int size() {
        return tasks.size();
    }

    /**
     * Marks the task at the given index as completed.
     *
     * @param taskIndex The zero-based task index.
     * @return The marked task.
     */
    public Task markTask(int taskIndex) {
        assert isValidIndex(taskIndex) : "Task operations require an index for an existing task";
        Task task = tasks.get(taskIndex);
        return task.markAsDone();
    }

    /**
     * Marks the task at the given index as not completed.
     *
     * @param taskIndex The zero-based task index.
     * @return The unmarked task.
     */
    public Task unmarkTask(int taskIndex) {
        assert isValidIndex(taskIndex) : "Task operations require an index for an existing task";
        Task task = tasks.get(taskIndex);
        return task.markAsNotDone();
    }

    /**
     * Adds a task to the end of this list.
     *
     * @param task The task to add.
     * @return The added task.
     */
    public Task addTask(Task task) {
        assert task != null : "A task list must not contain null tasks";
        tasks.add(task);
        return task;
    }

    /**
     * Removes the task at the given index.
     *
     * @param taskIndex The zero-based task index.
     * @return The removed task.
     */
    public Task deleteTask(int taskIndex) {
        assert isValidIndex(taskIndex) : "Task operations require an index for an existing task";
        Task task = tasks.get(taskIndex);
        tasks.remove(taskIndex);
        return task;
    }

    /**
     * Returns a task list containing tasks whose descriptions include the specified keyword.
     *
     * @param keyword The keyword to search for.
     * @return A task list of matching tasks, in their original order.
     */
    public TaskList findTasks(String keyword) {
        assert keyword != null : "Task searches require a keyword";
        TaskList matchingTasks = new TaskList();
        for (Task task : tasks) {
            if (task.hasKeyword(keyword)) {
                matchingTasks.addTask(task);
            }
        }
        return matchingTasks;
    }

    /**
     * Returns all tasks in the persistent file format.
     *
     * @return The persistent representation of this list.
     */
    public String toFileString() {
        StringBuilder builder = new StringBuilder();
        for (Task task : tasks) {
            builder.append(task.toFileString()).append(System.lineSeparator());
        }
        return builder.toString();
    }

    /** Returns whether the specified index refers to a task currently in this list. */
    private boolean isValidIndex(int taskIndex) {
        return taskIndex >= 0 && taskIndex < tasks.size();
    }

    /**
     * Returns the tasks as a numbered, user-facing list in insertion order.
     *
     * @return The numbered task list, with each task on a separate line.
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < tasks.size(); i++) {
            builder.append((i + 1)).append(".").append(tasks.get(i)).append("\n");
        }
        return builder.toString();
    }
}
