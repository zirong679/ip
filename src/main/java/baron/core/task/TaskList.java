package baron.core.task;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
     * Creates a task list containing the supplied tasks.
     *
     * @param tasks The tasks to copy into this list.
     */
    private TaskList(List<Task> tasks) {
        this.tasks = new ArrayList<>(tasks);
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
        return getTask(taskIndex).markAsDone();
    }

    /**
     * Marks the task at the given index as not completed.
     *
     * @param taskIndex The zero-based task index.
     * @return The unmarked task.
     */
    public Task unmarkTask(int taskIndex) {
        assert isValidIndex(taskIndex) : "Task operations require an index for an existing task";
        return getTask(taskIndex).markAsNotDone();
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
        return tasks.remove(taskIndex);
    }

    /** Returns the task at the specified zero-based index. */
    private Task getTask(int taskIndex) {
        return tasks.get(taskIndex);
    }

    /**
     * Returns a task list containing tasks whose descriptions include the specified keyword.
     *
     * @param keyword The keyword to search for.
     * @return A task list of matching tasks, in their original order.
     */
    public TaskList findTasks(String keyword) {
        assert keyword != null : "Task searches require a keyword";
        return new TaskList(tasks.stream()
                .filter(task -> task.hasKeyword(keyword))
                .toList());
    }

    /**
     * Returns all tasks in the persistent file format.
     *
     * @return The persistent representation of this list.
     */
    public String toFileString() {
        return tasks.stream()
                .map(task -> task.toFileString() + System.lineSeparator())
                .collect(Collectors.joining());
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
        return IntStream.range(0, tasks.size())
                .mapToObj(index -> (index + 1) + "." + tasks.get(index) + "\n")
                .collect(Collectors.joining());
    }
}
