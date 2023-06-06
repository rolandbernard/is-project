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
    public static long parseNumber(String number) throws NumberFormatException {
        var lastComma = number.lastIndexOf(',');
        var lastDot = number.lastIndexOf('.');
        var splitComma = number.split(",");
        var splitPoint = number.split("\\.");
        if (lastComma > lastDot && splitComma.length == 2 && splitPoint.length != 2) {
            return Long.parseLong("0" + splitComma[0].replaceAll("\\.", "")) * 100
                    + Long.parseLong((splitComma[1].replaceAll("\\.", "") + "00").substring(0, 2));
        } else if (lastDot > lastComma && splitPoint.length == 2 && splitComma.length != 2) {
            return Long.parseLong("0" + splitPoint[0].replaceAll(",", "")) * 100
                    + Long.parseLong((splitPoint[1].replaceAll(",", "") + "00").substring(0, 2));
        } else if (splitComma.length == 1) {
            return Long.parseLong(String.join("", splitPoint)) * 100;
        } else if (splitPoint.length == 1) {
            return Long.parseLong(String.join("", splitComma)) * 100;
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
     * @param bytes
     *            The bytes to be encoded.
     * @return The bytes encoded with base64
     */
    public static String base64Encode(byte[] bytes) {
        return Base64.getUrlEncoder().encodeToString(bytes);
    }

    public static LocalDateTime toLocalDateTime(long timestamp) {
        var currentZoneOffset = ZoneOffset.systemDefault().getRules().getOffset(LocalDateTime.now());
        return LocalDateTime.ofEpochSecond(timestamp / 1000, 0, currentZoneOffset);
    }
}
