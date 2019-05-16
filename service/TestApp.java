package service;

import java.util.ArrayList;

public class TestApp {

    // Operation to be executed (Backup, Restore, Delete)
    private String operation;
    private ArrayList<String> operationArgs;

    public TestApp(String args[]) throws Exception {

        this.operation = args[1];
        this.operationArgs = new ArrayList<>();

        for (int i = 2; i < args.length; i++) {
            this.operationArgs.add(args[i]);
        }
    }

    // Usage: java project/service/TestApp <sub_protocol> <opnd_1> <opnd_2>?
    // Example: java project/service/TestApp BACKUP ./file_test1.txt 2
    // Example: java project/service/TestApp DELETE ./file_test1.txt
    // Example: java project/service/TestApp RESTORE file_test1.txt
    // Example: java project/service/TestApp RECLAIM 0
    public static void main(String args[]) throws Exception {

        if (args.length != 2 && args.length != 3) {
            System.err.println(
                    "Wrong number of arguments.\nUsage: java project/service/TestApp <sub_protocol> <opnd_1> <opnd_2>?\n");
            System.exit(-1);
        }

        TestApp testApp = new TestApp(args);

        // testApp.invokeRequest();
    }

    private void invokeRequest() throws Exception {
        try {
            switch (this.operation) {
            case "BACKUP":

                break;
            case "BACKUPENH":

                break;
            case "RESTORE":

                break;
            case "DELETE":

                break;
            case "DELETEENH":

                break;
            case "RECLAIM":

                break;
            case "STATE":

                break;
            default:
                System.err.println(
                        "Wrong operation to make the request.\n" + "Make sure you use of the following requests:"
                                + "\n- BACKUP" + "\n- RESTORE" + "\n- DELETE" + "\n- RECLAIM" + "\n- STATE" + "\r\n");
                return;
            }
        } catch (Exception e) {
            System.err.println("ERROR --> " + this.getClass() + ": File not found!");
            System.exit(-2);
        }
    }
}