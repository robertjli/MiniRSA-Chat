package chat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.Socket;
import java.util.ArrayList;

import minirsa.MiniRSA;

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
    
    /* loop-- read a line from keyboard, write to socket */
    @Override
    public void run() {
        PrintWriter out;
        try {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(System.in));
            out = new PrintWriter(socket.getOutputStream(), true);
            String input = "";
            while (!(input.equals("quit"))) {
                input = in.readLine();
                if (!(input.equals("quit"))) {
                    System.out.println("You typed: " + input);
                }
                
                // Break up, convert to long
                ArrayList<Long> decryptedArray = encryptIntoChars(input);
                
                // Encrypt each character individually
                ArrayList<Long> encryptedArray = new ArrayList<>();
                for (Long decrypted : decryptedArray) {
                    Long encrypted = Long.valueOf(MiniRSA.endecrypt(decrypted.longValue(), exponent, modulus));
                    encryptedArray.add(encrypted);
                    System.out.println("Encrypted ASCII: " + decrypted + " to " + encrypted);
                }

                // convert ASCII to text
                String encryptedText = longToString(encryptedArray);
                System.out.println("Send: " + encryptedText);
                
                // write input to socket
                out.println(encryptedText);
            }  
        } catch (Exception e) {
            System.err.println("IO error in Reader");
            e.printStackTrace();
            System.exit(-1);
        }
    }
    
    private ArrayList<Long> encryptIntoChars(String longString) {
        ArrayList<Long> pieces = new ArrayList<>();
        for (int i = 0; i < longString.length(); i ++) {
            char piece = longString.charAt(i);
            long l = piece;
            pieces.add(Long.valueOf(l));
        }
        return pieces;
    }
    
    private String longToString(ArrayList<Long> ascii) {
        StringBuilder textString = new StringBuilder("");
        for (Long l : ascii) {
            String c = l.toString();
            textString.append(c);
        }
        return textString.toString();
    }

}
