package baron;

import baron.task.Task;

/**
 * Displays Baron messages in the command-line interface.
 */
public class Ui {
    private static final int LINE_INDENT_SIZE = 4;
    private static final int TEXT_INDENT_SIZE = 5;
    private static final int TASK_INDENT_SIZE = 7;
    private static final String BANNER = """
                ########      ######    ########      ######    ##      ## \s
                ########      ######    ########      ######    ##      ## \s
                ##      ##  ##      ##  ##      ##  ##      ##  ####    ## \s
                ##      ##  ##      ##  ##      ##  ##      ##  ####    ## \s
                ########    ##########  ########    ##      ##  ##  ##  ## \s
                ########    ##########  ########    ##      ##  ##  ##  ## \s
                ##      ##  ##      ##  ##    ##    ##      ##  ##    #### \s
                ##      ##  ##      ##  ##    ##    ##      ##  ##    #### \s
                ########    ##      ##  ##     ##     ######    ##      ## \s
                ########    ##      ##  ##      ##    ######    ##      ## \s
                """;
    private static final String INTRO = "Hi! My name is Baron.\nWhat can I do for you?";
    private static final String OUTRO = "Bye. Hope you have a wonderful day!";
    private static final String LINE = "____________________________________________________________";

    /**
     * Prevents instantiation of this utility class.
     */
    private Ui() {
    }

    /** Displays the welcome message. */
    public static void printIntro() {
        System.out.print(LINE.indent(LINE_INDENT_SIZE));
        System.out.print(BANNER.indent(TEXT_INDENT_SIZE));
        System.out.print(INTRO.indent(TEXT_INDENT_SIZE));
        System.out.print(LINE.indent(LINE_INDENT_SIZE));
    }

    /** Displays the farewell message. */
    public static void printOutro() {
        System.out.print(LINE.indent(LINE_INDENT_SIZE));
        System.out.print(OUTRO.indent(TEXT_INDENT_SIZE));
        System.out.print(LINE.indent(LINE_INDENT_SIZE));
    }

    /**
     * Displays all tasks in the given task list.
     *
     * @param tasks The task list to display.
     */
    public static void printAllTasks(TaskList tasks) {
        System.out.print(LINE.indent(LINE_INDENT_SIZE));
        System.out.print("Here are the tasks in your list:".indent(TEXT_INDENT_SIZE));
        System.out.print(tasks.toString().indent(TEXT_INDENT_SIZE));
        System.out.print(LINE.indent(LINE_INDENT_SIZE));
    }

    /**
     * Displays confirmation that a task was marked as completed.
     *
     * @param task The marked task.
     */
    public static void printMarkedTask(Task task) {
        System.out.print(LINE.indent(LINE_INDENT_SIZE));
        System.out.print("Nice! I've marked this task as done:".indent(TEXT_INDENT_SIZE));
        System.out.print(task.toString().indent(TASK_INDENT_SIZE));
        System.out.print(LINE.indent(LINE_INDENT_SIZE));
    }

    /**
     * Displays confirmation that a task was marked as not completed.
     *
     * @param task The unmarked task.
     */
    public static void printUnmarkedTask(Task task) {
        System.out.print(LINE.indent(LINE_INDENT_SIZE));
        System.out.print("OK, I've marked this task as not done yet:".indent(TEXT_INDENT_SIZE));
        System.out.print(task.toString().indent(TASK_INDENT_SIZE));
        System.out.print(LINE.indent(LINE_INDENT_SIZE));
    }

    /**
     * Displays confirmation that a task was added.
     *
     * @param task The added task.
     * @param tasks The updated task list.
     */
    public static void printAddedTask(Task task, TaskList tasks) {
        System.out.print(LINE.indent(LINE_INDENT_SIZE));
        System.out.print("Got it. I've added this task:".indent(TEXT_INDENT_SIZE));
        System.out.print(task.toString().indent(TASK_INDENT_SIZE));
        System.out.print(("Now you have " + tasks.size() + " tasks in the list").indent(TEXT_INDENT_SIZE));
        System.out.print(LINE.indent(LINE_INDENT_SIZE));
    }

    /**
     * Displays confirmation that a task was removed.
     *
     * @param task The removed task.
     * @param tasks The updated task list.
     */
    public static void printDeletedTask(Task task, TaskList tasks) {
        System.out.print(LINE.indent(LINE_INDENT_SIZE));
        System.out.print("Noted. I've removed this task:".indent(TEXT_INDENT_SIZE));
        System.out.print(task.toString().indent(TASK_INDENT_SIZE));
        System.out.print(("Now you have " + tasks.size() + " tasks in the list").indent(TEXT_INDENT_SIZE));
        System.out.print(LINE.indent(LINE_INDENT_SIZE));
    }

    /**
     * Displays tasks whose descriptions match a search keyword.
     *
     * @param tasks The matching tasks to display.
     */
    public static void printMatchingTasks(TaskList tasks) {
        System.out.print(LINE.indent(LINE_INDENT_SIZE));
        System.out.print("Here are the matching tasks in your list:".indent(TEXT_INDENT_SIZE));
        System.out.print(tasks.toString().indent(TASK_INDENT_SIZE));
        System.out.print(LINE.indent(LINE_INDENT_SIZE));
    }

    /**
     * Displays a user-facing command error.
     *
     * @param exception The error to display.
     */
    public static void printBaronException(BaronException exception) {
        System.out.print(LINE.indent(LINE_INDENT_SIZE));
        System.out.print(exception.getMessage().indent(TEXT_INDENT_SIZE));
        System.out.print(LINE.indent(LINE_INDENT_SIZE));
    }
}
