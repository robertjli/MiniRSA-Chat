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
                
                // Break into longs of length <= 5
                ArrayList<Long> longs = stringToLongs(inputString);
                
                // Encrypt each long
                ArrayList<Long> encryptedArray = new ArrayList<Long>();
                for (Long decrypted : longs) {
                    Long encrypted = Long.valueOf(MiniRSA.endecrypt(decrypted.longValue(), exponent, modulus));
                    encryptedArray.add(encrypted);
                }
                
                // convert longs to text
                String encryptedText = longsToString(encryptedArray);
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
    
    private String convertToAscii(String string) {
        StringBuilder asciiString = new StringBuilder("");
        for (int i = 0; i < string.length(); i++) {
            Integer ascii = (int) string.charAt(i);
            String s = ascii.toString();
            // if ascii value is 2-digits, append 0
            if (s.length() == 2) asciiString.append(0);
            asciiString.append(s);
        }
        return asciiString.toString();
    }
    
    private String longsToString(ArrayList<Long> longs) {
        StringBuilder textString = new StringBuilder("");
        for (Long l : longs) {
            String c = l.toString();
            textString.append(c);
        }
        return textString.toString();
    }
    
    private ArrayList<Long> stringToLongs(String ascii) {
        ArrayList<Long> longs = new ArrayList<Long>();
        for (int i = 0; i < ascii.length(); i++) {
            String piece;
            try {
                piece = ascii.substring(i, i + 5);
            } catch (Exception e) {
                piece = ascii.substring(i);
            }
            Long l = Long.valueOf(piece);
            longs.add(l);
        }
        return longs;
    }

}
