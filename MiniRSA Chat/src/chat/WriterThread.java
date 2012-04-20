package chat;

import java.net.Socket;

public class WriterThread implements Runnable {

    private Socket socket;
    private long exponent;
    private long modulus;
    
    public WriterThread(Socket socket, long exponent, long modulus) {
        this.socket = socket;
        this.exponent = exponent;
        this.modulus = modulus;
    }
    
    @Override
    public void run() {
        // TODO loop-- read a line from keyboard, write to socket

    }

}
