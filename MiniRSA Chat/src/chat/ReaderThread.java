package chat;

import java.net.Socket;

public class ReaderThread implements Runnable {

    private Socket socket;
    private long exponent;
    private long modulus;
    
    public ReaderThread(Socket socket, long exponent, long modulus) {
        this.socket = socket;
        this.exponent = exponent;
        this.modulus = modulus;
    }
    
    @Override
    public void run() {
        // TODO loop-- read chat from socket, write to screen

    }

}
