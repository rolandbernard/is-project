package secure;

import java.util.Arrays;

public class Des {
    public static record DesSubKeys(long[] keys) {
    }

    private static int[] PC1 = {
            7, 15, 23, 31, 39, 47, 55, 63, 6, 14, 22, 30, 38, 46, 54, 62, 5, 13, 21, 29, 37, 45, 53, 61, 4, 12, 20, 28,
            1, 9, 17, 25, 33, 41, 49, 57, 2, 10, 18, 26, 34, 42, 50, 58, 3, 11, 19, 27, 35, 43, 51, 59, 36, 44, 52, 60
    };

    private static int[] PC2 = {
            42, 39, 45, 32, 55, 51, 53, 28, 41, 50, 35, 46, 33, 37, 44, 52, 30, 48, 40, 49, 29, 36, 43, 54, 15, 4, 25,
            19, 9, 1, 26, 16, 5, 11, 23, 8, 12, 7, 17, 0, 22, 3, 10, 14, 6, 20, 27, 24
    };

    private static int[] KEY_SHIFTS = {
            1, 1, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 1
    };

    private static int[] IP = {
            6, 14, 22, 30, 38, 46, 54, 62, 4, 12, 20, 28, 36, 44, 52, 60, 2, 10, 18, 26, 34, 42, 50, 58, 0, 8, 16, 24,
            32, 40, 48, 56, 7, 15, 23, 31, 39, 47, 55, 63, 5, 13, 21, 29, 37, 45, 53, 61, 3, 11, 19, 27, 35, 43, 51, 59,
            1, 9, 17, 25, 33, 41, 49, 57
    };

    private static int[] E = {
            0, 31, 30, 29, 28, 27, 28, 27, 26, 25, 24, 23, 24, 23, 22, 21, 20, 19, 20, 19, 18, 17, 16, 15, 16, 15, 14,
            13, 12, 11, 12, 11, 10, 9, 8, 7, 8, 7, 6, 5, 4, 3, 4, 3, 2, 1, 0, 31
    };

    private static int[][] SUB = {
            { 14, 0, 4, 15, 13, 7, 1, 4, 2, 14, 15, 2, 11, 13, 8, 1, 3, 10, 10, 6, 6, 12, 12, 11, 5, 9, 9, 5, 0, 3, 7,
                    8, 4, 15, 1, 12, 14, 8, 8, 2, 13, 4, 6, 9, 2, 1, 11, 7, 15, 5, 12, 11, 9, 3, 7, 14, 3, 10, 10, 0, 5,
                    6, 0, 13 },
            { 15, 3, 1, 13, 8, 4, 14, 7, 6, 15, 11, 2, 3, 8, 4, 14, 9, 12, 7, 0, 2, 1, 13, 10, 12, 6, 0, 9, 5, 11, 10,
                    5, 0, 13, 14, 8, 7, 10, 11, 1, 10, 3, 4, 15, 13, 4, 1, 2, 5, 11, 8, 6, 12, 7, 6, 12, 9, 0, 3, 5, 2,
                    14, 15, 9 },
            { 10, 13, 0, 7, 9, 0, 14, 9, 6, 3, 3, 4, 15, 6, 5, 10, 1, 2, 13, 8, 12, 5, 7, 14, 11, 12, 4, 11, 2, 15, 8,
                    1, 13, 1, 6, 10, 4, 13, 9, 0, 8, 6, 15, 9, 3, 8, 0, 7, 11, 4, 1, 15, 2, 14, 12, 3, 5, 11, 10, 5, 14,
                    2, 7, 12 },
            { 7, 13, 13, 8, 14, 11, 3, 5, 0, 6, 6, 15, 9, 0, 10, 3, 1, 4, 2, 7, 8, 2, 5, 12, 11, 1, 12, 10, 4, 14, 15,
                    9, 10, 3, 6, 15, 9, 0, 0, 6, 12, 10, 11, 1, 7, 13, 13, 8, 15, 9, 1, 4, 3, 5, 14, 11, 5, 12, 2, 7, 8,
                    2, 4, 14 },
            { 2, 14, 12, 11, 4, 2, 1, 12, 7, 4, 10, 7, 11, 13, 6, 1, 8, 5, 5, 0, 3, 15, 15, 10, 13, 3, 0, 9, 14, 8, 9,
                    6, 4, 11, 2, 8, 1, 12, 11, 7, 10, 1, 13, 14, 7, 2, 8, 13, 15, 6, 9, 15, 12, 0, 5, 9, 6, 10, 3, 4, 0,
                    5, 14, 3 },
            { 12, 10, 1, 15, 10, 4, 15, 2, 9, 7, 2, 12, 6, 9, 8, 5, 0, 6, 13, 1, 3, 13, 4, 14, 14, 0, 7, 11, 5, 3, 11,
                    8, 9, 4, 14, 3, 15, 2, 5, 12, 2, 9, 8, 5, 12, 15, 3, 10, 7, 11, 0, 14, 4, 1, 10, 7, 1, 6, 13, 0, 11,
                    8, 6, 13 },
            { 4, 13, 11, 0, 2, 11, 14, 7, 15, 4, 0, 9, 8, 1, 13, 10, 3, 14, 12, 3, 9, 5, 7, 12, 5, 2, 10, 15, 6, 8, 1,
                    6, 1, 6, 4, 11, 11, 13, 13, 8, 12, 1, 3, 4, 7, 10, 14, 7, 10, 9, 15, 5, 6, 0, 8, 15, 0, 14, 5, 2, 9,
                    3, 2, 12 },
            { 13, 1, 2, 15, 8, 13, 4, 8, 6, 10, 15, 3, 11, 7, 1, 4, 10, 12, 9, 5, 3, 6, 14, 11, 5, 0, 0, 14, 12, 9, 7,
                    2, 7, 2, 11, 1, 4, 14, 1, 7, 9, 4, 12, 10, 14, 8, 2, 13, 0, 15, 6, 12, 10, 9, 13, 0, 15, 3, 3, 5, 5,
                    6, 8, 11 }
    };

