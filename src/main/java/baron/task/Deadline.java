package baron.task;

/**
 * Represents a task that must be completed by a specified time.
 */
public class Deadline extends Task {
    private String deadline;

    /**
     * Creates a deadline task with the specified description and deadline.
     *
     * @param description The task description.
     * @param deadline The time by which the task must be completed.
     */
    public Deadline(String description, String deadline) {
        super(description);
        this.deadline = deadline;
    }

    @Override
    public String toString() {
        return "[D]" + super.toString() + " (by: " + deadline + ")";
    }

    /** {@inheritDoc} */
    @Override
    public String toFileString() {
        return "D | " + super.toFileString() + " | " + this.deadline;
    }
}
