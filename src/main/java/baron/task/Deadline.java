package baron.task;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a task that must be completed by a specified time.
 */
public class Deadline extends Task {
    private LocalDateTime deadline;

    /**
     * Creates a deadline task with the specified description and deadline.
     *
     * @param description The task description.
     * @param deadline The time by which the task must be completed.
     */
    public Deadline(String description, LocalDateTime deadline) {
        super(description);
        this.deadline = deadline;
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a, d MMM yyyy");
        return "[D]" + super.toString() + " (by: " + this.deadline.format(formatter) + ")";
    }

    @Override
    public String toFileString() {
        return "D | " + super.toFileString() + " | " + this.deadline;
    }
}
