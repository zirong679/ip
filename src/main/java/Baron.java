import java.util.Scanner;

public class Baron {
    static void main(String[] args) {
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

        // Echo input
        Scanner input = new Scanner(System.in);
        String s = input.nextLine();
        while (!s.equals("bye")) {
            System.out.println(line);
            System.out.println(s);
            System.out.println(line);
            s = input.nextLine();
        }

        // Prints outro
        System.out.println(line);
        System.out.println(outro);
        System.out.println(line);
    }
}
