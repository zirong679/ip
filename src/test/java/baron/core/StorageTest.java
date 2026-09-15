package baron.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import baron.core.exception.BaronException;
import baron.core.task.Deadline;
import baron.core.task.Event;
import baron.core.task.Task;
import baron.core.task.TaskList;
import baron.core.task.Todo;

/**
 * Tests for {@link Storage}.
 */
class StorageTest {
    @TempDir
    private Path tempDir;

    /**
     * Clears the shared application task list before every test.
     */
    @BeforeEach
    void setUp() {
        clearTasks();
    }

    /**
     * Clears tasks added while running a test.
     */
    @AfterEach
    void tearDown() {
        clearTasks();
    }

    /**
     * Verifies that storage creates a missing task file and its parent directories.
     */
    @Test
    void constructor_nestedMissingPath_taskFileCreated() {
        Path filePath = tempDir.resolve("data").resolve("tasks.txt");

        new Storage(filePath);

        assertTrue(Files.isRegularFile(filePath));
    }

    /**
     * Verifies that saving replaces existing file contents with the current task list.
     */
    @Test
    void writeTasks_tasksPresent_taskListSerializedToFile() throws IOException {
        Path filePath = tempDir.resolve("tasks.txt");
        Storage storage = new Storage(filePath);
        Baron.TASKS.addTask(new Todo(
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                "buy milk"));
        Baron.TASKS.addTask(new Todo(
                UUID.fromString("00000000-0000-0000-0000-000000000002"),
                "read notes"));

        storage.writeTasks();

        assertEquals(
                """
                00000000-0000-0000-0000-000000000001 | T | 0 | buy milk |\s
                00000000-0000-0000-0000-000000000002 | T | 0 | read notes |\s""",
                Files.readString(filePath));
    }

    /**
     * Verifies that reading a saved task restores its description and completion state.
     */
    @Test
    void readTasks_savedCompletedTodo_taskRestored() throws IOException {
        Path filePath = tempDir.resolve("tasks.txt");
        Files.writeString(filePath, "00000000-0000-0000-0000-000000000001 | T | 1 | buy milk | ");
        Storage storage = new Storage(filePath);

        storage.readTasks();

        Task restoredTask = Baron.TASKS.getTasks().getFirst();
        assertEquals(1, Baron.TASKS.size());
        assertTrue(restoredTask.isDone());
        assertEquals("[T][X] buy milk", restoredTask.toString());
    }

    /**
     * Verifies that all task types and prerequisite relationships survive a storage round trip.
     */
    @Test
    void readTasks_allTaskKindsAndRelationships_roundTripCorrectly() throws BaronException {
        Path filePath = tempDir.resolve("tasks.txt");
        Storage storage = new Storage(filePath);
        Todo prerequisite = new Todo(
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                "prepare");
        Deadline deadline = new Deadline(
                UUID.fromString("00000000-0000-0000-0000-000000000002"),
                "submit",
                LocalDateTime.of(2026, 8, 30, 18, 0));
        Event event = new Event(
                UUID.fromString("00000000-0000-0000-0000-000000000003"),
                "meeting",
                LocalDateTime.of(2026, 8, 31, 10, 0),
                LocalDateTime.of(2026, 8, 31, 11, 0));
        deadline.setRequiredTasks(new TaskList(List.of(prerequisite)));
        Baron.TASKS.addTask(prerequisite);
        Baron.TASKS.addTask(deadline);
        Baron.TASKS.addTask(event);
        storage.writeTasks();

        clearTasks();
        new Storage(filePath).readTasks();

        assertEquals(3, Baron.TASKS.size());
        assertEquals("[T][ ] prepare", Baron.TASKS.getTasks().get(0).toString());
        assertEquals(
                """
                [D][ ] submit
                by: 06:00 PM, 30 Aug 2026""",
                Baron.TASKS.getTasks().get(1).toString());
        assertEquals(
                """
                [E][ ] meeting
                from: 10:00 AM, 31 Aug 2026
                to: 11:00 AM, 31 Aug 2026""",
                Baron.TASKS.getTasks().get(2).toString());
        assertTrue(Baron.TASKS.getTasks().get(1)
                .getNumberedTaskWithRelationship(true, false).contains("requires:"));
    }

    /**
     * Verifies that malformed and blank records are ignored without preventing valid records from
     * loading.
     */
    @Test
    void readTasks_malformedAndBlankRecords_ignoresBadRecords() throws IOException {
        Path filePath = tempDir.resolve("tasks.txt");
        Files.writeString(filePath,
                "\nnot a task\n00000000-0000-0000-0000-000000000001 | T | 0 | valid | \n");

        new Storage(filePath).readTasks();

        assertEquals(1, Baron.TASKS.size());
        assertTrue(Baron.TASKS.getTasks().getFirst().getNumberedTask().contains("valid"));
    }

    /**
     * Removes every task from Baron's shared task list.
     */
    private void clearTasks() {
        for (Task task : Baron.TASKS.getTasks()) {
            Baron.TASKS.deleteTask(task);
        }
    }
}
