package baron.core.task;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;

public class TodoTest {
    @Test
    public void constructor_uuidProvided_constructedWithProvidedUuid() {
        UUID uuid = UUID.randomUUID();
        Todo todo = new Todo(uuid, "task");
        assertTrue(todo.toFileString().contains(uuid.toString()));
    }

    @Test
    public void constructor_uuidNotProvided_constructedWithRandomUuid() {
        Todo todo = new Todo("task");
        String uuidString = todo.toFileString().split(" \\| ")[0];
        assertDoesNotThrow(() -> UUID.fromString(uuidString));
    }
}
