package secure;

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
            var messageDigest = MessageDigest.getInstance("md5");
            return messageDigest.digest(password.getBytes());
        } catch (NoSuchAlgorithmException e) {
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
