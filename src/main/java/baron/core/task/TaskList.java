package baron.core.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import baron.core.exception.BaronException;

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
    public TaskList(Collection<Task> tasks) {
        this.tasks = new ArrayList<>(tasks);
    }

    /**
     * Validates that an index identifies a task in this list.
     *
     * @param taskIndex The zero-based task index.
     * @throws BaronException If the index does not identify a task.
     */
    public void checkTaskIndex(int taskIndex) throws BaronException {
        if (taskIndex < 0 || taskIndex >= tasks.size()) {
            throw new BaronException("Invalid task index");
        }
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
     * Returns the zero-based position of a task in this list.
     *
     * @param task The task to locate.
     * @return The task index, or {@code -1} if it is absent.
     */
    public int indexOf(Task task) {
        return tasks.indexOf(task);
    }

    /**
     * Marks the task at the given index as completed.
     *
     * @param taskIndex The zero-based task index.
     * @return The marked task.
     * @throws BaronException If the index does not identify a task.
     */
    public Task markTask(int taskIndex) throws BaronException {
        checkTaskIndex(taskIndex);
        return tasks.get(taskIndex).markAsDone();
    }

    /**
     * Marks the task at the given index as not completed.
     *
     * @param taskIndex The zero-based task index.
     * @return The unmarked task.
     * @throws BaronException If the index does not identify a task.
     */
    public Task unmarkTask(int taskIndex) throws BaronException {
        checkTaskIndex(taskIndex);
        return tasks.get(taskIndex).markAsNotDone();
    }

    /**
     * Adds a task to the end of this list.
     *
     * @param task The task to add.
     */
    public void addTask(Task task) {
        assert task != null : "A task list must not contain null tasks";
        tasks.add(task);
    }

    /**
     * Removes the specified task from this list.
     *
     * @param task The task to remove.
     */
    public void deleteTask(Task task) {
        tasks.remove(task);
    }

    /**
     * Removes the task at the given index.
     *
     * @param taskIndex The zero-based task index.
     * @return The removed task.
     * @throws BaronException If the index does not identify a task.
     */
    public Task deleteTask(int taskIndex) throws BaronException {
        checkTaskIndex(taskIndex);
        Task taskToDelete = tasks.get(taskIndex);
        taskToDelete.clearRelationships();
        return tasks.remove(taskIndex);
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
     * Returns an unmodifiable snapshot of the tasks in this list.
     *
     * @return The tasks in this list.
     */
    public List<Task> getTasks() {
        return List.copyOf(tasks);
    }

    /**
     * Returns a list containing only completed tasks.
     *
     * @return The completed tasks.
     */
    protected TaskList getCompletedTasks() {
        return new TaskList(tasks.stream().filter(Task::isDone).toList());
    }

    /**
     * Returns a list containing only incomplete tasks.
     *
     * @return The incomplete tasks.
     */
    protected TaskList getIncompleteTasks() {
        return new TaskList(tasks.stream().filter(task -> !task.isDone()).toList());
    }

    /**
     * Returns the task UUIDs as a comma-separated string.
     *
     * @return The task identifiers.
     */
    protected String toUuidString() {
        return tasks.stream()
                .map(task -> task.getUuid().toString())
                .collect(Collectors.joining(", "));
    }

    /**
     * Returns all tasks in the persistent file format.
     *
     * @return The persistent representation of this list.
     */
    public String toFileString() {
        return tasks.stream().map(Task::toFileString).collect(Collectors.joining("\n"));
    }

    /**
     * Returns every task in this list on a separate line.
     *
     * @return The formatted tasks.
     */
    @Override
    public String toString() {
        return tasks.stream().map(Task::toString).collect(Collectors.joining("\n"));
    }

    /**
     * Returns every task in this list with its current task number.
     *
     * @return The numbered tasks.
     */
    public String getNumberedTasks() {
        return tasks.stream().map(Task::getNumberedTask).collect(Collectors.joining("\n"));
    }

    /**
     * Returns every task with its number and, optionally, its prerequisite and dependent tasks.
     *
     * @param showRequiredTasks Whether to include prerequisite tasks.
     * @param showUnlockedTasks Whether to include dependent tasks.
     * @return The numbered tasks and requested relationships.
     */
    public String getNumberedTasksWithRelationship(boolean showRequiredTasks, boolean showUnlockedTasks) {
        return tasks.stream()
                .map(task -> task.getNumberedTaskWithRelationship(showRequiredTasks, showUnlockedTasks))
                .collect(Collectors.joining("\n\n"));
    }
}
