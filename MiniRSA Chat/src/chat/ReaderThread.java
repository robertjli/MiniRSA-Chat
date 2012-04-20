package chat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigInteger;
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
        System.out.println("Reader decrypting with " + exponent + " " + modulus);
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
//                StringBuilder input = new StringBuilder("");
//                while ((line = in.readLine()) != null) {
            line = in.readLine();    
                    System.err.println("Reading line " + line); //debug
//                    input.append(line);
//                }
//                String inputString = input.toString();
                String inputString = line;
                System.out.println("Received encrypted message: " + inputString);

                // Convert to long
                BigInteger encrypted = new BigInteger(inputString);

                // decrypt input
                BigInteger decrypted = MiniRSA.endecrypt(encrypted, exponent, modulus);
                System.out.println("Decrypted ASCII: " + decrypted.toString());
                
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
    
    private String asciiToString(BigInteger ascii) {
        return ascii.toString();
    }

}
