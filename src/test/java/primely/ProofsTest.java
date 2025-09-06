package primely;

import com.rlnkoo.primely.Primes;
import org.junit.jupiter.api.Test;
import java.math.BigInteger;
import static org.junit.jupiter.api.Assertions.*;

class ProofsTest {

    @Test void pocklingtonOnSafePrime() {
        // small safe prime to keep test fast
        BigInteger q = new BigInteger("383");          // prime
        BigInteger p = q.shiftLeft(1).add(BigInteger.ONE); // 767
        assertTrue(Primes.isPrimeBPSW(p));
        assertTrue(Primes.provePocklington(p).isPresent(), "Pocklington should succeed for safe prime");
    }

    @Test void prattOnSmallPrime() {
        BigInteger p = new BigInteger("101"); // n-1=100 is easy to factor
        assertTrue(Primes.provePratt(p).isPresent(), "Pratt should succeed with fully factored n-1");
    }
}