package rainbow;

import java.security.*;

/**
 * A simple way to represent the problem to be solved by the rainbow table.
 */
public class HashProblem {
    /**
     * A small utility class for generating numbers from the hash.
     */
    private static class SipNumber {
        private byte[] bytes;
        private int index;
        private int capacity;
        private int data;

        public SipNumber(byte[] bytes, int offset) {
            this.bytes = bytes.clone();
            for (int i = 0; i < 4 && i < bytes.length; i++) {
                this.bytes[i] ^= offset >> (8 * i);
            }
            this.index = 0;
            this.capacity = 1;
            this.data = 0;
        }

        /**
         * @param end
         *            The end (exclusive) of the range of numbers from which to select.
         * @return Return a number between 0 and {@code end} (exclusive).
         */
        public int getNumber(int end) {
            while (capacity < end) {
                data = data * 256 + Byte.toUnsignedInt(bytes[index]);
                capacity *= 256;
                index = (index + 1) % bytes.length;
            }
            var result = (data % end + end) % end;
            data /= end;
            capacity /= end;
            return result;
        }
    }

    /**
     * The pattern that is to be used for the search space of the password.
     */
    private byte[][] pattern;
    private String hashAlgo;

    /**
     * @param pattern
     *            The pattern used for reduction.
     * @param hashAlgo
     *            The algorithm used for hashing.
     */
    private HashProblem(byte[][] pattern, String hashAlgo) throws NoSuchAlgorithmException {
        // Make sure that the selected algorithm is supported.
        MessageDigest.getInstance(hashAlgo);
        this.pattern = pattern;
        this.hashAlgo = hashAlgo;
    }

    /**
     * Simple hash problem for alphanumeric ascii string of length at most
     * {@code length}.
     *
     * @param length
     *            Maximum length of the password
     * @return A new problem instance
     */
    public static HashProblem md5AsciiAlphaNum(int length) {
        var single = new byte[2 * 26 + 10];
        for (int i = 0; i < 10; i++) {
            single[i] = (byte) ('0' + i);
        }
        for (int i = 0; i < 26; i++) {
            single[10 + i] = (byte) ('a' + i);
        }
        for (int i = 0; i < 26; i++) {
            single[36 + i] = (byte) ('A' + i);
        }
        var pattern = new byte[length][];
        for (int i = 0; i < length; i++) {
            pattern[i] = single;
        }
        try {
            return new HashProblem(pattern, "MD5");
        } catch (NoSuchAlgorithmException ex) {
            throw Utils.panic(ex);
        }
    }

    /**
     * Hash the given password using the hash algorithm for this problem.
     *
     * @param password
     *            The password that should be hashed.
     * @return The hash of the password.
     */
    public byte[] hash(byte[] password) {
        try {
            var md = MessageDigest.getInstance(hashAlgo);
            return md.digest(password);
        } catch (NoSuchAlgorithmException ex) {
            throw Utils.panic(ex);
        }
    }

    /**
     * Given the hash and chain index, generate a new possible password. The size of
     * the search space should be smaller than that of the hash function, otherwise
     * the reduce method in here will fail.
     *
     * @param hash
     *            The hash that should be reduced.
     * @param offset
     *            The offset in the chain at which the reduction is used.
     * @return The new possible password.
     */
    public byte[] reduce(byte[] hash, int offset) {
        var state = new SipNumber(hash, offset);
        int length = pattern.length;
        for (int i = pattern.length - 1; i >= 0; i--) {
            if (state.getNumber(pattern[i].length + 1) != 0) {
                break;
            }
            length--;
        }
        var result = new byte[length];
        for (int i = 0; i < length; i++) {
            var index = state.getNumber(pattern[i].length);
            result[i] = pattern[i][index];
        }
        return result;
    }
}
