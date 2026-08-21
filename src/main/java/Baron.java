import java.util.Scanner;

public class Baron {
    static int numTasks = 0;
    static Task[] tasks = new Task[100];

    static String banner = """
                ########      ######    ########      ######    ##      ## \s
                ########      ######    ########      ######    ##      ## \s
                ##      ##  ##      ##  ##      ##  ##      ##  ####    ## \s
                ##      ##  ##      ##  ##      ##  ##      ##  ####    ## \s
                ########    ##########  ########    ##      ##  ##  ##  ## \s
                ########    ##########  ########    ##      ##  ##  ##  ## \s
                ##      ##  ##      ##  ##    ##    ##      ##  ##    #### \s
                ##      ##  ##      ##  ##    ##    ##      ##  ##    #### \s
                ########    ##      ##  ##      ##    ######    ##      ## \s
                ########    ##      ##  ##      ##    ######    ##      ## \s
                """;
    static String intro = "Hello! I'm Baron\nI am a useful chatbot!";
    static String outro = "Bye. Hope you have a wonderful day!";
    static String line = "____________________________________________________________";

    static final int INDENT_SIZE = 4;

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
            if (command.equals("bye")) {
                System.out.print(outro.indent(INDENT_SIZE + 1));
            } else if (command.equals("list")) {
                handleList();
            } else if (command.startsWith("mark ")){
                handleMark(command);
            } else if (command.startsWith("unmark ")) {
                handleUnmark(command);
            } else if (command.startsWith("todo ")) {
                handleToDo(command);
            } else if (command.startsWith("deadline ")) {
                handleDeadline(command);
            } else if (command.startsWith("event ")) {
                handleEvent(command);
            } else {
                System.out.print("Error: Unknown command".indent(INDENT_SIZE + 1));
            }
            System.out.print(line.indent(INDENT_SIZE));

            // Terminate Baron
            if (command.equals("bye")) {
                return;
            }
        }
    }

    static void handleList() {
        if (numTasks == 0) {
            System.out.print("There are no tasks in your list.".indent(INDENT_SIZE + 1));
        } else {
            System.out.print("Here are the tasks in your list:".indent(INDENT_SIZE + 1));
            for (int i = 0; i < numTasks; i++) {
                System.out.print(((i + 1) + "." + tasks[i]).indent(INDENT_SIZE + 1));
            }
        }
    }

    static int parseTaskNumber(String argument) {
        if (argument.isEmpty()) {
            System.out.print("Error: Missing task number".indent(INDENT_SIZE + 1));
            return -1;
        }
        int taskNumber;
        try {
            taskNumber = Integer.parseInt(argument);
        } catch (NumberFormatException e) {
            System.out.print("Error: Task number must be an integer".indent(INDENT_SIZE + 1));
            return -1;
        }
        if (taskNumber < 1 || taskNumber > numTasks) {
            System.out.print("Error: Invalid task number".indent(INDENT_SIZE + 1));
            return -1;
        }
        return taskNumber;
    }

    static void handleMark(String command) {
        String argument = command.substring(4).trim();
        int taskNumber = parseTaskNumber(argument);
        if (taskNumber == -1) {
            return;
        }

        // Mark task as done
        tasks[taskNumber - 1].markAsDone();
        System.out.print("Nice! I've marked this task as done:".indent(INDENT_SIZE + 1));
        System.out.print(tasks[taskNumber - 1].toString().indent(INDENT_SIZE + 3));
    }

    static void handleUnmark(String command) {
        String argument = command.substring(6).trim();
        int taskNumber = parseTaskNumber(argument);
        if (taskNumber == -1) {
            return;
        }

        // Mark task as not done
        tasks[taskNumber - 1].markAsNotDone();
        System.out.print("OK, I've marked this task as not done yet:".indent(INDENT_SIZE + 1));
        System.out.print(tasks[taskNumber - 1].toString().indent(INDENT_SIZE + 3));
    }

    static void addTask(Task task) {
        tasks[numTasks++] = task;
        System.out.print("Got it. I've added this task:".indent(INDENT_SIZE + 1));
        System.out.print(task.toString().indent(INDENT_SIZE + 3));
        System.out.print(("Now you have " + numTasks + " tasks in the list").indent(INDENT_SIZE + 1));
    }

    static void handleToDo(String command) {
        // Extract description
        String description = command.substring(4).trim();
        if (description.isEmpty()) {
            System.out.print("Error: Missing task description".indent(INDENT_SIZE + 1));
            return;
        }

        // Add to-do task
        Baron.addTask(new ToDo(description));
    }

    static void handleDeadline(String command) {
        // Extract /by flag
        if (!command.contains(" /by ")) {
            System.out.print("Error: Missing deadline /by flag".indent(INDENT_SIZE + 1));
            return;
        }
        int byFlagIndex = command.indexOf(" /by ");

        // Extract description and deadline
        String description = command.substring(8, byFlagIndex).trim();
        String deadline =  command.substring(byFlagIndex + 5).trim();
        if (description.isEmpty() || deadline.isEmpty()) {
            if (description.isEmpty()) {
                System.out.print("Error: Missing deadline description".indent(INDENT_SIZE + 1));
            }
            if (deadline.isEmpty()) {
                System.out.print("Error: Missing deadline date/time".indent(INDENT_SIZE + 1));
            }
            return;
        }

        // Add deadline task
        Baron.addTask(new Deadline(description, deadline));
    }

    static void handleEvent(String command) {
        // Extract /from and /to flags
        if (!command.contains(" /to ") || !command.contains(" /from ")) {
            if (!command.contains(" /from ")) {
                System.out.print("Error: Missing event /from flag".indent(INDENT_SIZE + 1));
            }
            if (!command.contains(" /to ")) {
                System.out.print("Error: Missing event /to flag".indent(INDENT_SIZE + 1));
            }
            return;
        }
        int fromFlagIndex = command.indexOf(" /from ");
        int toFlagIndex = command.indexOf(" /to ");

        // Extract description, fromDate and toDate
        String description, fromDate, toDate;
        if (fromFlagIndex < toFlagIndex) {
            description = command.substring(5, fromFlagIndex).trim();
            fromDate = command.substring(fromFlagIndex + 7, toFlagIndex).trim();
            toDate = command.substring(toFlagIndex + 5).trim();
        } else {
            description = command.substring(5, toFlagIndex).trim();
            toDate = command.substring(toFlagIndex + 5, fromFlagIndex).trim();
            fromDate = command.substring(fromFlagIndex + 7).trim();
        }
        if (description.isEmpty() || fromDate.isEmpty() || toDate.isEmpty()) {
            if (description.isEmpty()) {
                System.out.print("Error: Missing event description".indent(INDENT_SIZE + 1));
            }
            if (fromDate.isEmpty()) {
                System.out.print("Error: Missing event from date/time".indent(INDENT_SIZE + 1));
            }
            if (toDate.isEmpty()) {
                System.out.print("Error: Missing event to date/time".indent(INDENT_SIZE + 1));
            }
            return;
        }

        // Add event task
        Baron.addTask(new Event(description, fromDate, toDate));
    }
}
