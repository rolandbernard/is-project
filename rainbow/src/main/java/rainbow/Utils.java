package rainbow;

public abstract class Utils {
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
     * @param value
     *            The value that should be rotated:
     * @param shift
     *            The number of bits by which to rotate;
     * @param bits
     *            The number of bits at which to wrap.
     * @return The rotated number.
     */
    public static long rotateLeft(long value, int shift, int bits) {
        shift %= bits;
        return ((value << shift) | (value >>> (bits - shift))) & ((1 << bits) - 1);
    }

    /**
     * @param arg
     *            The argument to test.
     * @return true if {@code arg} is an integer, false otherwise
     */
    public static boolean isUnsignedInteger(String arg) {
        try {
            Integer.parseUnsignedInt(arg);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }
}
