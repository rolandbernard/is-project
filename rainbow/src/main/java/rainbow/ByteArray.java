package rainbow;

import java.util.Arrays;

/**
 * A hashable wrapper around byte[].
 */
public class ByteArray {
    public final byte[] bytes;

    public ByteArray(byte[] bytes) {
        this.bytes = bytes;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(bytes);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ByteArray ba) {
            return Arrays.equals(bytes, ba.bytes);
        } else {
            return false;
        }
    }
}
