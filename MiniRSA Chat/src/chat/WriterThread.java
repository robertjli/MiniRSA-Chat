package chat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

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
            String input;
            while (!(input.equals("quit"))) {
                input = in.readLine();
                if (!(input.equals("quit"))) {
                    System.out.println("You typed: " + input);
                }
                
                // Convert to long
                long unencrypted = stringToASCII(input);
                System.out.println("Unencrypted ASCII: " +
                        Long.valueOf(unencrypted).toString());
                
                // encrypt input
                long encrypted = MiniRSA.endecrypt(unencrypted, exponent, modulus);
                System.out.println("Encrypted message: " + Long.valueOf(encrypted).toString());

            }  
        } catch (Exception e) {
            System.err.println("IO error in Reader");
            e.printStackTrace();
            System.exit(-1);
        }
    }
    
    private long stringToASCII(String inputString) {
        StringBuilder longBuild = new StringBuilder("");
        for (int i = 0; i < inputString.length(); i++) {
            char c = inputString.charAt(i);
            int ascii = c;
            longBuild.append(ascii);
            }
        String longString = longBuild.toString();
        long l = Long.parseLong(longString);
        return l;
    }

}
