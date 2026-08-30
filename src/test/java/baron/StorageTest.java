package baron;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import baron.task.Deadline;
import baron.task.Event;
import baron.task.Todo;

/** Tests persistent task-file operations performed by {@link Storage}. */
public class StorageTest {
    @TempDir
    private Path tempDir;

    @Test
    public void constructor_missingParentDirectories_createsTaskFile() {
        Path filePath = tempDir.resolve("data").resolve("tasks.txt");

        new Storage(filePath);

        assertTrue(Files.isRegularFile(filePath));
    }

    @Test
    public void readTasks_emptyFile_addsNoTasks() throws BaronException {
        Path filePath = tempDir.resolve("tasks.txt");
        Storage storage = new Storage(filePath);
        TaskList tasks = new TaskList();

        storage.readTasks(tasks);

        assertEquals(0, tasks.size());
    }

    @Test
    public void readTasks_validTasks_addsTasksToTaskList() throws Exception {
        Path filePath = tempDir.resolve("tasks.txt");
        Files.writeString(
                filePath,
                """
                T | 0 | buy milk
                D | 1 | submit report | 2026-08-30T18:00
                E | 0 | team meeting | 2026-08-31T10:00 | 2026-08-31T11:00
                """
        );
        Storage storage = new Storage(filePath);
        TaskList tasks = new TaskList();

        storage.readTasks(tasks);

        assertEquals(3, tasks.size());
    }

    @Test
    public void readTasks_blankLines_ignoresBlankLines() throws Exception {
        Path filePath = tempDir.resolve("tasks.txt");
        Files.writeString(filePath, "\nT | 0 | buy milk\n\n");
        Storage storage = new Storage(filePath);
        TaskList tasks = new TaskList();

        storage.readTasks(tasks);

        assertEquals(1, tasks.size());
    }

    @Test
    public void readTasks_invalidTask_throwsBaronException() throws Exception {
        Path filePath = tempDir.resolve("tasks.txt");
        Files.writeString(filePath, "D | 0 | submit report | not-a-date\n");
        Storage storage = new Storage(filePath);
        TaskList tasks = new TaskList();

        BaronException exception = assertThrows(BaronException.class, () -> storage.readTasks(tasks));

        assertEquals("Invalid task 'D | 0 | submit report | not-a-date'", exception.getMessage());
    }

    @Test
    public void writeTasks_existingContent_overwritesExistingContent() throws Exception {
        Path filePath = tempDir.resolve("tasks.txt");
        Files.writeString(filePath, "old contents");
        Storage storage = new Storage(filePath);
        TaskList tasks = new TaskList();
        tasks.addTask(new Todo("buy milk"));
        tasks.addTask(new Deadline(
                "submit report",
                LocalDateTime.of(2026, 8, 30, 18, 0)
        ));

        storage.writeTasks(tasks);

        assertEquals(
                """
                T | 0 | buy milk
                D | 0 | submit report | 2026-08-30T18:00
                """,
                Files.readString(filePath)
        );
    }

    @Test
    public void appendTask_existingContent_preservesExistingContent() throws Exception {
        Path filePath = tempDir.resolve("tasks.txt");
        Storage storage = new Storage(filePath);
        storage.appendTask(new Todo("buy milk"));

        storage.appendTask(new Event(
                "team meeting",
                LocalDateTime.of(2026, 8, 31, 10, 0),
                LocalDateTime.of(2026, 8, 31, 11, 0)
        ));

        assertEquals(
                """
                T | 0 | buy milk
                E | 0 | team meeting | 2026-08-31T10:00 | 2026-08-31T11:00
                """,
                Files.readString(filePath)
        );
    }
}
