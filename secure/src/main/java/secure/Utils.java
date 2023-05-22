package secure;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

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

    /**
     * Take the salt and password and combine it to a hash.
     *
     * @param password
     *            The password to hash.
     * @param salt
     *            The salt to hash with.
     * @return The hash for password and salt.
     */
    public static String hash(String password, String salt) {
        try {
            var messageDigest = MessageDigest.getInstance("SHA-512");
            var hash = messageDigest.digest((password + salt).getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw panic(e);
        }
    }
}
