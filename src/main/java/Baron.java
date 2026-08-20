import java.util.Scanner;

public class Baron {
    static void main(String[] args) {
        String line = "    ____________________________________________________________";
        String banner = """
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
        String intro = "     Hello! I'm Baron\n     I am a useful chatbot!";
        String outro = "     Bye. Hope you have a wonderful day!";

        // Prints intro
        System.out.println(line);
        System.out.println(banner);
        System.out.println(intro);
        System.out.println(line);

        int numTasks = 0;
        Task[] tasks = new Task[100];
        Scanner input = new Scanner(System.in);

        while (true) {
            // Read input
            String command = input.nextLine();
            command = command.trim().toLowerCase();
            if (command.isEmpty()) {
                continue;
            }

            // Prints output
            System.out.println(line);
            if (command.equals("bye")) {
                System.out.println(outro);
            } else if (command.equals("list")) {
                printTasks(tasks, numTasks);
            } else if (command.startsWith("mark") || command.startsWith("unmark")) {
                // Check arguments
                String[] arguments = command.split(" ");
                if (arguments.length != 2) {
                    System.out.println("     Error: Invalid number of arguments.");
                    System.out.println(line);
                    continue;
                }

                // Convert string to integer
                int taskNumber = -1;
                try {
                    taskNumber = Integer.parseInt(arguments[1]);
                } catch (NumberFormatException e) {
                    System.out.println("     Error: Task number must be an integer.");
                    System.out.println(line);
                    continue;
                }

                // Mark as done or not done
                if (taskNumber < 1 || taskNumber > numTasks) {
                    System.out.println("     Error: Invalid task number.");
                    System.out.println(line);
                    continue;
                } else if (command.startsWith("mark")) {
                    tasks[taskNumber - 1].markAsDone();
                    System.out.println("     Nice! I've marked this task as done:");
                    System.out.println("       " + tasks[taskNumber - 1]);
                } else if (command.startsWith("unmark")) {
                    tasks[taskNumber - 1].markAsNotDone();
                    System.out.println("     OK, I've marked this task as not done yet:");
                    System.out.println("       " + tasks[taskNumber - 1]);
                }
            } else { // Add task
                tasks[numTasks++] = new Task(command);
                System.out.println("     added: " + command);
            }
            System.out.println(line);

            // Terminate Baron
            if (command.equals("bye")) {
                return;
            }
        }
    }

    static void printTasks(Task[] memory, int count) {
        for (int i = 0; i < count; i++) {
            System.out.println("     " + (i + 1) + "." + memory[i]);
        }
    }
}
