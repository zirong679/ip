package baron.task;

/**
 * Represents a task that takes place between a start and end time.
 */
public class Event extends Task {
    private String fromDate;
    private String toDate;

    /**
     * Creates an event task with the specified description and time range.
     *
     * @param description The task description.
     * @param fromDate The event start time.
     * @param toDate The event end time.
     */
    public Event(String description, String fromDate, String toDate) {
        super(description);
        this.fromDate = fromDate;
        this.toDate = toDate;
    }

    @Override
    public String toString() {
        return "[E]" + super.toString() + " (from: " + this.fromDate + " to: " + this.toDate + ")";
    }
}
