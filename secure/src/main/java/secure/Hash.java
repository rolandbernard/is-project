package secure;

import java.io.UnsupportedEncodingException;
import java.security.*;

public class Hash {
    private static byte[] hash(int cnt, byte[] block1, byte[] block2) {
        try {
            var messageDigest = MessageDigest.getInstance("SHA-512");
            messageDigest.digest(new byte[] {
                    (byte) cnt, (byte) (cnt >>> 8), (byte) (cnt >>> 16), (byte) (cnt >>> 24)
            });
            messageDigest.digest(block1);
            messageDigest.digest(block2);
            return messageDigest.digest();
        } catch (NoSuchAlgorithmException e) {
            throw Utils.panic(e);
        }
    }

    private static byte[] hash(int cnt, byte[] block) {
        return hash(cnt, block, new byte[0]);
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

    public static byte[] balloon(byte[] passwd, byte[] salt, int spaceCost, int timeCost, int length) {
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
        var block_size = buf[0].length;
        for (int i = 0; i < length; i++) {
            var block = buf[spaceCost - 1 - (i / block_size)];
            hash[i] = block[i % block_size];
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
    public static byte[] keyDerivation(String password, byte[] salt, String usage, int length) {
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
}
