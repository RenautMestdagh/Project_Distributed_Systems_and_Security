import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class KDF {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int DERIVED_KEY_LENGTH = 256;
    private static final int ITERATIONS = 10000;
    public static SecretKey deriveKey(SecretKey key) throws NoSuchAlgorithmException, InvalidKeySpecException {

        // Convert the SecretKey to PBEKeySpec
        char[] password = new char[0];
        PBEKeySpec keySpec = new PBEKeySpec(password, key.getEncoded(), ITERATIONS, DERIVED_KEY_LENGTH);

        // Generate the derived key
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM);
        return new SecretKeySpec(keyFactory.generateSecret(keySpec).getEncoded(), "AES");
    }
}
