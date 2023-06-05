package secure;

import java.io.UnsupportedEncodingException;
import java.security.*;

public class Hash {
    private static byte[] hash(byte[]... blocks) {
        try {
            var messageDigest = MessageDigest.getInstance("SHA-512");
            for (var block : blocks) {
                messageDigest.update(block);
            }
            return messageDigest.digest();
        } catch (NoSuchAlgorithmException e) {
            throw Utils.panic(e);
        }
    }

    private static byte[] hash(int cnt, byte[]... blocks) {
        var comb = new byte[blocks.length + 1][];
        comb[0] = new byte[] {
                (byte) cnt, (byte) (cnt >>> 8), (byte) (cnt >>> 16), (byte) (cnt >>> 24)
        };
        for (int i = 0; i < blocks.length; i++) {
            comb[i + 1] = blocks[i];
        }
        return hash(blocks);
    }

    private static byte[] intToBlock(int a, int b, int c) {
        return new byte[] {
                (byte) a, (byte) (a >>> 8), (byte) (a >>> 16), (byte) (a >>> 24),
                (byte) b, (byte) (b >>> 8), (byte) (b >>> 16), (byte) (b >>> 24),
                (byte) c, (byte) (c >>> 8), (byte) (c >>> 16), (byte) (c >>> 24)
        };
    }

    private static int blockToInt(byte[] block) {
        return Byte.toUnsignedInt(block[0])
                | (Byte.toUnsignedInt(block[1]) << 8)
                | (Byte.toUnsignedInt(block[2]) << 16)
                | (Byte.toUnsignedInt(block[3]) << 24);
    }

    private static int mod(int x, int n) {
        return ((x % n) + n) % n;
    }

    /**
     * Implementation of the Balloon hashing algorithm based on the algorithm
     * presented in BONEH, Dan; CORRIGAN-GIBBS, Henry; SCHECHTER, Stuart. Balloon
     * hashing: A memory-hard function providing provable protection against
     * sequential attacks. In: Advances in Cryptology–ASIACRYPT 2016: 22nd
     * International Conference on the Theory and Application of Cryptology and
     * Information Security, Hanoi, Vietnam, December 4-8, 2016, Proceedings, Part I
     * 22. Springer Berlin Heidelberg, 2016. p. 220-248.
     *
     * @param passwd
     *            The password to be hashed.
     * @param salt
     *            The salt to hash with.
     * @param spaceCost
     *            The value regulating the amount of space required.
     * @param timeCost
     *            The value regulating the amount of time required.
     * @param length
     *            The length of the output hash.
     * @return The resulting hash.
     */
    private static byte[] balloon(byte[] passwd, byte[] salt, int spaceCost, int timeCost, int length) {
        int delta = 3;
        int cnt = 0;
        var buf = new byte[spaceCost][];
        buf[0] = hash(cnt++, passwd, salt);
        for (int m = 1; m < spaceCost; m++) {
            buf[m] = hash(cnt++, buf[m - 1]);
        }
        for (int t = 0; t < timeCost; t++) {
            for (int m = 0; m < spaceCost; m++) {
                var prev = buf[mod(m - 1, spaceCost)];
                buf[m] = hash(cnt++, prev, buf[m]);
                for (int i = 0; i < delta; i++) {
                    var idx_block = intToBlock(t, m, i);
                    int other = mod(blockToInt(hash(cnt++, salt, idx_block)), spaceCost);
                    buf[m] = hash(cnt++, buf[m], buf[other]);
                }
            }
        }
        var hash = new byte[length];
        var blockSize = buf[0].length;
        assert length <= blockSize * spaceCost;
        for (int i = 0; i < length; i++) {
            var block = buf[spaceCost - 1 - (i / blockSize)];
            hash[i] = block[i % blockSize];
        }
        return hash;
    }

    /**
     * Take the salt and password and combine it to a hash.
     *
     * @param password
     *            The password to hash.
     * @param salt
     *            The salt to hash with.
     * @param usage
     *            Unique string for the application.
     * @param length
     *            Length of the returned hash in bytes.
     * @return The hash for password and salt.
     */
    public static byte[] passwordHash(String password, byte[] salt, String usage, int length) {
        try {
            return balloon((usage + "\0" + password).getBytes("UTF-8"), salt, 32, 32, length);
        } catch (UnsupportedEncodingException e) {
            throw Utils.panic(e);
        }
    }

    /**
     * Compute the string for use in signature generation.
     *
     * @param message
     *            The password to hash.
     * @return The hash for the message.
     */
    public static byte[] signatureHash(String message) {
        try {
            var messageDigest = MessageDigest.getInstance("SHA-512");
            return messageDigest.digest(message.getBytes());
        } catch (NoSuchAlgorithmException e) {
            throw Utils.panic(e);
        }
    }

    /**
     * Generate a new key of the given length from the given key material.
     *
     * @param length
     *            The length of the resulting key.
     * @param keys
     *            The data to be used as the basis for generation.
     * @return The new key of requested length.
     */
    public static byte[] stretchKey(int length, byte[]... keys) {
        int cnt = 0;
        byte[] derived = new byte[length];
        int index = 0;
        var last = hash(cnt++, keys);
        while (index < length) {
            for (int i = 0; index + i < length && i < last.length; i++) {
                derived[index++] = last[i];
            }
            last = hash(cnt++, last);
        }
        return derived;
    }
}
