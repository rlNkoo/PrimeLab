package primely;

import com.rlnkoo.primely.Sieve;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SieveTest {

    @Test void countPrimesBelow1000Parallel() {
        long cnt = Sieve.primesBetween(1, 1_000, 1 << 16, true, 4).count();
        assertEquals(168, cnt);
    }

    @Test void smallRangeSequential() {
        long[] expected = {2,3,5,7,11,13,17,19};
        long[] got = Sieve.primesBetween(1, 20).toArray();
        assertArrayEquals(expected, got);
    }
}