package secure;

public class Des {
    public static record DesSubKeys(long[] keys) {
    }

    private static int[] PC1 = {
            57, 49, 41, 33, 25, 17, 9, 1, 58, 50, 42, 34, 26, 18, 10, 2, 59, 51, 43, 35, 27, 19, 11, 3, 60, 52, 44, 36,
            63, 55, 47, 39, 31, 23, 15, 7, 62, 54, 46, 38, 30, 22, 14, 6, 61, 53, 45, 37, 29, 21, 13, 5, 28, 20, 12, 4
    };

    private static int[] PC2 = {
            14, 17, 11, 24, 1, 5, 3, 28, 15, 6, 21, 10, 23, 19, 12, 4, 26, 8, 16, 7, 27, 20, 13, 2, 41, 52, 31, 37, 47,
            55, 30, 40, 51, 45, 33, 48, 44, 49, 39, 56, 34, 53, 46, 42, 50, 36, 29, 32
    };

    private static int[] KEY_SHIFTS = {
            1, 1, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 1
    };

    static int[] IP = {
            58, 50, 42, 34, 26, 18, 10, 2, 60, 52, 44, 36, 28, 20, 12, 4, 62, 54, 46, 38, 30, 22, 14, 6, 64, 56, 48, 40,
            32, 24, 16, 8, 57, 49, 41, 33, 25, 17, 9, 1, 59, 51, 43, 35, 27, 19, 11, 3, 61, 53, 45, 37, 29, 21, 13, 5,
            63, 55, 47, 39, 31, 23, 15, 7
    };

    private static int[] E = {
            32, 1, 2, 3, 4, 5, 4, 5, 6, 7, 8, 9, 8, 9, 10, 11, 12, 13, 12, 13, 14, 15, 16, 17, 16, 17, 18, 19, 20, 21,
            20, 21, 22, 23, 24, 25, 24, 25, 26, 27, 28, 29, 28, 29, 30, 31, 32, 1
    };

    private static int[][] SUB = {
            { 14, 0, 4, 15, 13, 7, 1, 4, 2, 14, 15, 2, 11, 13, 8, 1, 3, 10, 10, 6, 6, 12, 12, 11, 5, 9, 9, 5, 0, 3, 7,
                    8, 0, 0, 15, 15, 7, 7, 4, 4, 14, 14, 2, 2, 13, 13, 1, 1, 10, 10, 6, 6, 12, 12, 11, 11, 9, 9, 5, 5,
                    3, 3, 8, 8 },
            { 15, 3, 1, 13, 8, 4, 14, 7, 6, 15, 11, 2, 3, 8, 4, 14, 9, 12, 7, 0, 2, 1, 13, 10, 12, 6, 0, 9, 5, 11, 10,
                    5, 3, 3, 13, 13, 4, 4, 7, 7, 15, 15, 2, 2, 8, 8, 14, 14, 12, 12, 0, 0, 1, 1, 10, 10, 6, 6, 9, 9, 11,
                    11, 5, 5 },
            { 10, 13, 0, 7, 9, 0, 14, 9, 6, 3, 3, 4, 15, 6, 5, 10, 1, 2, 13, 8, 12, 5, 7, 14, 11, 12, 4, 11, 2, 15, 8,
                    1, 13, 13, 7, 7, 0, 0, 9, 9, 3, 3, 4, 4, 6, 6, 10, 10, 2, 2, 8, 8, 5, 5, 14, 14, 12, 12, 11, 11, 15,
                    15, 1, 1 },
            { 7, 13, 13, 8, 14, 11, 3, 5, 0, 6, 6, 15, 9, 0, 10, 3, 1, 4, 2, 7, 8, 2, 5, 12, 11, 1, 12, 10, 4, 14, 15,
                    9, 13, 13, 8, 8, 11, 11, 5, 5, 6, 6, 15, 15, 0, 0, 3, 3, 4, 4, 7, 7, 2, 2, 12, 12, 1, 1, 10, 10, 14,
                    14, 9, 9 },
            { 2, 14, 12, 11, 4, 2, 1, 12, 7, 4, 10, 7, 11, 13, 6, 1, 8, 5, 5, 0, 3, 15, 15, 10, 13, 3, 0, 9, 14, 8, 9,
                    6, 14, 14, 11, 11, 2, 2, 12, 12, 4, 4, 7, 7, 13, 13, 1, 1, 5, 5, 0, 0, 15, 15, 10, 10, 3, 3, 9, 9,
                    8, 8, 6, 6 },
            { 12, 10, 1, 15, 10, 4, 15, 2, 9, 7, 2, 12, 6, 9, 8, 5, 0, 6, 13, 1, 3, 13, 4, 14, 14, 0, 7, 11, 5, 3, 11,
                    8, 10, 10, 15, 15, 4, 4, 2, 2, 7, 7, 12, 12, 9, 9, 5, 5, 6, 6, 1, 1, 13, 13, 14, 14, 0, 0, 11, 11,
                    3, 3, 8, 8 },
            { 4, 13, 11, 0, 2, 11, 14, 7, 15, 4, 0, 9, 8, 1, 13, 10, 3, 14, 12, 3, 9, 5, 7, 12, 5, 2, 10, 15, 6, 8, 1,
                    6, 13, 13, 0, 0, 11, 11, 7, 7, 4, 4, 9, 9, 1, 1, 10, 10, 14, 14, 3, 3, 5, 5, 12, 12, 2, 2, 15, 15,
                    8, 8, 6, 6 },
            { 13, 1, 2, 15, 8, 13, 4, 8, 6, 10, 15, 3, 11, 7, 1, 4, 10, 12, 9, 5, 3, 6, 14, 11, 5, 0, 0, 14, 12, 9, 7,
                    2, 1, 1, 15, 15, 13, 13, 8, 8, 10, 10, 3, 3, 7, 7, 4, 4, 12, 12, 5, 5, 6, 6, 11, 11, 0, 0, 14, 14,
                    9, 9, 2, 2 }
    };

