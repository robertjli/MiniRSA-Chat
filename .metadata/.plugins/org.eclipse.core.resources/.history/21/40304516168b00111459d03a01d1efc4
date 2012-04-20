package chat;

import java.net.Socket;

/**
 * A Runnable class that RSA-encrypts and writes data to a socket.
 * 
 * @author Robert Li, Evan Schoenbach
 * @version April 20, 2012
 */
public class WriterThread implements Runnable {

    private Socket socket;
    private long exponent;
    private long modulus;
    
    /**
     * Creates a WriterThread.
     * 
     * @param socket the socket to write to.
     * @param exponent the exponent of the public RSA key.
     * @param modulus the modulus of the public RSA key.
     */
    public WriterThread(Socket socket, long exponent, long modulus) {
        this.socket = socket;
        this.exponent = exponent;
        this.modulus = modulus;
        System.out.println("Writer encrypting with " + exponent + " " + modulus);
    }
    
    @Override
    public void run() {
        // TODO loop-- read a line from keyboard, write to socket

    }

}
