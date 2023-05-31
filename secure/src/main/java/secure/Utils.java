package secure;

import java.time.*;
import java.util.*;

public class Utils {
    /**
     * Used for exceptions that we don't want to or can't handle. This function is
     * guaranteed to never return.
     *
     * @param e
     *            The exception that caused the panic.
     * @return Return a runtime exception it is given, so that it can be thrown
     *         again to avoid control flow checks failing.
     */
    public static RuntimeException panic(Exception e) {
        e.printStackTrace();
        throw new RuntimeException(e);
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

    public static LocalDateTime toLocalDateTime(long timestamp) {
        var currentZoneOffset = ZoneOffset.systemDefault().getRules().getOffset(LocalDateTime.now());
        return LocalDateTime.ofEpochSecond(timestamp / 1000, 0, currentZoneOffset);
    }
}
