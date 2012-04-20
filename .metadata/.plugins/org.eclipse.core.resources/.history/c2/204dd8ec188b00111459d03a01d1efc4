package minirsa;

import static org.junit.Assert.*;

import org.junit.Test;

public class MiniRSATest {

    @Test
    public void testCoprime() {
        for (int i = 0; i < 1000; i++) {
            long coprime1 = MiniRSA.coprime(1234);
            assertTrue("Coprime: 1234 -> " + coprime1, 
                       MiniRSA.GCD(1234, coprime1) == 1);
            
            long coprime2 = MiniRSA.coprime(8470);
            assertTrue("Coprime: 1234 -> " + coprime2, 
                       MiniRSA.GCD(8470, coprime2) == 1);
        }
    }

    @Test
    public void testEndecrypt() {
        assertEquals(1148, MiniRSA.endecrypt(72, 451, 2623));
        assertEquals(326, MiniRSA.endecrypt(101, 451, 2623));
        assertEquals(2041, MiniRSA.endecrypt(33, 451, 2623));
    }

    @Test
    public void testGCD() {
        assertEquals(0, MiniRSA.GCD(0, 0));
        assertEquals(1, MiniRSA.GCD(0, 1));
        assertEquals(13, MiniRSA.GCD(169, 91));
        assertEquals(14, MiniRSA.GCD(2842, 12698));
        assertEquals(1, MiniRSA.GCD(451, 2520));
    }

    @Test
    public void testModInverse() {
        assertEquals(47, MiniRSA.modInverse(23, 120));
        assertEquals(1531, MiniRSA.modInverse(451, 2520));
        assertEquals(2753, MiniRSA.modInverse(17, 3120));
    }

    @Test
    public void testModulo() {
        assertEquals(1148, MiniRSA.modulo(72, 451, 2623));
        assertEquals(326, MiniRSA.modulo(101, 451, 2623));
        assertEquals(2041, MiniRSA.modulo(33, 451, 2623));
    }

    @Test
    public void testTotientFromPrimes() {
        assertEquals(2520, MiniRSA.totient(43, 61));
        assertEquals(192, MiniRSA.totient(13, 17));
    }
    
    @Test
    public void testCrackKey() {
        assertEquals(1531, MiniRSA.crackKey(451, 2623));
    }

}
