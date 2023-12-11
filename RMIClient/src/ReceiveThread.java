import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.HashMap;

public class ReceiveThread implements Runnable {
    private final InterfaceServer server;
    private final GUIController controller;
    private final HashMap<String, Conversation> conversations;

    public ReceiveThread(InterfaceServer server, GUIController controller, HashMap<String, Conversation> conversations) {
        this.server = server;
        this.controller = controller;
        this.conversations = conversations;
    }

    @Override
    public void run() {
        try {
            for (Conversation c : conversations.values()) {
                while(true){

                    byte[] encryptedMessageWithMetadata = server.receiveMessage(c.getOtherCell(), c.getOtherPreimageTag());

                    if(encryptedMessageWithMetadata==null)
                        break;

                    Cipher cipher = Cipher.getInstance("AES");
                    cipher.init(Cipher.DECRYPT_MODE, c.getOtherKey());

                    String messageWithMetadata = new String(cipher.doFinal(Base64.getDecoder().decode(encryptedMessageWithMetadata)), StandardCharsets.UTF_8);

                    // String messageWithMetadata is built as: message,cellIndex,preimageTag

                    int lastCommaIndex = messageWithMetadata.lastIndexOf(",");
                    int secondLastCommaIndex = messageWithMetadata.lastIndexOf(',', lastCommaIndex - 1);

                    String message = messageWithMetadata.substring(0, secondLastCommaIndex);
                    int nextCellIndex = Integer.parseInt(messageWithMetadata.substring(secondLastCommaIndex+1, lastCommaIndex));
                    String nextPreimageTag = messageWithMetadata.substring(lastCommaIndex + 1);

                    c.setNewOther(nextCellIndex, nextPreimageTag);
                    controller.addMessage(c.getName(), "Other", message);

                }
            }
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException | RemoteException e) {
            throw new RuntimeException(e);
        }
    }
}
