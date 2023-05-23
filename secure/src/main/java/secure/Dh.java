package secure;

import java.math.BigInteger;

public class Dh {
    private static final BigInteger g = new BigInteger(
            "3FB32C9B73134D0B2E77506660EDBD484CA7B18F21EF2054"
                    + "07F4793A1A0BA12510DBC15077BE463FFF4FED4AAC0BB555"
                    + "BE3A6C1B0C6B47B1BC3773BF7E8C6F62901228F8C28CBB18"
                    + "A55AE31341000A650196F931C77A57F2DDF463E5E9EC144B"
                    + "777DE62AAAB8A8628AC376D282D6ED3864E67982428EBC83"
                    + "1D14348F6F2F9193B5045AF2767164E1DFC967C1FB3F2E55"
                    + "A4BD1BFFE83B9C80D052B985D182EA0ADB2A3B7313D3FE14"
                    + "C8484B1E052588B9B7D2BBD2DF016199ECD06E1557CD0915"
                    + "B3353BBB64E0EC377FD028370DF92B52C7891428CDC67EB6"
                    + "184B523D1DB246C32F63078490F00EF8D647D148D4795451"
                    + "5E2327CFEF98C582664B4C0F6CC41659",
            16);
    private static final BigInteger p = new BigInteger(
            "87A8E61DB4B6663CFFBBD19C651959998CEEF608660DD0F2"
                    + "5D2CEED4435E3B00E00DF8F1D61957D4FAF7DF4561B2AA30"
                    + "16C3D91134096FAA3BF4296D830E9A7C209E0C6497517ABD"
                    + "5A8A9D306BCF67ED91F9E6725B4758C022E0B1EF4275BF7B"
                    + "6C5BFC11D45F9088B941F54EB1E59BB8BC39A0BF12307F5C"
                    + "4FDB70C581B23F76B63ACAE1CAA6B7902D52526735488A0E"
                    + "F13C6D9A51BFA4AB3AD8347796524D8EF6A167B5A41825D9"
                    + "67E144E5140564251CCACB83E6B486F6B3CA3F7971506026"
                    + "C0B857F689962856DED4010ABD0BE621C3A3960A54E710C3"
                    + "75F26375D7014103A4B54330C198AF126116D2276E11715F"
                    + "693877FAD7EF09CADB094AE91E1A1597",
            16);

    public static record DhKey(BigInteger k) {
        public static DhKey fromByteArray(byte[] bytes) {
            return new DhKey(new BigInteger(1, bytes));
        }

        public byte[] toByteArray() {
            return k.toByteArray();
        }
    }

    public static record DhKeys(DhKey priv, DhKey pub) {
    }

    public static DhKeys generateKeys() {
        var priv = (new BigInteger(1, Random.instance().nextBytes(1 + p.bitLength() / 8))).mod(p);
        var pub = g.modPow(priv, p);
        return new DhKeys(new DhKey(priv), new DhKey(pub));
    }

    private static BigInteger sharedSecret(DhKey priv, DhKey pub) {
        return pub.k.modPow(priv.k, p);
    }

    public static byte[] generateKey(DhKeys own, DhKey other, int length) {
        if (other.k.compareTo(own.pub.k) < 0) {
            return Hash.stretchKey(length, sharedSecret(own.priv, other).toByteArray(),
                    other.toByteArray(), own.pub.toByteArray());
        } else {
            return Hash.stretchKey(length, sharedSecret(own.priv, other).toByteArray(),
                    own.pub.toByteArray(), other.toByteArray());
        }
    }
}
