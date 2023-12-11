import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.UUID;

public class Conversation {

    private static int bulletinBoardCells;
    private String name;
    private SecretKey ownKey;
    private int ownCell;
    private String ownTag;
    private SecretKey otherKey;
    private int otherCell;
    private String otherTag;

    public Conversation() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        this.ownKey = keyGen.generateKey();
        this.ownCell = getNewCell();
        this.ownTag = getNewTag();
    }

    public void setOther(String name, SecretKey otherKey, int otherCell, String otherTag) {
        this.name = name;
        this.otherKey = otherKey;
        this.otherCell = otherCell;
        this.otherTag = otherTag;
    }

    public static void setBulletinBoardCells(int bulletinBoardCells) {
        Conversation.bulletinBoardCells = bulletinBoardCells;
    }

    private static SecretKey deriveKey(SecretKey key) throws NoSuchAlgorithmException, InvalidKeySpecException {

        // Convert the SecretKey to PBEKeySpec
        char[] password = new char[0];
        PBEKeySpec keySpec = new PBEKeySpec(password, key.getEncoded(), 10000, 256);

        // Generate the derived key
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        return new SecretKeySpec(keyFactory.generateSecret(keySpec).getEncoded(), "AES");
    }

    public void setNewOwn() throws NoSuchAlgorithmException, InvalidKeySpecException {
        this.ownKey = deriveKey(ownKey);
        this.ownCell = getNewCell();
        this.ownTag = getNewTag();
    }

    public void setNewOther(int newOtherCell, String newOtherTag) throws NoSuchAlgorithmException, InvalidKeySpecException {
        this.otherKey = deriveKey(otherKey);
        this.otherCell = newOtherCell;
        this.otherTag = newOtherTag;
    }

    private int getNewCell() {
        return new SecureRandom().nextInt(bulletinBoardCells);
    }

    private String getNewTag() {
        return UUID.randomUUID().toString();
    }

    public String getName() {
        return name;
    }

    public SecretKey getOwnKey() {
        return ownKey;
    }

    public int getOwnCell() {
        return ownCell;
    }

    public String getOwnTag() {
        return ownTag;
    }

    public SecretKey getOtherKey() {
        return otherKey;
    }

    public int getOtherCell() {
        return otherCell;
    }

    public String getOtherTag() {
        return otherTag;
    }
}
