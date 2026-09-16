package baron.core;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.Set;

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

    /**
     * Creates a parser that uses the specified persistent storage.
     *
     * @param storage The persistent task storage.
     */
    public Parser(Storage storage) {
        this.storage = storage;
    }

    /**
     * Returns the response produced after processing the specified command.
     *
     * @param command The command to process.
     * @return The response to show to the user.
     */
    public String parse(String command) {
        assert command != null : "Commands passed from the user interface must not be null";
        try {
            return routeCommand(command);
        } catch (BaronException e) {
            return Response.respondWithBaronException(e);
        }
    }

    /** Routes the specified command to the handler responsible for it. */
    private String routeCommand(String command) throws BaronException {
        if (command.equals("bye")) {
            return Response.respondWithOutro();
        } else if (command.matches("^list(\\s+.*)?$")) {
            return handleList(command);
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
        } else if (command.matches("^specify(\\s+.*)?$")) {
            return handleSpecify(command);
        }
        throw new BaronException("Unknown command");
    }

    /** Returns the response for a list command. */
    private String handleList(String command) throws BaronException {
        if (Baron.getTasks().size() == 0) {
            throw new BaronException("There are no tasks in your list");
        }
        boolean willShowRequiredTasks = command.matches("^.*\\s+/requires(\\s+.*)?$");
        boolean willShowUnlockedTasks = command.matches("^.*\\s+/unlocks(\\s+.*)?$");
        return Response.respondWithAllTasks(Baron.getTasks(), willShowRequiredTasks, willShowUnlockedTasks);
    }

    /** Processes a mark command. */
    private String handleMark(String command) throws BaronException {
        int taskIndex = parseTaskIndex(getRequiredArgument("mark", command));
        Task markedTask = Baron.getTasks().markTask(taskIndex);
        storage.writeTasks();
        return Response.respondWithMarkedTask(markedTask);
    }

    /** Processes an unmark command. */
    private String handleUnmark(String command) throws BaronException {
        int taskIndex = parseTaskIndex(getRequiredArgument("unmark", command));
        Task unmarkedTask = Baron.getTasks().unmarkTask(taskIndex);
        storage.writeTasks();
        return Response.respondWithUnmarkedTask(unmarkedTask);
    }

    /** Processes a to-do command. */
    private String handleTodo(String command) throws BaronException {
        String description = getRequiredArgument("todo", command);
        Todo task = new Todo(description);
        Baron.getTasks().addTask(task);
        storage.writeTasks();
        return Response.respondWithAddedTask(task, Baron.getTasks());
    }

    /** Processes a deadline command. */
    private String handleDeadline(String command) throws BaronException {
        String description = getRequiredArgument("deadline", command);
        LocalDateTime deadline = parseDateTime(getRequiredArgument("/by", command));
        Deadline task = new Deadline(description, deadline);
        Baron.getTasks().addTask(task);
        storage.writeTasks();
        return Response.respondWithAddedTask(task, Baron.getTasks());
    }

    /** Processes an event command. */
    private String handleEvent(String command) throws BaronException {
        String description = getRequiredArgument("event", command);
        LocalDateTime fromDate = parseDateTime(getRequiredArgument("/from", command));
        LocalDateTime toDate = parseDateTime(getRequiredArgument("/to", command));
        if (!fromDate.isBefore(toDate)) {
            throw new BaronException("/to date must be after /from date");
        }
        Event task = new Event(description, fromDate, toDate);
        Baron.getTasks().addTask(task);
        storage.writeTasks();
        return Response.respondWithAddedTask(task, Baron.getTasks());
    }

    /** Processes a delete command. */
    private String handleDelete(String command) throws BaronException {
        int taskIndex = parseTaskIndex(getRequiredArgument("delete", command));
        Task deletedTask = Baron.getTasks().deleteTask(taskIndex);
        storage.writeTasks();
        return Response.respondWithDeletedTask(deletedTask, Baron.getTasks());
    }

    /** Processes a find command. */
    private String handleFind(String command) throws BaronException {
        String keyword = getRequiredArgument("find", command);
        TaskList matchingTasks = Baron.getTasks().findTasks(keyword);
        if (matchingTasks.size() == 0) {
            throw new BaronException("None of your tasks match '" + keyword + "'");
        }
        return Response.respondWithMatchingTasks(matchingTasks);
    }

    /** Processes a specify command. */
    private String handleSpecify(String command) throws BaronException {
        int taskIndex = parseTaskIndex(getRequiredArgument("specify", command));
        Task task = Baron.getTasks().getTasks().get(taskIndex);
        task.saveRelationships();
        task.clearRelationships();

        try {
            String requiredTaskNumbers = getRequiredArgument("/requires", command);
            if (requiredTaskNumbers.equals("-")) {
                task.setRequiredTasks(new TaskList());
            } else {
                task.setRequiredTasks(parseTaskNumbers(requiredTaskNumbers));
            }

            String unlockedTaskNumbers = getRequiredArgument("/unlocks", command);
            if (unlockedTaskNumbers.equals("-")) {
                task.setUnlockedTasks(new TaskList());
            } else {
                task.setUnlockedTasks(parseTaskNumbers(unlockedTaskNumbers));
            }
        } catch (BaronException baronException) {
            task.recoverRelationships();
            throw baronException;
        }

        storage.writeTasks();
        return Response.respondWithSpecifiedTask(task);
    }

    /** Returns the non-blank argument that follows the specified command flag. */
    private String getRequiredArgument(String flag, String command) throws BaronException {
        if (flag.charAt(0) == '/' && !command.matches("^.*\\s+" + flag + "(\\s+.*)?$")) {
            throw new BaronException("Missing flag " + flag);
        }

        int argumentStartIndex = command.indexOf(flag) + flag.length();
        int nextFlagIndex = command.indexOf('/', argumentStartIndex);
        int argumentEndIndex = nextFlagIndex == -1 ? command.length() : nextFlagIndex;
        String argument = command.substring(argumentStartIndex, argumentEndIndex);

        if (argument.trim().isEmpty()) {
            throw new BaronException("Missing argument for " + flag);
        }
        return argument.trim();
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

    /** Returns the task index parsed from the specified argument. */
    private int parseTaskIndex(String taskNumber) throws BaronException {
        try {
            int index = Integer.parseInt(taskNumber) - 1;
            Baron.getTasks().checkTaskIndex(index);
            return index;
        } catch (NumberFormatException e) {
            throw new BaronException("Task number must be an integer");
        }
    }

    /** Returns the tasks for the task numbers supplied in the argument. */
    private TaskList parseTaskNumbers(String argument) throws BaronException {
        String[] taskNumbers = argument.split("\\s+");

        int[] indices = new int[taskNumbers.length];
        for (int i = 0; i < taskNumbers.length; i++) {
            indices[i] = parseTaskIndex(taskNumbers[i]);
        }

        Set<Integer> setOfIndices = new HashSet<>();
        for (int index : indices) {
            setOfIndices.add(index);
        }

        TaskList tasks = new TaskList();
        for (int index : setOfIndices) {
            tasks.addTask(Baron.getTasks().getTasks().get(index));
        }
        return tasks;
    }
}
