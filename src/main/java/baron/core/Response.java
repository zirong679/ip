package baron.core;

import java.util.List;

import baron.core.exception.BaronException;
import baron.core.task.Task;
import baron.core.task.TaskList;

/**
 * Creates the user-facing messages returned by Baron.
 */
class Response {
    private static final String INTRO = "Hi! My name is Baron.\nWhat can I do for you?";
    private static final String OUTRO = "Bye. Hope you have a wonderful day!";

    /**
     * Returns Baron's introductory message.
     *
     * @return The introductory message.
     */
    public static String respondWithIntro() {
        return INTRO;
    }

    /**
     * Returns Baron's farewell message.
     *
     * @return The farewell message.
     */
    public static String respondWithOutro() {
        return OUTRO;
    }

    /**
     * Returns a message containing all tasks in the specified list.
     *
     * @param tasks The tasks to display.
     * @return The formatted task-list message.
     */
    public static String respondWithAllTasks(TaskList tasks) {
        String message = "Here are the tasks in your list:";
        List<String> lines = List.of(message, tasks.toString());
        return String.join("\n", lines);
    }

    /**
     * Returns a message confirming that the specified task is complete.
     *
     * @param task The task that was marked complete.
     * @return The confirmation message.
     */
    public static String respondWithMarkedTask(Task task) {
        String message = "Nice! I've marked this task as done:";
        List<String> lines = List.of(message, task.toString());
        return String.join("\n", lines);
    }

    /**
     * Returns a message confirming that the specified task is not complete.
     *
     * @param task The task that was marked incomplete.
     * @return The confirmation message.
     */
    public static String respondWithUnmarkedTask(Task task) {
        String message = "OK, I've marked this task as not done yet:";
        List<String> lines = List.of(message, task.toString());
        return String.join("\n", lines);
    }

    /**
     * Returns a message confirming that a task was added.
     *
     * @param task The task that was added.
     * @param tasks The updated task list.
     * @return The confirmation message.
     */
    public static String respondWithAddedTask(Task task, TaskList tasks) {
        String message = "Got it. I've added this task:";
        String numTasks = "Now you have " + tasks.size() + " tasks in the list";
        List<String> lines = List.of(message, task.toString(), numTasks);
        return String.join("\n", lines);
    }

    /**
     * Returns a message confirming that a task was removed.
     *
     * @param task The task that was removed.
     * @param tasks The updated task list.
     * @return The confirmation message.
     */
    public static String respondWithDeletedTask(Task task, TaskList tasks) {
        String message = "Noted. I've removed this task:";
        String numTasks = "Now you have " + tasks.size() + " tasks in the list";
        List<String> lines = List.of(message, task.toString(), numTasks);
        return String.join("\n", lines);
    }

    /**
     * Returns a message containing the tasks that match a search.
     *
     * @param tasks The matching tasks to display.
     * @return The formatted search-results message.
     */
    public static String respondWithMatchingTasks(TaskList tasks) {
        String message = "Here are the matching tasks in your list:";
        List<String> lines = List.of(message, tasks.toString());
        return String.join("\n", lines);
    }

    /**
     * Returns the message associated with the specified command error.
     *
     * @param exception The command error.
     * @return The error message.
     */
    public static String respondWithBaronException(BaronException exception) {
        return exception.getMessage();
    }
}
