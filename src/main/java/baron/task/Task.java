package baron.task;

/**
 * Represents a task that can be marked as completed.
 */
public class Task {
    private final String description;
    private boolean isDone;

    /**
     * Creates a task with the specified description.
     *
     * @param description The task description.
     */
    public Task(String description) {
        this.description = description;
        isDone = false;
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
     * Marks this task as completed.
     */
    public void markAsDone() {
        isDone = true;
    }

    /**
     * Marks this task as not completed.
     */
    public void markAsNotDone() {
        isDone = false;
    }

    @Override
    public String toString() {
        return "[" + getStatusIcon() + "] " + description;
    }

    /**
     * Returns this task in the file format used for persistent storage.
     *
     * @return The persistent representation of this task.
     */
    public String toFileString() {
        return (isDone ? "1" : "0") + " | " + description;
    }
}
