package secure;

import java.math.BigInteger;

public class Rsa {
    public static record RsaKey(BigInteger e, BigInteger n) {
    }

    public static record RsaKeys(RsaKey priv, RsaKey pub) {
    }

    public static BigInteger encrypt(BigInteger message, RsaKey key) {
        return message.modPow(key.e, key.n);
    }

    public static RsaKeys generateKeys(int bits) {
        var p = BigInteger.probablePrime(bits, Random.instance());
        var q = BigInteger.probablePrime(bits, Random.instance());
        var n = p.multiply(q);
        var phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));
        var lamb = phi.divide(p.subtract(BigInteger.ONE).gcd(q.subtract(BigInteger.ONE)));
        var e = BigInteger.valueOf(65537);
        assert e.compareTo(lamb) == -1;
        var d = e.modInverse(lamb);
        return new RsaKeys(new RsaKey(d, n), new RsaKey(e, n));
    }

    public static byte[] sign(String message, RsaKey key) {
        var digest = new BigInteger(Utils.signatureHash(message));
        return encrypt(digest, key).toByteArray();
    }

    public static boolean verify(String message, byte[] signature, RsaKey key) {
        var digest = new BigInteger(Utils.signatureHash(message));
        var signed = encrypt(new BigInteger(signature), key);
        return digest.equals(signed);
    }
}
