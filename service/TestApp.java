package service;

import java.security.InvalidParameterException;

import handlers.RequestManager;

public class TestApp {

    // Operation to be executed (Backup, Restore, Delete)
    private String operation;
    private String address;
    private String port;
    private String op1;
    private String op2;

    public TestApp(String args[]) throws Exception {
        this.address = args[0];
        this.port = args[1];
        this.operation = args[2];
        this.op1 = args[3];
        this.op2 = (args.length == 5) ? args[4] : null;
        if (this.operation.equals("BACKUP") && args.length == 4)
            throw new InvalidParameterException("Backup operation must have 5 arguments");

    }

    // Usage: java -Djavax.net.ssl.trustStore=truststore
    // -Djavax.net.ssl.trustStorePassword=123456 service/TestApp <node_address>
    // <node_port> <sub_protocol> <opnd_1> <opnd_2>?

    // Example: java service/TestApp BACKUP file_test1.txt 2
    // Example: java service/TestApp DELETE file_test1.txt
    // Example: java service/TestApp RESTORE file_test1.txt
    // Example: java service/TestApp RECLAIM 0
    public static void main(String args[]) throws Exception {

        if (args.length != 4 && args.length != 5) {
            System.err.println(
                    "Wrong number of arguments. Usage: <node_address> <node_port> <sub_protocol> <opnd_1> <opnd_2>?");
            System.exit(-1);
        }

        TestApp testApp = new TestApp(args);

        testApp.invokeRequest();
    }

    private void invokeRequest() throws Exception {

        switch (this.operation) {
        case "BACKUP":
            RequestManager.backupRequest(address, port, op1, op2);
            break;
        case "BACKUPENH":
            RequestManager.backupNhRequest(address, port, op1, op2);
            break;
        case "RESTORE":
            RequestManager.restoreRequest(address, port, op1);
            break;
        case "DELETE":
            RequestManager.deleteRequest(address, port, op1);
            break;
        case "DELETEENH":
            RequestManager.deleteNhRequest(address, port, op1);
            break;
        case "RECLAIM":
            RequestManager.reclaimRequest(address, port, op1);
            break;
        case "STATE":
            RequestManager.stateRequest(address, port, op1);
            break;
        default:
            System.err.println("Wrong operation to make the request.\n" + "Make sure you use of the following requests:"
                    + "\n- BACKUP" + "\n- RESTORE" + "\n- DELETE" + "\n- RECLAIM" + "\n- STATE" + "\r\n");
            return;
        }

    }
}