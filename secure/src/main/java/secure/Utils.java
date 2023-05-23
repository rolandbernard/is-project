package secure;

import java.io.UnsupportedEncodingException;
import java.security.*;
import java.util.*;

public class Utils {
    /**
     * Used for exceptions that we don't want to or can't handle. This function is
     * guaranteed to never return.
     *
     * @param e
     *            The exception that caused the panic.
     * @return Return a runtime exception it is given, so that it can be thrown
     *         again to
     *         avoid control flow checks failing.
     */
    public static RuntimeException panic(Exception e) {
        e.printStackTrace();
        System.exit(1);
        return new RuntimeException(e);
    }

    /**
     * Parse a decimal number that can use either '.' or ',' for the decimal
     * separator. A best effort guess is made in some ambiguous cases. Returns the
     * result as integer numerator over one hundred.
     *
     * @param number
     * @return The result of parsing.
     * @throws NumberFormatException
     *             If the number can not be parsed.
     */
    public static int parseNumber(String number) throws NumberFormatException {
        var splitComma = number.split(",");
        var splitPoint = number.split("\\.");
        if (splitComma.length == 2 && splitPoint.length != 2) {
            return Integer.parseInt("0" + String.join("", splitComma[0].replaceAll("\\.", ""))) * 100
                    + Integer.parseInt(String.join("", (splitComma[1].replaceAll("\\.", "") + "00").substring(0, 2)));
        } else if (splitPoint.length == 2 && splitComma.length != 2) {
            return Integer.parseInt("0" + String.join("", splitPoint[0].replaceAll(",", ""))) * 100
                    + Integer.parseInt(String.join("", (splitPoint[1].replaceAll(",", "") + "00").substring(0, 2)));
        } else if (splitComma.length == 1) {
            return Integer.parseInt(String.join("", splitPoint)) * 100;
        } else if (splitPoint.length == 1) {
            return Integer.parseInt(String.join("", splitComma)) * 100;
        } else {
            throw new NumberFormatException();
        }
    }

    /**
     * Generate a random UUID.
     *
     * @return A random UUID.
     */
    public static String newUuid() {
        return UUID.randomUUID().toString();
    }

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
            throw panic(e);
        }
    }

    private static byte[] hash(int cnt, byte[] block) {
        return hash(cnt, block, new byte[0]);
    }

    private static byte[] int_to_block(int a, int b, int c) {
        return new byte[] {
                (byte) a, (byte) (a >>> 8), (byte) (a >>> 16), (byte) (a >>> 24),
                (byte) b, (byte) (b >>> 8), (byte) (b >>> 16), (byte) (b >>> 24),
                (byte) c, (byte) (c >>> 8), (byte) (c >>> 16), (byte) (c >>> 24)
        };
    }

    private static int to_int(byte[] block) {
        return block[0] | (block[1] << 8) | (block[2] << 16) | (block[3] << 24);
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
                var prev = buf[(m + spaceCost - 1) % spaceCost];
                buf[m] = hash(cnt++, prev, buf[m]);
                for (int i = 0; i < delta; i++) {
                    var idx_block = int_to_block(t, m, i);
                    int other = to_int(hash(cnt++, salt, idx_block)) % spaceCost;
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
            return balloon((usage + "\0" + password).getBytes("UTF-8"), salt, 1_024, 32, length);
        } catch (UnsupportedEncodingException e) {
            throw panic(e);
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
            throw panic(e);
        }
    }

    /**
     * Validate password.
     *
     * @param password
     * @param repeatPassword
     * @return true if password is valid, false otherwise.
     */
    public static List<String> validatePassword(String password, String repeatPassword) {
        var errors = new ArrayList<String>();
        if (!password.equals(repeatPassword)) {
            errors.add("The two password must be equal.");
        }
        if (password.length() < 8) {
            errors.add("Password must be at least 8 characters long.");
        }
        if (!password.matches(".*[a-z].*")) {
            errors.add("Password must contain at least one lowercase letter.");
        }
        if (!password.matches(".*[A-Z].*")) {
            errors.add("Password must contain at least one uppercase letter.");
        }
        if (!password.matches(".*[0-9].*")) {
            errors.add("Password must contain at least one digit.");
        }
        if (!password.matches(".*\\W.*")) {
            errors.add("Password must contain at least one special character.");
        }
        return errors;
    }

    /**
     * Validate username.
     *
     * @param username
     * @return error messages if username is invalid, null otherwise.
     */
    public static List<String> validateUsername(String username) {
        var errors = new ArrayList<String>();
        if (username.length() < 3) {
            errors.add("Username must be at least 3 characters long.");
        }
        if (username.length() > 255) {
            errors.add("Username must be at most 255 characters long.");
        }
        if (!username.matches("[ a-zA-Z0-9]+")) {
            errors.add("Username must only contain alphanumeric characters.");
        }
        if (username.matches(".*\\s.*")) {
            errors.add("Username must not contain whitespace.");
        }
        return errors;
    }

    /**
     * @param bytes
     *            The bytes to be encoded.
     * @return The bytes encoded with base64
     */
    public static String base64encode(byte[] bytes) {
        return Base64.getUrlEncoder().encodeToString(bytes);
    }
}
