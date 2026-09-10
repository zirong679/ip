package baron.core.task;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

public class EventTest {
    @Test
    public void constructor_uuidProvided_constructedWithProvidedUuid() {
        UUID uuid = UUID.randomUUID();
        Event event = new Event(uuid, "task",
                LocalDateTime.of(2026, 1, 1, 12, 00),
                LocalDateTime.of(2026, 1, 1, 12, 01)
        );
        assertTrue(event.toFileString().contains(uuid.toString()));
    }

    @Test
    public void constructor_uuidNotProvided_constructedWithRandomUuid() {
        Event event = new Event("task",
                LocalDateTime.of(2026, 1, 1, 12, 00),
                LocalDateTime.of(2026, 1, 1, 12, 01)
        );
        String uuidString = event.toFileString().split(" \\| ")[0];
        assertDoesNotThrow(() -> UUID.fromString(uuidString));
    }
}
