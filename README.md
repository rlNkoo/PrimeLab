# PrimeLab — Prime Number Toolkit for Java

PrimeLab is a project focused on prime number computations and number theory utilities.  
It provides implementations of primality tests, factorization methods, sieve algorithms, and prime generators.

---

## Overview

PrimeLab contains the following main modules:

### Primes
General prime utilities and primality testing:
- **isPrimeDet64(long n)** — deterministic Miller–Rabin test for numbers up to 2⁶⁴  
- **isPrimeBPSW(BigInteger n)** — Baillie–PSW test for arbitrary large numbers  
- **nextPrime(BigInteger n)** — returns the next probable prime greater or equal to *n*  
- **randomPrime(int bits)** — generates a random probable prime with given bit length  
- **randomSafePrime(int bits)** — generates a safe prime (*p* where (*p−1)/2 is also prime*)  
- **provePratt(BigInteger n)** — tries to construct a Pratt primality certificate  
- **provePocklington(BigInteger n)** — tries to construct a Pocklington primality certificate  

---

### Sieve
Efficient generation of primes in ranges:
- **primesBetween(from, to)** — returns a stream of primes in the given range  
- **primesBetween(from, to, segmentSize, parallel, parallelism)** — segmented sieve with configuration:  
  - *segmentSize* — how many numbers per block (affects performance/memory)  
  - *parallel* — whether to use multiple threads  
  - *parallelism* — number of threads (ForkJoinPool parallelism level)  

---

### Factorizer
Best-effort integer factorization:
- Combines **trial division**, **Pollard Rho (Brent)**, **Pollard p−1 (phase I)**, and a minimal **ECM phase I** sketch  
- Returns a **Factorization** object containing:
  - map of prime → exponent  
  - completeness flag  
  - method tag  

---

### Proofs
Primality certificate generation:
- **Pratt** certificate: uses full factorization of *n−1* and checks group order conditions  
- **Pocklington** certificate: succeeds if product of known factors of *n−1* is larger than √n and a suitable base exists  

Each proof can be pretty-printed to show the certificate steps.

---

### ModMath
Helper functions for modular arithmetic:
- **modPow(a, e, m)** — modular exponentiation  
- **modInverse(a, m)** — modular inverse  
- **gcd(a, b)** — greatest common divisor  
- **lcm(a, b)** — least common multiple  
- **crt(residues, moduli)** — Chinese Remainder Theorem solver  

---

## How to Use

1. Add the project to your Java environment (for example via Maven build).  
2. Import the classes you need, e.g. `io.primely.Primes`, `io.primely.Sieve`, etc.  
3. Call the static methods for prime testing, generation, factorization, or sieve-based enumeration.  
4. For advanced checks, use `Primes.provePratt` or `Primes.provePocklington` to obtain certificates.  
5. For number theory utilities like modular exponentiation or CRT, use `ModMath`.

---

## Testing

The project contains a comprehensive suite of JUnit 5 tests, covering:
- deterministic primality checks for 64-bit integers  
- Baillie–PSW tests on large primes  
- sieve prime counts in given ranges  
- factorization of known composites  
- primality certificates for selected safe primes  
- random prime and safe prime generation  

Run tests with:

```bash
mvn test
```

## Roadmap
- Full ECM (Elliptic Curve Method) implementation with stage II
- Recursive Pratt proof trees with complete detail
- Performance optimizations (SIMD / Panama Vector API)
- Packaging and release to Maven Central
