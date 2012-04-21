package minirsa;

import java.util.Random;

/**
 * This class provides static library functions for the RSA algorithm.
 * 
 * @author Robert Li, Evan Schoenbach
 * @version April 17, 2012
 */
public class MiniRSA {

    /**
     * Picks a random integer that is coprime to x, greater than 1, and less than x.
     * 
     * @param x The integer for which a coprime integer is selected.
     * @return An integer that is coprime to x, greater than 1, and less than x.
     */
    public static long coprime(long x) {
        Random rand = new Random();
        long result = rand.nextLong() % x;
        while (result <= 1 || GCD(x, result) != 1) {
            result = rand.nextLong() % x;
        }
        return result;
    }
    
    /**
     * Encrypts the integer msg using the RSA key (key, c).
     * 
     * @param msg The message to be encrypted.
     * @param key The exponent in the RSA key.
     * @param c The modulus in the RSA key.
     * @return The encrypted message.
     */
    public static long endecrypt(int msg, long key, long c) {
        return modulo(msg, key, c);
    }
    
    /**
     * Calculates the greatest common divisor of a and b.
     * 
     * @param a
     * @param b
     * @return The greatest common divisor of a and b, or -1 if a and b are 
     *         both 0.
     */
    public static long GCD(long a, long b) {
        // Implemented using the Euclidean Algorithm. (Ugh CIS 160...)
        if (a == 0 && b == 0) {
            return 0;
        }
        long x = Math.abs(a);
        long y = Math.abs(b);
        long r;
        while (y > 0) {
            r = x % y;
            x = y;
            y = r;
        }
        return x;
    }
    
    /**
     * Calculates the modular inverse <code>base<super>-1</super> % m</code>.
     * 
     * @param base
     * @param m
     * @return The modular inverse of base (mod m).
     */
    public static long modInverse(long base, long m) {
        long x = 0, y = 1, lastX = 1, lastY = 0, mod = m;
        long quotient, temp;
        while (mod != 0) {
            quotient = base / mod;
            
            temp = base;
            base = mod;
            mod = temp % mod;
            
            temp = x;
            x = lastX - quotient * x;
            lastX = temp;
            
            temp = y;
            y = lastY - quotient * y;
            lastY = temp;
        }
        if (lastX < 0) {
            lastX += m;
        }
        return lastX;
    }
    
    /**
     * Computes a<super>b</super> % c for large values of a, b, and c.
     * 
     * @param a
     * @param b
     * @param c
     * @return a<super>b</super> % c.
     */
    public static long modulo(int a, long b, long c) {
        // TODO is there a better algo?
        long result = 1;
        for (long i = 0; i < b; i++) {
            result = (result * a) % c;
        }
        return result;
    }
    
    /**
     * Computes Euler's totient of the product of primes p and q.
     * 
     * @param p A prime number.
     * @param q A prime number.
     * @return Euler's totient of n = p * q.
     */
    public static long totient(long p, long q) {
        return (p - 1) * (q - 1);
    }
    
    /**
     * Generates an RSA public/private key pair using primes p and q.
     * 
     * @param p A prime number.
     * @param q A prime number.
     * @return A 3-element array <code>a</code>, where <code>a[0]</code> is the 
     * public exponent, <code>a[1]</code> is the private exponent, and 
     * <code>a[2]</code> is the modulus.
     */
    public static long[] generateKey(long p, long q) {
        long n = p * q;
        long m = totient(p, q);
        long e = coprime(m);
        long d = modInverse(e, m);
        return new long[] {e, d, n};
    }
    
    /**
     * Given a public key (e, n), returns the private exponent d. Beware, this 
     * can take a really <i>really</i> long time...
     * 
     * @param e The public exponent.
     * @param n The modulus.
     * @return The private exponent.
     */
    public static long crackKey(long e, long n) {
        long q = -1;
        long p;
        for (p = 2; p < n / 2; p++) {
            // find factors of n
            q = n / p;
            if (p * q == n) {
                break;
            }
        }
        if (q == -1) {
            return -1;
        }
        long m = totient(p, q);
        return modInverse(e, m);
    }
}
