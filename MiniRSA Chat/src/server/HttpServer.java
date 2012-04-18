package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.*;

/**
 * A multithreaded HTTP Server implementation
 * 
 * @author Evan Schoenbach
 * @version February 22, 2012
 */
public class HttpServer {


    /* Thread queue, web server's root, timeout (to prevent deadlock), max worker threads */
    private static File root;
    private static int socketTimeout; // to prevent deadlock
    private static int listenPort;
    private static ServerSocket serverSocket;
    private static BlockingQueue<Socket> queue;
    private static ThreadPool<Socket> threadPool;
    private static File errorLog;
    private static PrintWriter errorWriter;

    public HttpServer(int port, File rootDir, ThreadPool<Socket> pool,
            BlockingQueue<Socket> blockingQueue, int timeout) {

        /* Initialize instance variables */
        listenPort = port;
        root = rootDir;
        threadPool = pool;
        queue = blockingQueue;
        socketTimeout = timeout;

        /* Create error log and its writer */
        errorLog = new File(root, "errorlog.txt");
        try {
            errorWriter= new PrintWriter(new FileWriter(errorLog));
        } catch (IOException e1) {
            System.err.println("Error log creation failed");
            e1.getStackTrace();
            System.exit(-1);
        }
    }

    private void startServer() {

        /* connect to socket */
        try {
            serverSocket = new ServerSocket(listenPort);
            serverSocket.setSoTimeout(socketTimeout);
            // System.out.println("Got port!");
        } catch (IOException e) {
            HttpServer.writeToLog("Could not listen on port " + listenPort + ".", e);
            System.exit(-1);
        }

        /* Read client requests */
        while (true) {
            Socket clientSocket = null;
            try {
                clientSocket = serverSocket.accept();
            } catch (SocketTimeoutException e) {
                continue;
            } catch (IOException e) {
                HttpServer.writeToLog("Accept failed: " + listenPort, e);
                continue;
            }
            try {
                queue.enqueue(clientSocket);
            } catch (InterruptedException e) {
                writeToLog("Error while enqueuing Socket " + clientSocket.toString(), e);
                continue;
            }
        }
    }

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        /* default HttpServer parameters */
        int port = 80;
        File rootDir = new File("/website");
        int timeout = 5000;
        int workers = 10;
        int maxQueued = workers * 100;

        /* Get inputs if valid, or resort to defaults */
        validateInputs(args);

        /* Retrieve and validate port */
        int newPort = getPort(args[0]);
        if (newPort != 0) {
            port = newPort;
        }

        /* Retrieve and validate root directory */
        if (args.length > 1) {
            File newRoot = getRoot(args[1]);
            if (newRoot != null) {
                rootDir = newRoot;
            }
        } 
        if (!rootDir.isDirectory()) {
            System.err.println("Default root " + rootDir.getAbsolutePath() + " not a valid directory");
            System.exit(-1);
        }

        /* create thread pool and worker threads */
        BlockingQueue<Socket> blockingQueue = new BlockingQueue<Socket>(maxQueued);
        ThreadPool<Socket> pool = new ThreadPool<Socket>(blockingQueue, workers);
        pool.start();

