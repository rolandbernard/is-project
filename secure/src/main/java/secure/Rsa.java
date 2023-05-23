package secure;

import java.math.BigInteger;

public class Rsa {
    public static record RsaKey(BigInteger e, BigInteger n) {
        public static RsaKey fromByteArray(byte[] bytes) {
            int exponentLength = Byte.toUnsignedInt(bytes[0]) | (Byte.toUnsignedInt(bytes[1]) << 8);
            int modulusLength = Byte.toUnsignedInt(bytes[2]) | (Byte.toUnsignedInt(bytes[3]) << 8);
            return new RsaKey(
                new BigInteger(1, bytes, 4, exponentLength),
                new BigInteger(1, bytes, exponentLength + 4, modulusLength));
        }

        public byte[] toByteArray() {
            var exponent = e.toByteArray();
            var modulus = n.toByteArray();
            var combined = new byte[4 + exponent.length + modulus.length];
            combined[0] = (byte)exponent.length;
            combined[1] = (byte)(exponent.length >>> 8);
            combined[2] = (byte)modulus.length;
            combined[3] = (byte)(modulus.length >>> 8);
            for (int i = 0; i < exponent.length; i++) {
                combined[4 + i] = exponent[i];
            }
            for (int i = 0; i < modulus.length; i++) {
                combined[4 + exponent.length + i] = modulus[i];
            }
            return combined;
        }
    }

    public static record RsaKeys(RsaKey priv, RsaKey pub) {
    }

    public static RsaKeys generateKeys(int bits) {
        var p = new BigInteger(bits / 2, 128, Random.instance());
        var q = new BigInteger(bits / 2, 128, Random.instance());
        var n = p.multiply(q);
        var phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));
        var lamb = phi.divide(p.subtract(BigInteger.ONE).gcd(q.subtract(BigInteger.ONE)));
        var e = BigInteger.valueOf(65537);
        assert e.compareTo(lamb) == -1;
        var d = e.modInverse(lamb);
        return new RsaKeys(new RsaKey(d, n), new RsaKey(e, n));
    }

    public static BigInteger crypt(BigInteger message, RsaKey key) {
        assert message.compareTo(key.n) < 0;
        return message.modPow(key.e, key.n);
    }

    public static byte[] sign(String message, RsaKey key) {
        var digest = new BigInteger(1, Hash.signatureHash(message));
        return crypt(digest, key).toByteArray();
    }

    public static boolean verify(String message, byte[] signature, RsaKey key) {
        var digest = new BigInteger(1, Hash.signatureHash(message));
        var signed = crypt(new BigInteger(1, signature), key);
        return digest.equals(signed);
    }
}
