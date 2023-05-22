package secure;

import java.util.Base64;

public class Random {
    private static final Random instance = new Random();

    private long state;
    private long mul;
    private long inc;
    private long mod;

    public Random(long seed, long mul, long inc, long mod) {
        this.state = seed % mod;
        this.mul = mul;
        this.inc = inc;
        this.mod = mod;
    }

    public Random() {
        this(System.nanoTime(), 1103515245, 12345, 1 << 31);
    }

    public static Random instance() {
        return instance;
    }

    public int nextInt() {
        state = ((state * mul) + inc) % mod;
        return (int) state;
    }

    public byte nextByte() {
        return (byte) nextInt();
    }

    public byte[] nextBytes(int size) {
        var bytes = new byte[size];
        for (int i = 0; i < size; i++) {
            bytes[i] = nextByte();
        }
        return bytes;
    }

    public String nextBytesBase64(int size) {
        return Base64.getEncoder().encodeToString(nextBytes(size));
    }
}
