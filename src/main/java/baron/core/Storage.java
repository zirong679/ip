package baron.core;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import baron.core.exception.BaronException;
import baron.core.task.Deadline;
import baron.core.task.Event;
import baron.core.task.Task;
import baron.core.task.TaskList;
import baron.core.task.Todo;

/**
 * Reads and writes Baron tasks in a text file.
 */
class Storage {
    private final Path filePath;

    /**
     * Creates storage for the specified task file, creating it if necessary.
     *
     * @param filePath The path to the task data file.
     */
    public Storage(Path filePath) {
        this.filePath = filePath;
        try {
            Files.createDirectories(filePath.getParent());
            if (Files.notExists(filePath)) {
                Files.createFile(filePath);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Reads saved tasks and adds them to the given task list.
     *
     * @param tasks The task list to populate.
     * @throws BaronException If a saved task has an invalid format.
     */
    public void readTasks(TaskList tasks) throws BaronException {
        String taskStrings = "";
        try {
            taskStrings = Files.readString(filePath, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        for (String taskString : taskStrings.split("\\R")) {
            Task task = parseTaskString(taskString);
            if (task != null) {
                tasks.addTask(task);
            }
        }
    }

    /**
     * Replaces the saved tasks with the contents of the given task list.
     *
     * @param tasks The task list to save.
     */
    public void writeTasks(TaskList tasks) {
        try {
            Files.writeString(filePath, tasks.toFileString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Adds one task to the end of the saved task file.
     *
     * @param task The task to save.
     */
    public void appendTask(Task task) {
        try {
            Files.writeString(
                    filePath,
                    task.toFileString() + System.lineSeparator(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Returns a task reconstructed from one saved task record.
     *
     * @param taskString The saved task record.
     * @return The reconstructed task, or {@code null} for a blank record.
     * @throws BaronException If the record has an invalid format.
     */
    private Task parseTaskString(String taskString) throws BaronException {
        if (taskString.isBlank()) {
            return null;
        }
        String[] taskFields = taskString.split(" \\| ");
        try {
            Task task = switch (taskFields[0]) {
                case "T" -> new Todo(taskFields[2]);
                case "D" -> new Deadline(taskFields[2], LocalDateTime.parse(taskFields[3]));
                case "E" -> new Event(
                        taskFields[2],
                        LocalDateTime.parse(taskFields[3]),
                        LocalDateTime.parse(taskFields[4])
                );
                default -> throw new BaronException("Unknown task");
            };
            if (taskFields[1].equals("1")) {
                task.markAsDone();
            }
            return task;
        } catch (ArrayIndexOutOfBoundsException | DateTimeParseException | BaronException e) {
            throw new BaronException("Invalid task '" + taskString + "'");
        }
    }
}
