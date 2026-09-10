package baron.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import baron.core.exception.BaronException;
import baron.core.task.Deadline;
import baron.core.task.Event;
import baron.core.task.TaskList;
import baron.core.task.Todo;

/**
 * Tests persistent task-file operations performed by {@link Storage}.
 */
public class StorageTest {
    @TempDir
    private Path tempDir;

    private ByteArrayOutputStream output;
    private PrintStream originalOut;

    @BeforeEach
    void setUp() {
        originalOut = System.out;
        output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    public void constructor_missingParentDirectories_createsTaskFile() {
        Path filePath = tempDir.resolve("data").resolve("tasks.txt");

        new Storage(filePath);

        assertTrue(Files.isRegularFile(filePath));
    }

    @Test
    public void readTasks_emptyFile_doesNotAddTasks() throws BaronException {
        Path filePath = tempDir.resolve("tasks.txt");
        Storage storage = new Storage(filePath);
        TaskList tasks = new TaskList();

        storage.readTasks(tasks);

        assertEquals(0, tasks.size());
    }

    @Test
    public void readTasks_validTasks_addsTasksToTaskList() throws Exception {
        Path filePath = tempDir.resolve("tasks.txt");
        UUID uuid = UUID.randomUUID();
        Files.writeString(filePath, String.format(
                """
                %s | T | 0 | buy milk
                %s | D | 1 | submit report | 2026-08-30T18:00
                %s | E | 0 | team meeting | 2026-08-31T10:00 | 2026-08-31T11:00
                """, uuid, uuid, uuid
        ));
        Storage storage = new Storage(filePath);
        TaskList tasks = new TaskList();

        storage.readTasks(tasks);

        assertEquals(3, tasks.size());
    }

    @Test
    public void readTasks_blankLines_ignoresBlankLines() throws Exception {
        Path filePath = tempDir.resolve("tasks.txt");
        Files.writeString(filePath, "\n" + UUID.randomUUID() + " | T | 0 | buy milk\n\n");
        Storage storage = new Storage(filePath);
        TaskList tasks = new TaskList();

        storage.readTasks(tasks);

        assertEquals(1, tasks.size());
    }

    @Test
    public void readTasks_invalidTask_reportsInvalidTask() throws Exception {
        Path filePath = tempDir.resolve("tasks.txt");
        UUID uuid = UUID.randomUUID();
        Files.writeString(filePath, uuid + " | D | 0 | submit report | not-a-date\n");
        Storage storage = new Storage(filePath);
        TaskList tasks = new TaskList();

        storage.readTasks(tasks);

        assertEquals(
                String.format("Invalid task '%s | D | 0 | submit report | not-a-date'\n", uuid),
                output.toString()
        );
    }

    @Test
    public void writeTasks_existingContent_overwritesExistingContent() throws Exception {
        Path filePath = tempDir.resolve("tasks.txt");
        Files.writeString(filePath, "old contents");
        Storage storage = new Storage(filePath);
        TaskList tasks = new TaskList();
        UUID uuid = UUID.randomUUID();
        tasks.addTask(new Todo(uuid, "buy milk"));
        tasks.addTask(new Deadline(
                uuid,
                "submit report",
                LocalDateTime.of(2026, 8, 30, 18, 0)
        ));

        storage.writeTasks(tasks);

        assertEquals(
                String.format(
                        """
                        %s | T | 0 | buy milk
                        %s | D | 0 | submit report | 2026-08-30T18:00
                        """, uuid, uuid
                ),
                Files.readString(filePath)
        );
    }

    @Test
    public void appendTask_existingContent_preservesExistingContent() throws Exception {
        Path filePath = tempDir.resolve("tasks.txt");
        Storage storage = new Storage(filePath);
        UUID uuid = UUID.randomUUID();
        storage.appendTask(new Todo(uuid, "buy milk"));

        storage.appendTask(new Event(
                uuid,
                "team meeting",
                LocalDateTime.of(2026, 8, 31, 10, 0),
                LocalDateTime.of(2026, 8, 31, 11, 0)
        ));

        assertEquals(
                String.format(
                        """
                        %s | T | 0 | buy milk
                        %s | E | 0 | team meeting | 2026-08-31T10:00 | 2026-08-31T11:00
                        """, uuid, uuid
                ),
                Files.readString(filePath)
        );
    }
}
