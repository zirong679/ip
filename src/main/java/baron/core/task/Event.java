package baron.core.task;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a task that takes place between a start and end time.
 */
public class Event extends Task {
    private final LocalDateTime fromDate;
    private final LocalDateTime toDate;

    /**
     * Creates an event task with the specified description and time range.
     *
     * @param description The task description.
     * @param fromDate The event start time.
     * @param toDate The event end time.
     */
    public Event(String description, LocalDateTime fromDate, LocalDateTime toDate) {
        assert fromDate != null && toDate != null : "Event tasks must have start and end times";
        assert fromDate.isBefore(toDate) : "An event's start time must be before its end time";
        super(TaskType.EVENT, description);
        this.fromDate = fromDate;
        this.toDate = toDate;
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a, d MMM yyyy");
        return super.toString()
                + " (from: " + fromDate.format(formatter)
                + " to: " + toDate.format(formatter) + ")";
    }

    @Override
    public String toFileString() {
        return super.toFileString() + " | " + fromDate + " | " + toDate;
    }
}
