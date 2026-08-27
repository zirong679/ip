package baron.task;

/**
 * Represents a task without a deadline or event time.
 */
public class ToDo extends Task {
    /**
     * Creates a to-do task with the specified description.
     *
     * @param description The task description.
     */
    public ToDo(String description) {
        super(description);
    }

    @Override
    public String toString() {
        return "[T]" + super.toString();
    }
}
