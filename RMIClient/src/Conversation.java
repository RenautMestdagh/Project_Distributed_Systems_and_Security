import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.UUID;

public class Conversation {

    private static int bulletinBoardCells;
    private String name;
    private SecretKey ownKey;
    private int ownCell;
    private String ownPreimageTag;
    private SecretKey otherKey;
    private int otherCell;
    private String otherPreimageTag;

    public Conversation() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        this.ownKey = keyGen.generateKey();
        this.ownCell = getNewCell();
        this.ownPreimageTag = getNewPreimageTag();
    }

    public void setOther(String name, SecretKey otherKey, int otherCell, String otherPreimageTag) {
        this.name = name;
        this.otherKey = otherKey;
        this.otherCell = otherCell;
        this.otherPreimageTag = otherPreimageTag;
    }

    public static void setBulletinBoardCells(int bulletinBoardCells) {
        Conversation.bulletinBoardCells = bulletinBoardCells;
    }

    private static SecretKey deriveKey(SecretKey key) throws NoSuchAlgorithmException, InvalidKeySpecException {

        // Convert the SecretKey to PBEKeySpec
        String salt = "Dcf@@4!F79DwGy8f";
        PBEKeySpec keySpec = new PBEKeySpec(Base64.getEncoder().encodeToString(key.getEncoded()).toCharArray(), salt.getBytes(), 10000, 256);

        // Generate the derived key
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        return new SecretKeySpec(keyFactory.generateSecret(keySpec).getEncoded(), "AES");
    }

    public void setNewOwn() throws NoSuchAlgorithmException, InvalidKeySpecException {
        this.ownKey = deriveKey(ownKey);
        this.ownCell = getNewCell();
        this.ownPreimageTag = getNewPreimageTag();
    }

    public void setNewOther(int newOtherCell, String newOtherPreimageTag) throws NoSuchAlgorithmException, InvalidKeySpecException {
        this.otherKey = deriveKey(otherKey);
        this.otherCell = newOtherCell;
        this.otherPreimageTag = newOtherPreimageTag;
    }

    private int getNewCell() {
        return new SecureRandom().nextInt(bulletinBoardCells);
    }

    private String getNewPreimageTag() {
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

    public String getOwnPreimageTag() {
        return ownPreimageTag;
    }

    public SecretKey getOtherKey() {
        return otherKey;
    }

    public int getOtherCell() {
        return otherCell;
    }

    public String getOtherPreimageTag() {
        return otherPreimageTag;
    }
}
