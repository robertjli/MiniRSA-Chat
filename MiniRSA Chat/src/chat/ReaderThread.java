package chat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

import minirsa.MiniRSA;

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
        BufferedReader in;
        try {
            in = new BufferedReader(
                    new InputStreamReader(
                            socket.getInputStream()));
            String line;
            while (true) {
                StringBuilder input = new StringBuilder("");
                while ((line = in.readLine()) != null) {
                    System.err.println("Reading line " + line); //debug
                    input.append(line);
                }
                String inputString = input.toString();
                System.out.println("Received encrypted message: " + inputString);

                // Convert to long
                long encrypted = Long.parseLong(inputString);

                // decrypt input
                long decrypted = MiniRSA.endecrypt(encrypted, exponent, modulus);
                System.out.println("Decrypted ASCII: " + Long.valueOf(decrypted).toString());
                
                // convert ASCII to text
                String text = asciiToString(decrypted);
                System.out.println("Rec: " + text);
            }  
        } catch (Exception e) {
            System.err.println("IO error in Reader");
            e.printStackTrace();
            System.exit(-1);
        }
    }
    
    private String asciiToString(long ascii) {
        Long asciiLong = Long.valueOf(ascii);
        return asciiLong.toString();
    }

}
