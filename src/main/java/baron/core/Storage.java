package baron.core;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import baron.core.exception.BaronException;
import baron.core.task.Deadline;
import baron.core.task.Event;
import baron.core.task.Task;
import baron.core.task.TaskList;
import baron.core.task.TaskType;
import baron.core.task.Todo;

/**
 * Reads and writes Baron tasks in a text file.
 */
class Storage {
    private static final String FIELD_SEPARATOR = " \\| ";
    private static final String TASK_UUID_SEPARATOR = ", ";
    private static final String COMPLETED_TASK_STATUS = "1";

    private static final int TASK_UUID_FIELD_INDEX = 0;
    private static final int TASK_TYPE_FIELD_INDEX = 1;
    private static final int TASK_STATUS_FIELD_INDEX = 2;
    private static final int TASK_DESCRIPTION_FIELD_INDEX = 3;
    private static final int REQUIRED_TASKS_FIELD_INDEX = 4;
    private static final int DEADLINE_FIELD_INDEX = 5;
    private static final int EVENT_START_FIELD_INDEX = 5;
    private static final int EVENT_END_FIELD_INDEX = 6;

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
     * Reads saved tasks and adds them to Baron's list of tasks.
     */
    public void readTasks() {
        String taskStrings;
        try {
            taskStrings = Files.readString(filePath, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return;
        }

        Map<UUID, Task> uuidToTask = new HashMap<>();
        for (String taskString : taskStrings.split(System.lineSeparator())) {
            if (taskString.isBlank()) {
                continue;
            }
            try {
                Task task = parseTaskString(taskString);
                uuidToTask.put(task.getUuid(), task);
                Baron.TASKS.addTask(task);
            } catch (BaronException e) {
                System.out.println(e.getMessage());
            }
        }

        for (String taskString : taskStrings.split(System.lineSeparator())) {
            if (taskString.isBlank()) {
                continue;
            }
            establishRequirements(taskString, uuidToTask);
        }
    }

    /**
     * Replaces the saved tasks with the current contents of Baron's task list.
     */
    public void writeTasks() {
        try {
            Files.writeString(filePath, Baron.TASKS.toFileString(), StandardCharsets.UTF_8);
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
        String[] taskFields = taskString.split(FIELD_SEPARATOR, -1);
        try {
            TaskType taskType = TaskType.fromFileCode(taskFields[TASK_TYPE_FIELD_INDEX]);
            Task task = switch (taskType) {
                case TODO -> new Todo(UUID.fromString(taskFields[TASK_UUID_FIELD_INDEX]),
                        taskFields[TASK_DESCRIPTION_FIELD_INDEX]);
                case DEADLINE -> new Deadline(UUID.fromString(taskFields[TASK_UUID_FIELD_INDEX]),
                        taskFields[TASK_DESCRIPTION_FIELD_INDEX],
                        LocalDateTime.parse(taskFields[DEADLINE_FIELD_INDEX]));
                case EVENT -> new Event(
                        UUID.fromString(taskFields[TASK_UUID_FIELD_INDEX]),
                        taskFields[TASK_DESCRIPTION_FIELD_INDEX],
                        LocalDateTime.parse(taskFields[EVENT_START_FIELD_INDEX]),
                        LocalDateTime.parse(taskFields[EVENT_END_FIELD_INDEX]));
            };
            if (taskFields[TASK_STATUS_FIELD_INDEX].equals(COMPLETED_TASK_STATUS)) {
                task.markAsDone();
            }
            return task;
        } catch (ArrayIndexOutOfBoundsException
                | DateTimeParseException
                | IllegalArgumentException e) {
            throw new BaronException("Invalid task '" + taskString + "'");
        }
    }

    /**
     * Restores the prerequisite relationships in a saved task record.
     *
     * @param taskString The saved task record.
     * @param uuidToTask The tasks indexed by their identifiers.
     */
    private void establishRequirements(String taskString, Map<UUID, Task> uuidToTask) {
        try {
            String[] taskFields = taskString.split(FIELD_SEPARATOR, -1);
            Task task = uuidToTask.get(UUID.fromString(taskFields[TASK_UUID_FIELD_INDEX]));
            if (task == null || taskFields[REQUIRED_TASKS_FIELD_INDEX].isBlank()) {
                return;
            }
            TaskList requiredTasks = new TaskList(
                    Arrays.stream(taskFields[REQUIRED_TASKS_FIELD_INDEX].split(TASK_UUID_SEPARATOR))
                            .map(uuid -> uuidToTask.get(UUID.fromString(uuid))).toList());
            task.setRequiredTasks(requiredTasks);
        } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException | BaronException e) {
            System.out.println(e.getMessage());
        }
    }
}
