import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javax.crypto.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class MainClient extends Application {

    private InterfaceServer server;
    private final HashMap<String, Conversation> conversations = new HashMap<>();
    private Conversation tmpConversation = null;

    @Override
    public void start(Stage stage) throws IOException, NotBoundException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("GUI.fxml"));
        Parent root = loader.load();
        GUIController controller = loader.getController();
        controller.setGUIApplication(this);

        // fire to localhost port 1099
        Registry myRegistry = LocateRegistry.getRegistry("localhost", 1099);

        // search for CommunicationService
        server = (InterfaceServer) myRegistry.lookup("CommunicationService");
        int bulletinBoardCells = server.getBulletinBoardCells();
        Conversation.setBulletinBoardCells( String.valueOf(bulletinBoardCells).length() );

        Scene scene = new Scene(root, 810, 410);
        stage.setScene(scene);
        stage.setTitle("Chat Client");
        stage.show();


        // Schedule the task to run every 50 milliseconds with an initial delay of 0 milliseconds
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        ReceiveThread receiveThread = new ReceiveThread(server, controller, conversations);
        scheduler.scheduleAtFixedRate(receiveThread, 0, 50, TimeUnit.MILLISECONDS);


        // Set a handler for the window close event
        stage.setOnCloseRequest(event -> {
            // Ensure the JavaFX application exits properly
            scheduler.shutdown();
            Platform.exit();
        });
    }

    public void sendMessage(String chatName, String message) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, RemoteException {
        Conversation c = conversations.get(chatName);

        SecretKey currOwnKey = c.getOwnKey();
        int currOwnCell = c.getOwnCell();
        String currOwnTag = c.getOwnTag();

        c.setNewOwn();

        int nextOwnCell = c.getOwnCell();
        String nextTag = c.getOwnTag();

        String messageWithMetadata = message+","+nextOwnCell+","+nextTag;
        byte[] encryptedMessageWithMetadata = encryptMessageWithSymmetricKey(messageWithMetadata, currOwnKey);

        server.sendMessage(currOwnCell, currOwnTag, encryptedMessageWithMetadata);
    }

    public Conversation startNewConversation() throws NoSuchAlgorithmException {
        tmpConversation = new Conversation();
        return tmpConversation;
    }

    public void cancelNewConversation() {
        tmpConversation = null;
    }

    public void submitNewConversation(String chatName, SecretKey otherKey, int otherCell,  String otherTag) {
        tmpConversation.setOther(chatName, otherKey, otherCell,  otherTag);
        conversations.put(chatName, tmpConversation);
        cancelNewConversation();
    }

    private byte[] encryptMessageWithSymmetricKey(String message, SecretKey key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);

        byte[] encryptedMessage = cipher.doFinal(message.getBytes(StandardCharsets.UTF_8));

        return Base64.getEncoder().encode(encryptedMessage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
