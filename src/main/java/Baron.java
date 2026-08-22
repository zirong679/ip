import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import Task.Deadline;
import Task.Event;
import Task.Task;
import Task.ToDo;

public class Baron {
    static final int INDENT_SIZE = 4;
    static String banner = """
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
    static String intro = "Hello! I'm Baron\nI am a useful chatbot!";
    static String outro = "Bye. Hope you have a wonderful day!";
    static String line = "____________________________________________________________";
    static List<Task> tasks = new ArrayList<Task>();

    static void main(String[] args) {
        // Prints intro
        System.out.print(line.indent(INDENT_SIZE));
        System.out.print(banner.indent(INDENT_SIZE + 1));
        System.out.print(intro.indent(INDENT_SIZE + 1));
        System.out.print(line.indent(INDENT_SIZE));

        Scanner input = new Scanner(System.in);
        while (true) {
            // Read input
            String command = input.nextLine().trim();

            // Prints output
            System.out.print(line.indent(INDENT_SIZE));
            try {
                if (command.equals("bye")) {
                    System.out.print(outro.indent(INDENT_SIZE + 1));
                } else if (command.equals("list")) {
                    handleList();
                } else if (command.startsWith("mark ") || command.equals("mark")) {
                    handleMark(command);
                } else if (command.startsWith("unmark ") || command.equals("unmark")) {
                    handleUnmark(command);
                } else if (command.startsWith("todo ") || command.equals("todo")) {
                    handleToDo(command);
                } else if (command.startsWith("deadline ") || command.equals("deadline")) {
                    handleDeadline(command);
                } else if (command.startsWith("event ") || command.equals("event")) {
                    handleEvent(command);
                } else if (command.startsWith("delete ") || command.equals("delete")) {
                    handleDelete(command);
                } else {
                    throw new BaronException("Error: Unknown command");
                }
            } catch (BaronException e) {
                System.out.print(e.getMessage().indent(INDENT_SIZE + 1));
            }
            System.out.print(line.indent(INDENT_SIZE));

            // Terminate Baron
            if (command.equals("bye")) {
                return;
            }
        }
    }

    static void handleList() {
        if (tasks.isEmpty()) {
            System.out.print("There are no tasks in your list".indent(INDENT_SIZE + 1));
        } else {
            System.out.print("Here are the tasks in your list:".indent(INDENT_SIZE + 1));
            for (int i = 0; i < tasks.size(); i++) {
                System.out.print(((i + 1) + "." + tasks.get(i)).indent(INDENT_SIZE + 1));
            }
        }
    }

    static int parseTaskNumber(String argument) throws BaronException {
        if (argument.isEmpty()) {
            throw new BaronException("Error: Missing task number");
        }
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

    static void handleMark(String command) throws BaronException {
        String argument = command.substring(4).trim();
        int taskNumber = parseTaskNumber(argument);

        // Mark task as done
        tasks.get(taskNumber - 1).markAsDone();
        System.out.print("Nice! I've marked this task as done:".indent(INDENT_SIZE + 1));
        System.out.print(tasks.get(taskNumber - 1).toString().indent(INDENT_SIZE + 3));
    }

    static void handleUnmark(String command) throws BaronException {
        String argument = command.substring(6).trim();
        int taskNumber = parseTaskNumber(argument);

        // Mark task as not done
        tasks.get(taskNumber - 1).markAsNotDone();
        System.out.print("OK, I've marked this task as not done yet:".indent(INDENT_SIZE + 1));
        System.out.print(tasks.get(taskNumber - 1).toString().indent(INDENT_SIZE + 3));
    }

    static void addTask(Task task) {
        tasks.add(task);
        System.out.print("Got it. I've added this task:".indent(INDENT_SIZE + 1));
        System.out.print(task.toString().indent(INDENT_SIZE + 3));
        System.out.print(("Now you have " + tasks.size() + " tasks in the list").indent(INDENT_SIZE + 1));
    }

    static void handleToDo(String command) throws BaronException {
        // Extract description
        String description = command.substring(4).trim();
        if (description.isEmpty()) {
            throw new BaronException("Error: Missing task description");
        }

        // Add to-do task
        Baron.addTask(new ToDo(description));
    }

    static void handleDeadline(String command) throws BaronException {
        // Extract /by flag
        if (!command.contains(" /by ")) {
            throw new BaronException("Error: Missing /by flag for deadline");
        }
        int byFlagIndex = command.indexOf(" /by ");

        // Extract description and deadline
        String description = command.substring(8, byFlagIndex).trim();
        String deadline = command.substring(byFlagIndex + 4).trim();
        if (description.isEmpty()) {
            throw new BaronException("Error: Missing deadline description");
        }
        if (deadline.isEmpty()) {
            throw new BaronException("Error: Missing deadline date/time");
        }

        // Add deadline task
        Baron.addTask(new Deadline(description, deadline));
    }

    static void handleEvent(String command) throws BaronException {
        // Extract /from and /to flags
        if (!command.contains(" /from ")) {
            throw new BaronException("Error: Missing /from flag for event");
        }
        if (!command.contains(" /to ")) {
            throw new BaronException("Error: Missing /to flag for event");
        }
        int fromFlagIndex = command.indexOf(" /from ");
        int toFlagIndex = command.indexOf(" /to ");

        // Extract description, fromDate and toDate
        String description, fromDate, toDate;
        if (fromFlagIndex < toFlagIndex) {
            description = command.substring(5, fromFlagIndex).trim();
            fromDate = command.substring(fromFlagIndex + 6, toFlagIndex).trim();
            toDate = command.substring(toFlagIndex + 4).trim();
        } else {
            description = command.substring(5, toFlagIndex).trim();
            toDate = command.substring(toFlagIndex + 4, fromFlagIndex).trim();
            fromDate = command.substring(fromFlagIndex + 6).trim();
        }
        if (description.isEmpty()) {
            throw new BaronException("Error: Missing event description");
        }
        if (fromDate.isEmpty()) {
            throw new BaronException("Error: Missing event from date/time");
        }
        if (toDate.isEmpty()) {
            throw new BaronException("Error: Missing event to date/time");
        }

        // Add event task
        Baron.addTask(new Event(description, fromDate, toDate));
    }

    static void handleDelete(String command) throws BaronException {
        String argument = command.substring(6).trim();
        int taskNumber = parseTaskNumber(argument);

        // Delete task from tasks
        Task removed = tasks.remove(taskNumber - 1);
        System.out.print("Noted. I've removed this task:".indent(INDENT_SIZE + 1));
        System.out.print(removed.toString().indent(INDENT_SIZE + 3));
        System.out.print(("Now you have " + tasks.size() + " tasks in the list").indent(INDENT_SIZE + 1));
    }
}
