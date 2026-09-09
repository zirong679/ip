package baron.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import baron.core.task.TaskList;
import baron.core.task.Todo;

/** Tests command processing through the public {@link Parser#parse(String)} method. */
public class ParserTest {
    @TempDir
    private Path tempDir;
    private Path filePath;

    @BeforeEach
    public void setUp() {
        filePath = tempDir.resolve("tasks.txt");
    }

    @Test
    public void parse_listWithTasks_printsTasks() {
        TaskList tasks = new TaskList();
        tasks.addTask(new Todo("buy milk"));
        Parser parser = createParser(tasks);

        String response = parser.parse("list");

        assertTrue(response.contains("1.[T][ ] buy milk"));
    }

    @Test
    public void parse_todoWithDescription_addsAndSavesToFile() throws IOException {
        TaskList tasks = new TaskList();
        Parser parser = createParser(tasks);

        String response = parser.parse("todo buy milk");

        assertEquals(1, tasks.size());
        assertTrue(Files.readString(filePath).contains("T | 0 | buy milk"));
        assertTrue(response.contains("[T][ ] buy milk"));
    }

    @Test
    public void parse_todoWithoutDescription_printsErrorWithoutAddingTask() throws IOException {
        TaskList tasks = new TaskList();
        Parser parser = createParser(tasks);

        String response = parser.parse("todo");

        assertEquals(0, tasks.size());
        assertEquals("", Files.readString(filePath));
        assertTrue(response.contains("Argument for todo is missing"));
    }

    @Test
    public void parse_deadlineWithValidDate_addsAndSavesToFile() throws IOException {
        TaskList tasks = new TaskList();
        Parser parser = createParser(tasks);

        String response = parser.parse("deadline submit report /by 30082026 1800");

        assertEquals(1, tasks.size());
        assertTrue(Files.readString(filePath).contains("D | 0 | submit report | 2026-08-30T18:00"));
        assertTrue(response.contains("[D][ ] submit report (by: 06:00 PM, 30 Aug 2026)"));
    }

    @Test
    public void parse_deadlineWithoutDate_printsErrorWithoutAddingTask() throws IOException {
        TaskList tasks = new TaskList();
        Parser parser = createParser(tasks);

        String response = parser.parse("deadline submit report");

        assertEquals(0, tasks.size());
        assertEquals("", Files.readString(filePath));
        assertTrue(response.contains("Argument for /by is missing"));
    }

    @Test
    public void parse_deadlineWithInvalidDate_printsErrorWithoutAddingTask() throws IOException {
        TaskList tasks = new TaskList();
        Parser parser = createParser(tasks);

        String response = parser.parse("deadline submit report /by Tuesday");

        assertEquals(0, tasks.size());
        assertEquals("", Files.readString(filePath));
        assertTrue(response.contains("Date/time must be in ddMMyyyy HHmm"));
    }

    @Test
    public void parse_eventWithValidDateRange_addsAndSavesToFile() throws IOException {
        TaskList tasks = new TaskList();
        Parser parser = createParser(tasks);

        String response = parser.parse("event team meeting /from 31082026 1000 /to 31082026 1100");

        assertEquals(1, tasks.size());
        assertTrue(Files.readString(filePath).contains(
                "E | 0 | team meeting | 2026-08-31T10:00 | 2026-08-31T11:00"
        ));
        assertTrue(response.contains(
                "[E][ ] team meeting (from: 10:00 AM, 31 Aug 2026 to: 11:00 AM, 31 Aug 2026)"
        ));
    }

    @Test
    public void parse_eventWithEqualDates_printsErrorWithoutAddingTask() throws IOException {
        TaskList tasks = new TaskList();
        Parser parser = createParser(tasks);

        String response = parser.parse("event meeting /from 31082026 1100 /to 31082026 1100");

        assertEquals(0, tasks.size());
        assertEquals("", Files.readString(filePath));
        assertTrue(response.contains("/to date must be after /from date"));
    }

    @Test
    public void parse_markValidTaskNumber_marksAndSavesToFile() throws IOException {
        TaskList tasks = new TaskList();
        tasks.addTask(new Todo("buy milk"));
        Parser parser = createParser(tasks);

        String response = parser.parse("mark 1");

        assertTrue(Files.readString(filePath).contains("T | 1 | buy milk"));
        assertTrue(response.contains("[T][X] buy milk"));
    }

    @Test
    public void parse_unmarkValidTaskNumber_ummarksAndSavesToFile() throws IOException {
        TaskList tasks = new TaskList();
        Todo task = new Todo("buy milk");
        tasks.addTask(task.markAsDone());
        Parser parser = createParser(tasks);

        String response = parser.parse("unmark 1");

        assertTrue(Files.readString(filePath).contains("T | 0 | buy milk"));
        assertTrue(response.contains("[T][ ] buy milk"));
    }

    @Test
    public void parse_deleteValidTaskNumber_deletesAndSavesToFile() throws IOException {
        TaskList tasks = new TaskList();
        tasks.addTask(new Todo("buy milk"));
        tasks.addTask(new Todo("read book"));
        Parser parser = createParser(tasks);

        String response = parser.parse("delete 1");

        assertEquals(1, tasks.size());
        assertFalse(Files.readString(filePath).contains("T | 0 | buy milk"));
        assertTrue(response.contains("[T][ ] buy milk"));
    }

    @Test
    public void parse_deleteInvalidTaskNumber_printsErrorWithoutDeletingTask() {
        TaskList tasks = new TaskList();
        tasks.addTask(new Todo("buy milk"));
        Parser parser = createParser(tasks);

        String response = parser.parse("delete two");

        assertEquals(1, tasks.size());
        assertTrue(response.contains("Task number must be an integer"));
    }

    @Test
    public void parse_tasksMatchKeyword_printsMatchingTasks() {
        TaskList tasks = new TaskList();
        tasks.addTask(new Todo("sell milk"));
        tasks.addTask(new Todo("buy milk"));
        tasks.addTask(new Todo("buy cheese"));
        Parser parser = createParser(tasks);

        String response = parser.parse("find buy");

        assertTrue(response.contains("1.[T][ ] buy milk"));
        assertTrue(response.contains("2.[T][ ] buy cheese"));
    }

    @Test
    public void parse_noTasksMatchKeyword_printsTaskNotFound() {
        TaskList tasks = new TaskList();
        tasks.addTask(new Todo("buy milk"));
        Parser parser = createParser(tasks);

        String response = parser.parse("find sell");

        assertTrue(response.contains("None of your tasks match 'sell'"));
    }

    @Test
    public void parse_invalidCommand_printsUnknownCommand() {
        TaskList tasks = new TaskList();
        Parser parser = createParser(tasks);

        String response = parser.parse("remind me");

        assertTrue(response.contains("Unknown command"));
    }

    private Parser createParser(TaskList tasks) {
        return new Parser(new Storage(filePath), tasks);
    }
}
