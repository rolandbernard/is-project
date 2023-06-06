package secure;

public class Random extends java.util.Random {
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

    @Override
    public long nextLong() {
        state = ((state * mul) + inc) % mod;
        return state;
    }

    public String nextBytesBase64(int size) {
        return Utils.base64Encode(nextBytes(size));
    }

    public byte[] nextBytes(int size) {
        var bytes = new byte[size];
        nextBytes(bytes);
        return bytes;
    }
}
