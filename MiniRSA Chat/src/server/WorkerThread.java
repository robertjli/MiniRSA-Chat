package server;

import java.io.File;
import java.net.Socket;

/**
 * A worker thread that runs a server implementation.
 * 
 * @author Evan Schoenbach (with inspiration from tutorials.jenkov.com/java-concurrency and Oracle Java tutorials)
 * @version February 22, 2012
 */
public class WorkerThread implements Runnable {

    private BlockingQueue<Socket> queue = null;
    private boolean isTerminated = false;
    private String workerName = null;
    
    private RequestProcessor requestProcessor;

    public WorkerThread(BlockingQueue<Socket> socketQueue) {
        queue = socketQueue;
        requestProcessor = new RequestProcessor();
    }

    /* 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        // System.out.println("Started worker thread");
        if (isTerminated) return;

        while(true) {
            Socket clientSocket = null;
            try{
                // System.out.println("Trying to dequeue...");
                clientSocket = queue.dequeue();
                if (isTerminated) return;
            } catch(Exception e){
                HttpServer.writeToLog(e.getMessage(), e);
                if (isTerminated) return;
            }
            if (clientSocket != null) {
                // System.out.println("Socket = " + clientSocket.getPort() + " Time = " + System.currentTimeMillis());
                notifyQueue();
                File root = HttpServer.getRoot();
                // System.out.println("Retrieved root " + root);

                requestProcessor.processRequest(clientSocket, root);
            }
        }
    }
    
    public synchronized void notifyQueue() {
        notify();
    }

    public synchronized void terminate() {
        isTerminated = true;
    }

    public synchronized boolean isTerminated() {
        return isTerminated;
    }

    public void setName(String name) {
        workerName = name;
    }

    public String getName() {
        return workerName;
    }
    
    public String getFilePath() {
        return requestProcessor.getFilePath();
    }
}