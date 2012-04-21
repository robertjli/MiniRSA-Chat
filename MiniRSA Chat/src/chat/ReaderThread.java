package chat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.Socket;
import java.util.ArrayList;

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
                line = in.readLine();    
                System.err.println("Reading line " + line); //debug
                String inputString = line;
                System.out.println("Received encrypted message: " + inputString);

                // Break up, convert to long
                ArrayList<Long> encryptedArray = breakIntoChars(inputString);
                
                // Decrypt each character individually
                ArrayList<Long> decryptedArray = new ArrayList<>();
                for (Long encrypted : encryptedArray) {
                    Long decrypted = Long.valueOf(MiniRSA.endecrypt(encrypted.longValue(), exponent, modulus));
                    decryptedArray.add(decrypted);
                    System.out.println("Decrypted ASCII: " + encrypted + " to " + decrypted);
                }
                
                // convert ASCII to text
                String text = longAsciiToString(decryptedArray);
                System.out.println("Rec: " + text);
            }  
        } catch (Exception e) {
            System.err.println("IO error in Reader");
            e.printStackTrace();
            System.exit(-1);
        }
    }
    
    private ArrayList<Long> breakIntoChars(String longString) {
        ArrayList<Long> pieces = new ArrayList<>();
        for (int i = 0; i < longString.length() / 3; i += 3) {  //TODO should not be incrementing by 3, since encrypted values are not same as ascii values
            String piece = longString.substring(i, i + 3);
            Long l = Long.getLong(piece);
            pieces.add(l);
        }
        return pieces;
    }
    
    private String longAsciiToString(ArrayList<Long> ascii) {
        StringBuilder textString = new StringBuilder("");
        for (Long l : ascii) {
            char c = (char) l.longValue();
            System.err.println("Retrieving value " + l + ", char " + c);
            textString.append(c);
        }
        return textString.toString();
    }

}
