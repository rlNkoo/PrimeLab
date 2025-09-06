package primely;

import com.rlnkoo.primely.Primes;
import org.junit.jupiter.api.Test;
import java.math.BigInteger;
import static org.junit.jupiter.api.Assertions.*;

class BpswTest {

    @Test void mersenne127() {
        BigInteger p = new BigInteger("170141183460469231731687303715884105727");
        assertTrue(Primes.isPrimeBPSW(p));
    }

    @Test void squaresAndEven() {
        assertFalse(Primes.isPrimeBPSW(BigInteger.valueOf(1)));
        assertFalse(Primes.isPrimeBPSW(BigInteger.valueOf(4)));
        assertFalse(Primes.isPrimeBPSW(BigInteger.valueOf(100)));
    }

    @Test void nextPrimeWorks() {
        assertEquals(BigInteger.valueOf(2), Primes.nextPrime(BigInteger.ONE));
        assertEquals(new BigInteger("101"), Primes.nextPrime(new BigInteger("100")));
    }
}