        /* Create HttpServer and start it */
        HttpServer newServer = new HttpServer(port, rootDir, pool, blockingQueue, timeout);
        newServer.startServer();
    }

    private static void validateInputs(String[] args) {
        if (args.length > 3) {
            System.err.println(args.length + " command-line arguments! Only 3 possible.");
            System.exit(-1);
        } else if (args.length == 0){
            System.out.println("Full name: Evan Schoenbach");
            System.out.println("SEAS login: evanscho");
            System.exit(1);
        }
    }

    /* Get input port number and authenticate it */
    private static int getPort(String portString) {
        int port = 0;
        try {
            int newPort = Integer.parseInt(portString);
            if (newPort >= 0 && newPort <= 65535) {
                port = Integer.parseInt(portString);
            } else {
                System.err.println("Port number out of range.");
                System.exit(-1);
            }
        } catch (NumberFormatException e) {
            System.err.println("Port number not in integer form");
            e.getStackTrace();
            System.exit(-1);
        }
        return port;
    }

    private static File getRoot(String rootString) {
        /* Get input root */
        File newRoot = new File(rootString);
        return newRoot;
    }

    private static File getWebxml(String webxmlString) {
        /* Get input web.xml location */
        File newWebxmlLocation = new File(webxmlString);
        if (newWebxmlLocation.isFile() && newWebxmlLocation.toString().contains(".xml")) {
            return newWebxmlLocation;
        }
        System.err.println("input web.xml location not a valid .xml file");
        System.exit(-1);
        return null;
    }

    /* Synchronized so only one thread can access at a time */
    public synchronized static void openControl(Socket clientSocket, String inputLine) {
        PrintWriter out;
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            writeToLog("Error creating PrintWriter for control panel", e);
            //            System.err.println("Error creating PrintWriter for control panel");
            return;
        }

        /* Create header */
        String httpTag = inputLine.substring(inputLine.lastIndexOf(" "));
        out.println(httpTag + " " + "200 OK");
        out.println("Date: " + (new Date()));
        out.println("Content-Type: text/html");
        //		out.println("Content-Length: " + target.length());
        out.println("Connection: close");
        out.println("\r\n");

        /* Create panel */
        out.println("<html><title>Control Panel</title><p>\n");
        out.println("");
        out.println("<body><h6>Full name: Evan Schoenbach<br>");
        out.println("SEAS login: evanscho</h6>");

        out.println("<h4>Worker&#160&#160&#160&#160&#160&#160&#160&#160&#160&#160&#160" +
                "&#160&#160&#160&#160&#160&#160&#160&#160&#160" + "Status</h4>");
        out.println("<p>");
        ArrayList<WorkerThread> workerThreads = threadPool.getNonTerminatedThreads();
        String workerID = null, filePath = null;
        for (int i = 0; workerThreads != null && i < workerThreads.size(); i++) {
            workerID = workerThreads.get(i).getName();
            if ((filePath = workerThreads.get(i).getFilePath()) == null)
                filePath = "Waiting...";
            out.println(workerID + "&#160&#160&#160&#160&#160&#160&#160&#160&#160&#160&#160" +
                    filePath + "<br>");
        }
        out.println("</p>");
        out.println("<a href=\"shutdown\">"+"Shutdown all threads"+"</a><br>");
        out.println("<a href=\"errorlog\">"+"View error log"+"</a><br>");
        out.println("<p><hr><br><i>" + (new Date()) + "</i></body></html>");
        out.close();
    }

    public synchronized static void shutdownAll(Socket clientSocket, String inputLine) {
        PrintWriter out;
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            writeToLog(e.getMessage(), e);
            System.exit(-1);
            return;
        }

        /* Create header */
        String httpTag = inputLine.substring(inputLine.lastIndexOf(" "));
        out.println(httpTag + " " + "200 OK");
        out.println("Date: " + (new Date()));
        out.println("Content-Type: text/html");
        //		out.println("Content-Length: " + target.length());
        out.println("Connection: close");
        out.println("\r\n");

        /* Create shutdown title */
        out.println("<html><title>Server shutdown</title><p>\n");
        out.println("");
        out.println("<body><h2>Server Shutdown</h2><br><html></body>");

        /* Terminate threads, close sockets, exit server */
        threadPool.terminate();
        try {
            //			clientSocket.close();
            serverSocket.close();
        } catch (IOException e) {
            HttpServer.writeToLog(e.getMessage(), e);
        }
        System.exit(1);
    }

    public synchronized static void displayErrorLog(Socket clientSocket, String inputLine) {
        PrintWriter out;
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            HttpServer.writeToLog(e.getMessage(), e);
            System.exit(-1);
            return;
        }

        /* Create header */
        String httpTag = inputLine.substring(inputLine.lastIndexOf(" "));
        out.println(httpTag + " " + "200 OK");
        out.println("Date: " + (new Date()));
        out.println("Content-Type: text/html");
        //              out.println("Content-Length: " + target.length());
        out.println("Connection: close");
        out.println("\r\n");

        /* Create error log */
        out.println("<html><title>Error Log</title><p>\n");
        out.println("");
        out.print("<body><p>");

        /* Read error log */

        try {
            BufferedReader input =  new BufferedReader(new FileReader(errorLog));
            try {
                String line = null;
                while ((line = input.readLine()) != null) {
                    out.println(line + "<br>");
                }
            }
            finally {
                input.close();
            }
        }
        catch (IOException ex){
            HttpServer.writeToLog(ex.getMessage(), ex);
        }

        out.println("</p>");
        out.println("<p><hr><br><i>" + (new Date()) + "</i></body></html>");
        out.close();
    }

    public synchronized static PrintWriter getErrorWriter() {
        return errorWriter;
    }

    public synchronized static void writeToLog(String line) {
        errorWriter.println("Error at: " + new Date());
        errorWriter.println(line);
        errorWriter.println();
        errorWriter.flush();
    }

    public synchronized static void writeToLog(String line, Throwable e) {
        errorWriter.println("Error at: " + new Date());
        if (line != null) errorWriter.println(line);
        e.printStackTrace(errorWriter);
        errorWriter.println();
        errorWriter.flush();
    }

    public static File getRoot() {
        return root;
    }

    public static int getSocketTimeout() {
        return socketTimeout;
    }
}
