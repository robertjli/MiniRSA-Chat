package chat;

import java.net.Socket;

/**
 * A Runnable class that reads and decrypts RSA-encrypted data from a socket.
 * 
 * @author Robert Li, Evan Schoenbach
 * @version April 20, 2012
 */
public class ReaderThread implements Runnable {

    private Socket socket;
    private long exponent;
    private long modulus;
    
    /**
     * Creates a ReaderThread.
     * 
     * @param socket the socket to listen on.
     * @param exponent the exponent of the private RSA key.
     * @param modulus the modulus of the private RSA key.
     */
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
