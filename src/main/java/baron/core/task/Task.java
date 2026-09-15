package baron.core.task;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import baron.core.Baron;
import baron.core.exception.BaronException;

/**
 * Represents a task that can be marked as completed.
 */
public abstract class Task {
    private final UUID uuid;
    private final TaskType taskType;
    private final String description;
    private boolean isDone;
    private TaskList requiredTasks;
    private TaskList unlockedTasks;
    private TaskList savedRequiredTasks;
    private TaskList savedUnlockedTasks;

    /**
     * Creates a task with its immutable identity and an empty dependency set.
     *
     * @param uuid The task identifier.
     * @param taskType The kind of task.
     * @param description The task description.
     */
    protected Task(UUID uuid, TaskType taskType, String description) {
        assert uuid != null : "Tasks must have a task id";
        assert taskType != null : "Tasks must have a task type";
        assert description != null : "Task descriptions must not be null";
        this.uuid = uuid;
        this.taskType = taskType;
        this.description = description;
        isDone = false;
        requiredTasks = new TaskList();
        unlockedTasks = new TaskList();
    }

    /**
     * Returns this task's unique identifier.
     *
     * @return The task identifier.
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     * Returns whether this task has been completed.
     *
     * @return Whether this task is complete.
     */
    public boolean isDone() {
        return isDone;
    }

    /**
     * Returns the icon representing this task's completion status.
     *
     * @return {@code X} for a completed task, or a blank space otherwise.
     */
    public String getStatusIcon() {
        return (isDone ? "X" : " ");
    }

    /**
     * Marks this task as completed if all prerequisite tasks are complete.
     *
     * @return This completed task.
     * @throws BaronException If a prerequisite task is incomplete.
     */
    public Task markAsDone() throws BaronException {
        TaskList allRequiredIncompleteTasks = getAllRequiredTasks().getIncompleteTasks();
        assert (allRequiredIncompleteTasks != null);
        if (allRequiredIncompleteTasks.size() > 0) {
            throw new BaronException(String.format(
                    "Cannot mark task %s as done because the following tasks are not done:\n%s",
                    getNumberedTask(),
                    allRequiredIncompleteTasks.getNumberedTasksWithRelationship(false, false)));
        }
        isDone = true;
        return this;
    }

    /**
     * Marks this task as incomplete if no completed task depends on it.
     *
     * @return This incomplete task.
     * @throws BaronException If a dependent task is complete.
     */
    public Task markAsNotDone() throws BaronException {
        TaskList allUnlockedCompletedTasks = getAllUnlockedTasks().getCompletedTasks();
        assert (allUnlockedCompletedTasks != null);
        if (allUnlockedCompletedTasks.size() > 0) {
            throw new BaronException(String.format(
                    "Cannot mark task %s as not done because the following tasks are done:\n%s",
                    getNumberedTask(),
                    allUnlockedCompletedTasks.getNumberedTasksWithRelationship(false, false)));
        }
        isDone = false;
        return this;
    }

    /**
     * Returns whether this task's description contains the specified keyword.
     *
     * @param keyword The keyword to search for.
     * @return Whether the task description contains the keyword.
     */
    public boolean hasKeyword(String keyword) {
        return description.contains(keyword);
    }

    /**
     * Sets the tasks that must be completed before this task.
     *
     * @param requiredTasks The prerequisite tasks.
     * @throws BaronException If the dependencies create a cycle.
     */
    public void setRequiredTasks(TaskList requiredTasks) throws BaronException {
        assert requiredTasks != null : "Tasks required must not be null";
        for (Task requiredTask : requiredTasks.getTasks()) {
            if (isDone && !requiredTask.isDone) {
                clearRelationships();
                throw new BaronException(String.format(
                        "%1$s requires %2$s, but %1$s is done and %2$s is not done",
                        getTaskNumber(),
                        requiredTask.getTaskNumber()));
            }
            if (willUnlock(requiredTask)) {
                clearRelationships();
                throw new BaronException(String.format(
                        "%1$s cannot require %2$s because %1$s unlocks %2$s",
                        getTaskNumber(),
                        requiredTask.getTaskNumber()));
            }
            requiredTask.unlockedTasks.addTask(this);
        }
        this.requiredTasks = requiredTasks;
    }

    /**
     * Sets the tasks that this task unlocks upon completion.
     *
     * @param unlockedTasks The dependent tasks.
     * @throws BaronException If the dependencies create a cycle.
     */
    public void setUnlockedTasks(TaskList unlockedTasks) throws BaronException {
        assert unlockedTasks != null : "Tasks unlocked must not be null";
        for (Task unlockedTask : unlockedTasks.getTasks()) {
            if (!isDone && unlockedTask.isDone) {
                clearRelationships();
                throw new BaronException(String.format(
                        "%1$s unlocks %2$s, but %1$s is not done and %2$s is done",
                        getTaskNumber(),
                        unlockedTask.getTaskNumber()));
            }
            if (willRequire(unlockedTask)) {
                clearRelationships();
                throw new BaronException(String.format(
                        "%1$s cannot unlock %2$s because %1$s requires %2$s",
                        getTaskNumber(),
                        unlockedTask.getTaskNumber()));
            }
            unlockedTask.requiredTasks.addTask(this);
        }
        this.unlockedTasks = unlockedTasks;
    }

    /**
     * Saves this task's current prerequisite and dependent-task relationships.
     */
    public void saveRelationships() {
        savedRequiredTasks = requiredTasks;
        savedUnlockedTasks = unlockedTasks;
    }