    static int[] P = {
            16, 7, 20, 21, 29, 12, 28, 17, 1, 15, 23, 26, 5, 18, 31, 10, 2, 8, 24, 14, 32, 27, 3, 9, 19, 13, 30, 6, 22,
            11, 4, 25
    };

    static int[] FP = {
            40, 8, 48, 16, 56, 24, 64, 32, 39, 7, 47, 15, 55, 23, 63, 31, 38, 6, 46, 14, 54, 22, 62, 30, 37, 5, 45, 13,
            53, 21, 61, 29, 36, 4, 44, 12, 52, 20, 60, 28, 35, 3, 43, 11, 51, 19, 59, 27, 34, 2, 42, 10, 50, 18, 58, 26,
            33, 1, 41, 9, 49, 17, 57, 25
    };

    private static long rotateLeft(long value, int shift, int bits) {
        return ((value << shift) | (value >>> (bits - shift))) & ((1 << bits) - 1);
    }

    private static long bitPermutation(long value, int[] box) {
        long mixed = 0;
        for (int i : box) {
            mixed |= (value >> i) & 1;
            mixed <<= 1;
        }
        return mixed;
    }

    private static int[] invertPermutationBox(int[] box) {
        var inv = new int[box.length];
        for (int i = 0; i < box.length; i++) {
            inv[box[i]] = i;
        }
        return inv;
    }

    public static DesSubKeys generateSubKeys(long key) {
        var keys = new long[16];
        var extendedKey = bitPermutation(key, PC1);
        var left = extendedKey >>> 28;
        var right = extendedKey & 0xf_ff_ff_ff;
        for (int i = 0; i < 16; i++) {
            left = rotateLeft(left, KEY_SHIFTS[i], 28);
            right = rotateLeft(right, KEY_SHIFTS[i], 28);
            keys[i] = bitPermutation((left << 28) | right, PC2);
        }
        return new DesSubKeys(keys);
    }

    public static long feistel(long block, long subkey) {
        var preSub = bitPermutation(block, E) ^ subkey;
        var postSub = 0;
        for (int i = 0; i < SUB.length; i++) {
            var subInput = (preSub >>> 6 * i);
            var subOut = SUB[i][(int) subInput];
            postSub |= subOut << 4 * i;
        }
        return bitPermutation(postSub, P);
    }

    public static long encryptBlock(long message, DesSubKeys keys) {
        var state = bitPermutation(message, IP);
        for (int i = 0; i < 16; i++) {
            var left = state >> 32;
            var right = state & 0xff_ff_ff_ff;
            state = (right << 32) | (left ^ feistel(right, keys.keys[i]));
        }
        return bitPermutation(state, FP);
    }

    public static long decryptBlock(long message, DesSubKeys keys) {
        var state = bitPermutation(message, invertPermutationBox(FP));
        for (int i = 0; i < 16; i++) {
            var left = state >> 32;
            var right = state & 0xff_ff_ff_ff;
            state = (right << 32) | (left ^ feistel(right, keys.keys[15 - i]));
        }
        return bitPermutation(state, invertPermutationBox(IP));
    }
}
