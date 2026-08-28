package baron;

import java.nio.file.Path;
import java.util.Scanner;

/**
 * Starts Baron and coordinates its input loop.
 */
public class Baron {
    private final Parser parser;

    /**
     * Creates Baron using the given file for persistent task storage.
     *
     * @param path The path to the task data file.
     */
    private Baron(Path path) {
        Storage storage = new Storage(path);
        TaskList tasks = new TaskList();
        try {
            storage.readTasks(tasks);
        } catch (BaronException e) {
            System.err.println(e.getMessage());
        }
        parser = new Parser(storage, tasks);
    }

    /**
     * Runs Baron until the user enters the exit command.
     */
    private void run() {
        Ui.printIntro();
        Scanner scanner = new Scanner(System.in);
        boolean isDone = parser.parse(scanner.nextLine());
        while (!isDone) {
            isDone = parser.parse(scanner.nextLine());
        }
    }

    /**
     * Starts the Baron application.
     */
    static void main() {
        new Baron(Path.of("data", "tasks.txt")).run();
    }
}
