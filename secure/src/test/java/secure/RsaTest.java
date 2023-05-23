package secure;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.math.BigInteger;

public class RsaTest {
    @Test
    void testEncryptWithPubDecryptWithPriv() throws Exception {
        BigInteger msg = new BigInteger("42");
        var keyPair = Rsa.generateKeys(2048);
        BigInteger cipherText = Rsa.crypt(msg, keyPair.pub());
        BigInteger decryptedText = Rsa.crypt(cipherText, keyPair.priv());
        assertEquals(msg, decryptedText);
    }

    @Test
    void testEncryptWithPrivDecryptWithPub() throws Exception {
        BigInteger msg = new BigInteger("42");
        var keyPair = Rsa.generateKeys(2048);
        BigInteger cipherText = Rsa.crypt(msg, keyPair.priv());
        BigInteger decryptedText = Rsa.crypt(cipherText, keyPair.pub());
        assertEquals(msg, decryptedText);
    }

    @Test
    void testSignAndVerify() throws Exception {
        String msg = "Hello World!";
        var keyPair = Rsa.generateKeys(2048);
        var signature = Rsa.sign(msg, keyPair.priv());
        assertTrue(Rsa.verify("Hello World!", signature, keyPair.pub()));
    }

    @Test
    void testSignAndInvalid() throws Exception {
        String msg = "Hello World!";
        var keyPair = Rsa.generateKeys(2048);
        var signature = Rsa.sign(msg, keyPair.priv());
        signature[0]++;
        assertFalse(Rsa.verify("Hello World!", signature, keyPair.pub()));
    }

    @Test
    void testSignAndInvalid2() throws Exception {
        String msg = "Hello World!";
        var keyPair = Rsa.generateKeys(2048);
        var signature = Rsa.sign(msg, keyPair.priv());
        assertFalse(Rsa.verify("Hello World.", signature, keyPair.pub()));
    }
}