    /**
     * Removes this task's current prerequisite and dependent-task relationships.
     */
    public void clearRelationships() {
        for (Task requiredTask : requiredTasks.getTasks()) {
            requiredTask.unlockedTasks.deleteTask(this);
        }
        requiredTasks = new TaskList();

        for (Task unlockedTask : unlockedTasks.getTasks()) {
            unlockedTask.requiredTasks.deleteTask(this);
        }
        unlockedTasks = new TaskList();
    }

    /**
     * Restores the prerequisite and dependent-task relationships saved most recently.
     *
     * @throws BaronException If the restored relationships form a cycle.
     */
    public void recoverRelationships() throws BaronException {
        setRequiredTasks(savedRequiredTasks);
        setUnlockedTasks(savedUnlockedTasks);
    }

    /**
     * Returns whether completing this task requires the specified task, directly or indirectly.
     *
     * @param task The task to look for.
     * @return Whether this task requires the specified task.
     */
    private boolean willRequire(Task task) {
        if (equals(task)) {
            return true;
        }
        return requiredTasks.getTasks().stream()
                .map(requiredTask -> requiredTask.willRequire(task))
                .reduce(false, (result, bool) -> result || bool);
    }

    /**
     * Returns whether completing this task unlocks the specified task, directly or indirectly.
     *
     * @param task The task to look for.
     * @return Whether this task unlocks the specified task.
     */
    private boolean willUnlock(Task task) {
        if (equals(task)) {
            return true;
        }
        return unlockedTasks.getTasks().stream()
                .map(unlockedTask -> unlockedTask.willUnlock(task))
                .reduce(false, (result, bool) -> result || bool);
    }

    /**
     * Returns every task that must be completed before this task.
     *
     * @return The direct and indirect prerequisite tasks.
     */
    private TaskList getAllRequiredTasks() {
        if (requiredTasks.size() == 0) {
            return new TaskList();
        }
        Set<Task> allRequiredTasks = requiredTasks.getTasks().stream()
                .flatMap(requiredTask -> requiredTask.getAllRequiredTasks().getTasks().stream())
                .collect(Collectors.toSet());
        allRequiredTasks.addAll(requiredTasks.getTasks());
        return new TaskList(allRequiredTasks);
    }

    /**
     * Returns every task unlocked by completing this task.
     *
     * @return The direct and indirect dependent tasks.
     */
    private TaskList getAllUnlockedTasks() {
        if (unlockedTasks.size() == 0) {
            return new TaskList();
        }
        Set<Task> allUnlockedTasks = unlockedTasks.getTasks().stream()
                .flatMap(unlockedTask -> unlockedTask.getAllUnlockedTasks().getTasks().stream())
                .collect(Collectors.toSet());
        allUnlockedTasks.addAll(unlockedTasks.getTasks());
        return new TaskList(allUnlockedTasks);
    }

    /**
     * Returns this task in the file format used for persistent storage.
     *
     * @return The persistent representation of this task.
     */
    public String toFileString() {
        return String.join(
                " | ",
                uuid.toString(),
                taskType.getFileCode(),
                isDone ? "1" : "0",
                description,
                requiredTasks.toUuidString());
    }

    /**
     * Returns a user-facing representation containing this task's completion status and
     * description.
     *
     * @return The formatted task description.
     */
    @Override
    public String toString() {
        return getTaskString();
    }

    /**
     * Returns this task's current one-based number in Baron's task list.
     *
     * @return The task number, or {@code #?} if this task is not in the list.
     */
    protected String getTaskNumber() {
        int taskIndex = Baron.TASKS.indexOf(this);
        return "#" + (taskIndex == -1 ? "?" : taskIndex + 1);
    }

    /**
     * Returns this task's completion status and description.
     *
     * @return The formatted task details.
     */
    protected String getTaskString() {
        return String.format("[%s][%s] %s", taskType.getFileCode(), getStatusIcon(), description);
    }

    /**
     * Returns this task with its current task number.
     *
     * @return The numbered task.
     */
    public String getNumberedTask() {
        return getTaskNumber() + " " + getTaskString();
    }

    /**
     * Returns the requested prerequisite and dependent-task details for this task.
     *
     * @param showRequiredTasks Whether to include prerequisite tasks.
     * @param showUnlockedTasks Whether to include dependent tasks.
     * @return The formatted relationship details.
     */
    protected String getRelationship(boolean showRequiredTasks, boolean showUnlockedTasks) {
        String requiredTaskString = showRequiredTasks && requiredTasks.size() > 0
                ? "\nrequires:\n" + requiredTasks.getNumberedTasks() : "";
        String unlockedTaskString = showUnlockedTasks && unlockedTasks.size() > 0
                ? "\nunlocks:\n" + unlockedTasks.getNumberedTasks() : "";
        return requiredTaskString + unlockedTaskString;
    }

    /**
     * Returns this task with its number and, optionally, its prerequisite and dependent tasks.
     *
     * @param showRequiredTasks Whether to include prerequisite tasks.
     * @param showUnlockedTasks Whether to include dependent tasks.
     * @return The numbered task and requested relationship details.
     */
    public String getNumberedTaskWithRelationship(boolean showRequiredTasks, boolean showUnlockedTasks) {
        return "task " + getTaskNumber() + " " + this + getRelationship(showRequiredTasks, showUnlockedTasks);
    }
}
