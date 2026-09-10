package baron.core.task;

/**
 * Represents a task without a deadline or event time.
 */
public class Todo extends Task {
    /**
     * Creates a to-do task with the specified description.
     *
     * @param description The task description.
     */
    public Todo(String description) {
        super(TaskType.TODO, description);
    }

}
