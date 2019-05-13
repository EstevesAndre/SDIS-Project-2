package source;

public class App {

    // java source/App "127.0.0.1" 1234
    public static void main(String[] args) {

        if (args.length != 2) {
            System.err.println("Usage: java source/App <chord_ip_address> <chord_port>");
            System.exit(1);
        }
        else 
            new Chord(args[0], args[1]);

        System.out.println("Chord created");
    }
}