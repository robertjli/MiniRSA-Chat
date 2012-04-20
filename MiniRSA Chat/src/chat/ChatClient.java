package chat;

public class ChatClient {

    private static boolean server;
    
    public ChatClient(int port) {
        server = true;
        // TODO
        System.out.println("Server started.");
    }
    
    public ChatClient(String host, int port) {
        server = false;
        // TODO
        System.out.println("Client started.");
    }
    
    private void startServer() {
        // TODO
        System.out.println("This is a server.");
    }
    
    private void startClient() {
        // TODO
        System.out.println("This is a client.");
    }
    
    public static void main(String args[]) {
        if (args.length != 2) {
            printUsage();
        }
        
        int port = 8484;
        String host = "localhost";

        if (args[0].equals("-s")) { // server
            try {
                port = Integer.parseInt(args[1]);
            } catch (Exception e) {
                printUsage();
            }
            ChatClient client = new ChatClient(port);
            client.startServer();
        } else if (args[0].equals("-c")) { // client
            String[] parts = args[1].split(":");
            if (parts.length != 2) {
                printUsage();
            }
            try {
                port = Integer.parseInt(parts[1]);
            } catch (Exception e) {
                printUsage();
            }
            host = parts[0];
            ChatClient client = new ChatClient(host, port);
            client.startClient();
        } else {
            printUsage();
        }
    }
    
    public static void printUsage() {
        System.err.println("USAGE");
        System.err.println("Server: java ChatClient -s <port number>");
        System.err.println("Client: java ChatClient -c <host>:<port number>");
        System.exit(-1);
    }
}
