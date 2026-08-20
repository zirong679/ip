import java.util.Scanner;

public class Baron {
    static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        String[] memory = new String[100];
        int count = 0;

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
                ########    ##      ##  ##      ##    ######    ##      ## \s""";

        String line = "____________________________________________________________";
        String intro = "Hello! I'm Baron\nI am a useful chatbot!";
        String outro = "Bye. Hope you have a wonderful day!";

        // Prints intro
        System.out.println(line);
        System.out.println(banner);
        System.out.println(intro);
        System.out.println(line);

        // Stores input
        while (true) {
            String s = input.nextLine();
            System.out.println(line);
            switch (s) {
                case "bye":
                    System.out.println(outro);
                    break;
                case "list":
                    printList(memory, count);
                    break;
                default:
                    System.out.println("added: " + s);
                    memory[count++] = s;
                    break;
            }
            System.out.println(line);
            if (s.equals("bye")) {
                return;
            }
        }
    }

    static void printList(String[] memory, int count) {
        for (int i = 0; i < count; i++) {
            System.out.println((i + 1) + ". " + memory[i]);
        }
    }
}
