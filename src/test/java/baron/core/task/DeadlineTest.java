package baron.core.task;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

public class DeadlineTest {
    @Test
    public void constructor_uuidProvided_constructedWithProvidedUuid() {
        UUID uuid = UUID.randomUUID();
        Deadline deadline = new Deadline(uuid, "task",
                LocalDateTime.of(2026, 1, 1, 12, 00));
        assertTrue(deadline.toFileString().contains(uuid.toString()));
    }

    @Test
    public void constructor_uuidNotProvided_constructedWithRandomUuid() {
        Deadline deadline = new Deadline("task",
                LocalDateTime.of(2026, 1, 1, 12, 00));
        String uuidString = deadline.toFileString().split(" \\| ")[0];
        assertDoesNotThrow(() -> UUID.fromString(uuidString));
    }
}
