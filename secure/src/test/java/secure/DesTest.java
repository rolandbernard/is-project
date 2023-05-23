package secure;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class DesTest {
    @Test
    void testSubKeyGeneration() {
        var key = 0b0001001100110100010101110111100110011011101111001101111111110001L;
        var keys = Des.generateSubKeys(key);
        assertEquals(0b000110110000001011101111111111000111000001110010L, keys.keys()[0]);
        assertEquals(0b011110011010111011011001110110111100100111100101L, keys.keys()[1]);
        assertEquals(0b010101011111110010001010010000101100111110011001L, keys.keys()[2]);
        assertEquals(0b011100101010110111010110110110110011010100011101L, keys.keys()[3]);
        assertEquals(0b011111001110110000000111111010110101001110101000L, keys.keys()[4]);
        assertEquals(0b011000111010010100111110010100000111101100101111L, keys.keys()[5]);
        assertEquals(0b111011001000010010110111111101100001100010111100L, keys.keys()[6]);
        assertEquals(0b111101111000101000111010110000010011101111111011L, keys.keys()[7]);
        assertEquals(0b111000001101101111101011111011011110011110000001L, keys.keys()[8]);
        assertEquals(0b101100011111001101000111101110100100011001001111L, keys.keys()[9]);
        assertEquals(0b001000010101111111010011110111101101001110000110L, keys.keys()[10]);
        assertEquals(0b011101010111000111110101100101000110011111101001L, keys.keys()[11]);
        assertEquals(0b100101111100010111010001111110101011101001000001L, keys.keys()[12]);
        assertEquals(0b010111110100001110110111111100101110011100111010L, keys.keys()[13]);
        assertEquals(0b101111111001000110001101001111010011111100001010L, keys.keys()[14]);
        assertEquals(0b110010110011110110001011000011100001011111110101L, keys.keys()[15]);
    }

    @Test
    void testInitialPermutation() {
        var msg = 0b0000000100100011010001010110011110001001101010111100110111101111L;
        var permutation = Des.initialPermutation(msg);
        assertEquals(0b1100110000000000110011001111111111110000101010101111000010101010L, permutation);
    }

    @Test
    void testEncryption() {
        var key = 0b0001001100110100010101110111100110011011101111001101111111110001L;
        var keys = Des.generateSubKeys(key);
        var plain = 0b0000000100100011010001010110011110001001101010111100110111101111L;
        var cipher = Des.encryptBlock(plain, keys);
        assertEquals(0b1000010111101000000100110101010000001111000010101011010000000101L, cipher);
    }

    @Test
    void testDecryption() {
        var key = 0b0001001100110100010101110111100110011011101111001101111111110001L;
        var cipher = 0b1000010111101000000100110101010000001111000010101011010000000101L;
        var keys = Des.generateSubKeys(key);
        var plain = Des.decryptBlock(cipher, keys);
        assertEquals(0b0000000100100011010001010110011110001001101010111100110111101111L, plain);
    }
}