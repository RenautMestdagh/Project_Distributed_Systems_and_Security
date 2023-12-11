import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;

public class InterfaceServerImpl extends UnicastRemoteObject implements InterfaceServer {

    private static final int bulletinBoardCells = 10;
    private final List<HashMap<String, byte[]>> bulletinBoard = new ArrayList<>(bulletinBoardCells) {{
        for (int i = 0; i < bulletinBoardCells; i++)
            add(new HashMap<>());
    }};


    public InterfaceServerImpl() throws RemoteException {

    }

    @Override
    public int getBulletinBoardCells () throws RemoteException {
        return bulletinBoardCells;
    }
    @Override
    public void sendMessage(int cellIndex, String preimageTag, byte[] message) throws RemoteException, NoSuchAlgorithmException {
        bulletinBoard.get(cellIndex).put(cryptographicHash(preimageTag), message);
    }

    @Override
    public synchronized ArrayList<String[]> receiveMessage(int cellIndex, String preimageTag, SecretKey symKeySender) throws RemoteException, NoSuchAlgorithmException {
        ArrayList<String[]> messages = new ArrayList<>();

        while (true){
            byte[] encryptedMessageWithMetadata = bulletinBoard.get(cellIndex).get(cryptographicHash(preimageTag));

            if(encryptedMessageWithMetadata==null)
                return messages;

            try{
                Cipher cipher = Cipher.getInstance("AES");
                cipher.init(Cipher.DECRYPT_MODE, symKeySender);

                String messageWithMetadata = new String(cipher.doFinal(Base64.getDecoder().decode(encryptedMessageWithMetadata)), StandardCharsets.UTF_8);
                bulletinBoard.get(cellIndex).remove(cryptographicHash(preimageTag));

                // String messageWithMetadata is built as: message,cellIndex,preimageTag

                String[] formattedMessage = new String[3];
                int lastIndex = messageWithMetadata.lastIndexOf(",");
                int secondLastCommaIndex = messageWithMetadata.lastIndexOf(',', lastIndex - 1);
                String message = messageWithMetadata.substring(0, secondLastCommaIndex);
                String nextCellIndex = messageWithMetadata.substring(secondLastCommaIndex+1, lastIndex);
                String nextPreimageTag = messageWithMetadata.substring(lastIndex + 1);

                formattedMessage[0] = message;
                formattedMessage[1] = nextCellIndex;
                formattedMessage[2] = nextPreimageTag;

                messages.add(formattedMessage);

                cellIndex = Integer.parseInt(nextCellIndex);
                preimageTag = nextPreimageTag;
                symKeySender = KDF.deriveKey(symKeySender);

            } catch (Exception e){
                System.out.println("Corrupted state");
                return messages;
            }
        }
    }

    private String cryptographicHash(String preimageTag) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(preimageTag.getBytes());

        byte[] byteData = digest.digest();

        StringBuilder sb = new StringBuilder();
        for (byte b : byteData)
            sb.append(String.format("%02x", b));

        return sb.toString();
    }
}