    private static int[] P = {
            16, 25, 12, 11, 3, 20, 4, 15, 31, 17, 9, 6, 27, 14, 1, 22, 30, 24, 8, 18, 0, 5, 29, 23, 13, 19, 2, 26, 10,
            21, 28, 7
    };

    private static int[] FP = {
            24, 56, 16, 48, 8, 40, 0, 32, 25, 57, 17, 49, 9, 41, 1, 33, 26, 58, 18, 50, 10, 42, 2, 34, 27, 59, 19, 51,
            11, 43, 3, 35, 28, 60, 20, 52, 12, 44, 4, 36, 29, 61, 21, 53, 13, 45, 5, 37, 30, 62, 22, 54, 14, 46, 6, 38,
            31, 63, 23, 55, 15, 47, 7, 39
    };

    private static long rotateLeft(long value, int shift, int bits) {
        return ((value << shift) | (value >>> (bits - shift))) & ((1 << bits) - 1);
    }

    private static long bitPermutation(long value, int[] box) {
        long mixed = 0;
        for (int i : box) {
            mixed <<= 1;
            mixed |= (value >>> i) & 1;
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
            var off = (SUB.length - 1 - i);
            var subInput = (preSub >>> (6 * off)) & 0x3f;
            var subOut = SUB[i][(int) subInput];
            postSub |= subOut << (4 * off);
        }
        return bitPermutation(postSub, P);
    }

    static long initialPermutation(long message) {
        return bitPermutation(message, IP);
    }

    static long finalPermutation(long message) {
        return bitPermutation(message, FP);
    }

    public static long performRounds(long state, DesSubKeys keys, boolean inv) {
        var left = state >>> 32;
        var right = state & 0xff_ff_ff_ffL;
        for (int i = 0; i < 16; i++) {
            var tmp = left ^ feistel(right, keys.keys[inv ? 15 - i : i]);
            left = right;
            right = tmp;
        }
        return (right << 32) | left;
    }

    public static long encryptBlock(long message, DesSubKeys keys) {
        var state = bitPermutation(message, IP);
        state = performRounds(state, keys, false);
        return bitPermutation(state, FP);
    }

    public static long decryptBlock(long message, DesSubKeys keys) {
        var state = bitPermutation(message, invertPermutationBox(FP));
        state = performRounds(state, keys, true);
        return bitPermutation(state, invertPermutationBox(IP));
    }

    public static long bytesToBlock(byte[] bytes, int offset, int pad) {
        long block = 0;
        for (int i = 0; i < 8; i++) {
            block <<= 8;
            if (offset + i < bytes.length) {
                block |= Byte.toUnsignedLong(bytes[offset + i]);
            } else {
                block |= pad;
            }
        }
        return block;
    }

    public static void blockToBytes(long block, byte[] bytes, int offset) {
        for (int i = 0; i < 8; i++) {
            if (offset + 7 - i < bytes.length) {
                bytes[offset + 7 - i] = (byte) block;
            }
            block >>>= 8;
        }
    }

    public static byte[] encryptCbc(byte[] message, long key, long iv) {
        var size = (message.length + 8) & ~0b111;
        var keys = generateSubKeys(key);
        byte[] cipher = new byte[size];
        long block = iv;
        for (int i = 0; i < size; i += 8) {
            block ^= bytesToBlock(message, i, size - message.length);
            block = encryptBlock(block, keys);
            blockToBytes(block, cipher, i);
        }
        return cipher;
    }

    public static byte[] encryptCbc(byte[] message, byte[] keys) {
        long key = bytesToBlock(keys, 0, 0);
        long iv;
        if (keys.length >= 16) {
            iv = bytesToBlock(keys, 8, 0);
        } else {
            iv = 0xb8_93_87_fa_a0_0c_0e_e4L;
        }
        return encryptCbc(message, key, iv);
    }

    public static byte[] decryptCbc(byte[] message, long key, long iv) {
        assert message.length % 8 == 0;
        var keys = generateSubKeys(key);
        byte[] plain = new byte[message.length];
        long last = iv;
        for (int i = 0; i < message.length; i += 8) {
            long cipherBlock = bytesToBlock(message, i, 0);
            long plainBlock = last ^ decryptBlock(cipherBlock, keys);
            blockToBytes(plainBlock, plain, i);
            last = cipherBlock;
        }
        return Arrays.copyOf(plain, message.length - plain[plain.length - 1]);
    }

    public static byte[] decryptCbc(byte[] message, byte[] keys) {
        long key = bytesToBlock(keys, 0, 0);
        long iv;
        if (keys.length >= 16) {
            iv = bytesToBlock(keys, 8, 0);
        } else {
            iv = 0xb8_93_87_fa_a0_0c_0e_e4L;
        }
        return decryptCbc(message, key, iv);
    }
}
