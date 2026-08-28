package baron.task;

/**
 * Represents a task that can be marked as completed.
 */
public class Task {
    private String description;
    private boolean isDone;

    /**
     * Creates a task with the specified description.
     *
     * @param description The task description.
     */
    public Task(String description) {
        this.description = description;
        this.isDone = false;
    }

    public String getStatusIcon() {
        return (isDone ? "X" : " "); // mark done task with X
    }

    /**
     * Marks this task as completed.
     */
    public void markAsDone() {
        this.isDone = true;
    }

    /**
     * Marks this task as not completed.
     */
    public void markAsNotDone() {
        this.isDone = false;
    }

    @Override
    public String toString() {
        return "[" + this.getStatusIcon() + "] " + this.description;
    }

    /**
     * Returns this task in the file format used for persistent storage.
     *
     * @return The persistent representation of this task.
     */
    public String toFileString() {
        return (this.isDone ? "1" : "0") + " | " + this.description;
    }
}
