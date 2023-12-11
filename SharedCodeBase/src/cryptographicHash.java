import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class cryptographicHash {

    public static String hashPreimageTag(String preimageTag) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(preimageTag.getBytes());

        byte[] byteData = digest.digest();

        StringBuilder sb = new StringBuilder();
        for (byte b : byteData)
            sb.append(String.format("%02x", b));

        return sb.toString();
    }

}
