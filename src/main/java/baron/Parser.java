package baron;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import baron.task.Deadline;
import baron.task.Event;
import baron.task.Task;
import baron.task.Todo;

/**
 * Interprets user commands and coordinates the task list, storage, and user interface.
 */
public class Parser {
    private final Storage storage;
    private final TaskList tasks;

    /**
     * Creates a parser that operates on the given storage and task list.
     *
     * @param storage The persistent task storage.
     * @param tasks The task list to update.
     */
    public Parser(Storage storage, TaskList tasks) {
        this.storage = storage;
        this.tasks = tasks;
    }

    /**
     * Processes one user command and displays the corresponding response.
     *
     * @param command The command entered by the user.
     * @return Whether the command ends the application.
     */
    public boolean parse(String command) {
        command = command.trim();
        try {
            if (command.equals("bye")) {
                Ui.printOutro();
            } else if (command.equals("list")) {
                Ui.printList(tasks);
            } else if (command.matches("^mark(\\s+.*)?$")) {
                handleMark(command);
            } else if (command.matches("^unmark(\\s+.*)?$")) {
                handleUnmark(command);
            } else if (command.matches("^todo(\\s+.*)?$")) {
                handleTodo(command);
            } else if (command.matches("^deadline(\\s+.*)?$")) {
                handleDeadline(command);
            } else if (command.matches("^event(\\s+.*)?$")) {
                handleEvent(command);
            } else if (command.matches("^delete(\\s+.*)?$")) {
                handleDelete(command);
            } else {
                throw new BaronException("Error: Unknown command");
            }
        } catch (BaronException e) {
            Ui.printException(e);
        }
        return command.equals("bye");
    }

    private void handleMark(String command) throws BaronException {
        int taskIndex = parseTaskNumber(getArgument("mark ", command)) - 1;
        Ui.markTaskSuccess(tasks.markTask(taskIndex));
        storage.writeTasks(tasks);
    }

    private void handleUnmark(String command) throws BaronException {
        int taskIndex = parseTaskNumber(getArgument("unmark ", command)) - 1;
        Ui.unmarkTaskSuccess(tasks.unmarkTask(taskIndex));
        storage.writeTasks(tasks);
    }

    private void handleTodo(String command) throws BaronException {
        String description = getArgument("todo ", command);
        Task task = tasks.addTask(new Todo(description));
        Ui.addTaskSuccess(task, tasks);
        storage.appendTask(task);
    }

    private void handleDeadline(String command) throws BaronException {
        String description = getArgument("deadline ", command);
        LocalDateTime deadline = parseDateTime(getArgument("/by ", command));
        Task task = tasks.addTask(new Deadline(description, deadline));
        Ui.addTaskSuccess(task, tasks);
        storage.appendTask(task);
    }

    private void handleEvent(String command) throws BaronException {
        String description = getArgument("event ", command);
        LocalDateTime fromDate = parseDateTime(getArgument("/from ", command));
        LocalDateTime toDate = parseDateTime(getArgument("/to ", command));
        if (!fromDate.isBefore(toDate)) {
            throw new BaronException("Error: /to date must be after /from date");
        }
        Task task = tasks.addTask(new Event(description, fromDate, toDate));
        Ui.addTaskSuccess(task, tasks);
        storage.appendTask(task);
    }

    private void handleDelete(String command) throws BaronException {
        int taskIndex = parseTaskNumber(getArgument("delete ", command)) - 1;
        Ui.deleteTaskSuccess(tasks.deleteTask(taskIndex), tasks);
        storage.writeTasks(tasks);
    }

    private String getArgument(String flag, String command) throws BaronException {
        StringBuilder builder = new StringBuilder();
        if (!command.contains(flag)) {
            throw new BaronException("Error: Argument for " + flag.trim() + " is missing");
        }
        for (int i = command.indexOf(flag) + flag.length(); i < command.length(); i++) {
            if (command.charAt(i) == '/') {
                break;
            }
            builder.append(command.charAt(i));
        }
        String argument = builder.toString().trim();
        if (argument.isEmpty()) {
            throw new BaronException("Error: Argument for " + flag.trim() + " is missing");
        }
        return argument;
    }

    private int parseTaskNumber(String argument) throws BaronException {
        int taskNumber;
        try {
            taskNumber = Integer.parseInt(argument);
        } catch (NumberFormatException e) {
            throw new BaronException("Error: Task number must be an integer");
        }
        if (taskNumber < 1 || taskNumber > tasks.size()) {
            throw new BaronException("Error: Invalid task number");
        }
        return taskNumber;
    }

    /**
     * Converts a date and time in the accepted command format to a {@code LocalDateTime}.
     *
     * @param dateTime The date and time supplied by the user.
     * @return The parsed date and time.
     * @throws BaronException If the input does not use the required format.
     */
    static LocalDateTime parseDateTime(String dateTime) throws BaronException {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy HHmm");
            return LocalDateTime.parse(dateTime, formatter);
        } catch (DateTimeParseException e) {
            throw new BaronException("Error: Date/time must be in ddMMyyyy HHmm");
        }
    }
}
