package baron.core;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import baron.core.exception.BaronException;
import baron.core.task.Deadline;
import baron.core.task.Event;
import baron.core.task.Task;
import baron.core.task.TaskList;
import baron.core.task.Todo;

/**
 * Parses user commands and updates the task list and persistent storage.
 */
class Parser {
    private final Storage storage;
    private final TaskList tasks;

    /**
     * Creates a parser that uses the specified storage and task list.
     *
     * @param storage The persistent task storage.
     * @param tasks The task list to update.
     */
    public Parser(Storage storage, TaskList tasks) {
        this.storage = storage;
        this.tasks = tasks;
    }

    /**
     * Returns the response produced after processing the specified command.
     *
     * @param command The command to process.
     * @return The response to show to the user.
     */
    public String parse(String command) {
        try {
            if (command.equals("bye")) {
                return Response.respondWithOutro();
            } else if (command.equals("list")) {
                return handleList();
            } else if (command.matches("^mark(\\s+.*)?$")) {
                return handleMark(command);
            } else if (command.matches("^unmark(\\s+.*)?$")) {
                return handleUnmark(command);
            } else if (command.matches("^todo(\\s+.*)?$")) {
                return handleTodo(command);
            } else if (command.matches("^deadline(\\s+.*)?$")) {
                return handleDeadline(command);
            } else if (command.matches("^event(\\s+.*)?$")) {
                return handleEvent(command);
            } else if (command.matches("^delete(\\s+.*)?$")) {
                return handleDelete(command);
            } else if (command.matches("^find(\\s+.*)?$")) {
                return handleFind(command);
            } else {
                throw new BaronException("Unknown command");
            }
        } catch (BaronException e) {
            return Response.respondWithBaronException(e);
        }
    }

    /** Returns the response for a list command. */
    private String handleList() throws BaronException {
        if (tasks.size() == 0) {
            throw new BaronException("There are no tasks in your list");
        }
        return Response.respondWithAllTasks(tasks);
    }

    /** Processes a mark command. */
    private String handleMark(String command) throws BaronException {
        int taskIndex = parseTaskNumber(getArgument("mark ", command)) - 1;
        Task markedTask = tasks.markTask(taskIndex);
        storage.writeTasks(tasks);
        return Response.respondWithMarkedTask(markedTask);
    }

    /** Processes an unmark command. */
    private String handleUnmark(String command) throws BaronException {
        int taskIndex = parseTaskNumber(getArgument("unmark ", command)) - 1;
        Task unmarkedTask = tasks.unmarkTask(taskIndex);
        storage.writeTasks(tasks);
        return Response.respondWithUnmarkedTask(unmarkedTask);
    }

    /** Processes a to-do command. */
    private String handleTodo(String command) throws BaronException {
        String description = getArgument("todo ", command);
        Task addedTask = tasks.addTask(new Todo(description));
        storage.appendTask(addedTask);
        return Response.respondWithAddedTask(addedTask, tasks);
    }

    /** Processes a deadline command. */
    private String handleDeadline(String command) throws BaronException {
        String description = getArgument("deadline ", command);
        LocalDateTime deadline = parseDateTime(getArgument("/by ", command));
        Task addedTask = tasks.addTask(new Deadline(description, deadline));
        storage.appendTask(addedTask);
        return Response.respondWithAddedTask(addedTask, tasks);
    }

    /** Processes an event command. */
    private String handleEvent(String command) throws BaronException {
        String description = getArgument("event ", command);
        LocalDateTime fromDate = parseDateTime(getArgument("/from ", command));
        LocalDateTime toDate = parseDateTime(getArgument("/to ", command));
        if (!fromDate.isBefore(toDate)) {
            throw new BaronException("/to date must be after /from date");
        }
        Task addedTask = tasks.addTask(new Event(description, fromDate, toDate));
        storage.appendTask(addedTask);
        return Response.respondWithAddedTask(addedTask, tasks);
    }

    /** Processes a delete command. */
    private String handleDelete(String command) throws BaronException {
        int taskIndex = parseTaskNumber(getArgument("delete ", command)) - 1;
        Task deletedTask = tasks.deleteTask(taskIndex);
        storage.writeTasks(tasks);
        return Response.respondWithDeletedTask(deletedTask, tasks);
    }

    /** Processes a find command. */
    private String handleFind(String command) throws BaronException {
        String keyword = getArgument("find ", command);
        TaskList matchingTasks = tasks.findTasks(keyword);
        if (matchingTasks.size() == 0) {
            throw new BaronException("None of your tasks match '" + keyword + "'");
        }
        return Response.respondWithMatchingTasks(matchingTasks);
    }

    /** Returns the non-blank argument that follows the specified command flag. */
    private String getArgument(String flag, String command) throws BaronException {
        StringBuilder builder = new StringBuilder();
        if (!command.contains(flag)) {
            throw new BaronException("Argument for " + flag.trim() + " is missing");
        }
        for (int i = command.indexOf(flag) + flag.length(); i < command.length(); i++) {
            if (command.charAt(i) == '/') {
                break;
            }
            builder.append(command.charAt(i));
        }
        String argument = builder.toString().trim();
        if (argument.isEmpty()) {
            throw new BaronException("Argument for " + flag.trim() + " is missing");
        }
        return argument;
    }

    /** Returns a valid zero-based task number parsed from the specified argument. */
    private int parseTaskNumber(String argument) throws BaronException {
        int taskNumber;
        try {
            taskNumber = Integer.parseInt(argument);
        } catch (NumberFormatException e) {
            throw new BaronException("Task number must be an integer");
        }
        if (taskNumber < 1 || taskNumber > tasks.size()) {
            throw new BaronException("Invalid task number");
        }
        return taskNumber;
    }

    /** Returns a date and time parsed from the specified command argument. */
    private LocalDateTime parseDateTime(String dateTime) throws BaronException {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy HHmm");
            return LocalDateTime.parse(dateTime, formatter);
        } catch (DateTimeParseException e) {
            throw new BaronException("Date/time must be in ddMMyyyy HHmm");
        }
    }
}
