package baron;

import java.util.ArrayList;
import java.util.List;

import baron.task.Task;

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
        tasks.get(taskIndex).markAsDone();
        return tasks.get(taskIndex);
    }

    /**
     * Marks the task at the given index as not completed.
     *
     * @param taskIndex The zero-based task index.
     * @return The unmarked task.
     */
    public Task unmarkTask(int taskIndex) {
        tasks.get(taskIndex).markAsNotDone();
        return tasks.get(taskIndex);
    }

    /**
     * Adds a task to the end of this list.
     *
     * @param task The task to add.
     * @return The added task.
     */
    public Task addTask(Task task) {
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
        Task task = tasks.get(taskIndex);
        tasks.remove(taskIndex);
        return task;
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
