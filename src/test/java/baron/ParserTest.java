package baron;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import baron.task.Todo;

/** Tests command processing through the public {@link Parser#parse(String)} method. */
public class ParserTest {
    @TempDir
    private Path tempDir;
    private Path filePath;

    private final PrintStream originalOutput = System.out;
    private ByteArrayOutputStream output;

    @BeforeEach
    public void setUp() {
        filePath = tempDir.resolve("tasks.txt");
        output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));
    }

    @AfterEach
    public void tearDown() {
        System.setOut(originalOutput);
    }

    @Test
    public void parse_bye_returnsTrue() {
        TaskList tasks = new TaskList();
        Parser parser = createParser(tasks);

        boolean shouldExit = parser.parse("bye");

        assertTrue(shouldExit);
    }

    @Test
    public void parse_list_printsTasks() {
        TaskList tasks = new TaskList();
        tasks.addTask(new Todo("buy milk"));
        Parser parser = createParser(tasks);

        parser.parse("list");

        assertTrue(output.toString().contains("1.[T][ ] buy milk"));
    }

    @Test
    public void parse_todoWithDescription_addsAndSavesTodo() throws IOException {
        TaskList tasks = new TaskList();
        Parser parser = createParser(tasks);

        parser.parse("todo buy milk");

        assertEquals(1, tasks.size());
        assertTrue(Files.readString(filePath).contains("T | 0 | buy milk"));
        assertTrue(output.toString().contains("[T][ ] buy milk"));
    }

    @Test
    public void parse_todoWithoutDescription_printsErrorWithoutAddingTask() throws IOException {
        TaskList tasks = new TaskList();
        Parser parser = createParser(tasks);

        parser.parse("todo");

        assertEquals(0, tasks.size());
        assertEquals("", Files.readString(filePath));
        assertTrue(output.toString().contains("Argument for todo is missing"));
    }

    @Test
    public void parse_deadlineWithDescriptionWithDate_addsAndSavesDeadline() throws IOException {
        TaskList tasks = new TaskList();
        Parser parser = createParser(tasks);

        parser.parse("deadline submit report /by 30082026 1800");

        assertEquals(1, tasks.size());
        assertTrue(Files.readString(filePath).contains("D | 0 | submit report | 2026-08-30T18:00"));
        assertTrue(output.toString().contains("[D][ ] submit report (by: 06:00 PM, 30 Aug 2026)"));
    }

    @Test
    public void parse_deadlineWithDescriptionWithoutDate_printsErrorWithoutAddingTask() throws IOException {
        TaskList tasks = new TaskList();
        Parser parser = createParser(tasks);

        parser.parse("deadline submit report");

        assertEquals(0, tasks.size());
        assertEquals("", Files.readString(filePath));
        assertTrue(output.toString().contains("Argument for /by is missing"));
    }

    @Test
    public void parse_deadlineWithInvalidDate_printsErrorWithoutAddingTask() throws IOException {
        TaskList tasks = new TaskList();
        Parser parser = createParser(tasks);

        parser.parse("deadline submit report /by Tuesday");

        assertEquals(0, tasks.size());
        assertEquals("", Files.readString(filePath));
        assertTrue(output.toString().contains("Date/time must be in ddMMyyyy HHmm"));
    }

    @Test
    public void parse_eventWithDescriptionWithDateRange_addAndSaveEvent() throws IOException {
        TaskList tasks = new TaskList();
        Parser parser = createParser(tasks);

        parser.parse("event team meeting /from 31082026 1000 /to 31082026 1100");

        assertEquals(1, tasks.size());
        assertTrue(Files.readString(filePath).contains(
                "E | 0 | team meeting | 2026-08-31T10:00 | 2026-08-31T11:00"
        ));
        assertTrue(output.toString().contains(
                "[E][ ] team meeting (from: 10:00 AM, 31 Aug 2026 to: 11:00 AM, 31 Aug 2026)"
        ));
    }

    @Test
    public void parse_eventToDateEqualFromDate_printsErrorWithoutAddingTask() throws IOException {
        TaskList tasks = new TaskList();
        Parser parser = createParser(tasks);

        parser.parse("event meeting /from 31082026 1100 /to 31082026 1100");

        assertEquals(0, tasks.size());
        assertEquals("", Files.readString(filePath));
        assertTrue(output.toString().contains("/to date must be after /from date"));
    }

    @Test
    public void parse_markValidTaskNumber_markAndSaveTask() throws IOException {
        TaskList tasks = new TaskList();
        tasks.addTask(new Todo("buy milk"));
        Parser parser = createParser(tasks);

        parser.parse("mark 1");

        assertTrue(Files.readString(filePath).contains("T | 1 | buy milk"));
        assertTrue(output.toString().contains("[T][X] buy milk"));
    }

    @Test
    public void parse_unmarkValidTaskNumber_unmarkAndSaveTask() throws IOException {
        TaskList tasks = new TaskList();
        Todo task = new Todo("buy milk");
        tasks.addTask(task.markAsDone());
        Parser parser = createParser(tasks);

        parser.parse("unmark 1");

        assertTrue(Files.readString(filePath).contains("T | 0 | buy milk"));
        assertTrue(output.toString().contains("[T][ ] buy milk"));
    }

    @Test
    public void parse_deleteValidTaskNumber_deleteTask() throws IOException {
        TaskList tasks = new TaskList();
        tasks.addTask(new Todo("buy milk"));
        tasks.addTask(new Todo("read book"));
        Parser parser = createParser(tasks);

        parser.parse("delete 1");

        assertEquals(1, tasks.size());
        assertFalse(Files.readString(filePath).contains("T | 0 | buy milk"));
        assertTrue(output.toString().contains("[T][ ] buy milk"));
    }

    @Test
    public void parse_deleteInvalidTaskNumber_printErrorWithoutDeletingTask() {
        TaskList tasks = new TaskList();
        tasks.addTask(new Todo("buy milk"));
        Parser parser = createParser(tasks);

        parser.parse("delete two");

        assertEquals(1, tasks.size());
        assertTrue(output.toString().contains("Task number must be an integer"));
    }

    @Test
    public void parse_invalidCommand_printsUnknownCommand() {
        TaskList tasks = new TaskList();
        Parser parser = createParser(tasks);

        parser.parse("remind me");

        assertTrue(output.toString().contains("Unknown command"));
    }

    private Parser createParser(TaskList tasks) {
        return new Parser(new Storage(filePath), tasks);
    }
}
