import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.HashMap;

public class GUIController {

    private MainClient mainClient;
    private final HashMap<String, TextFlow> conversations = new HashMap<>();
    private String currentChatName;

    @FXML
    private HBox chatGUI;

    @FXML
    private VBox conversationList;

    @FXML
    private HBox currentChatLabel;

    @FXML
    private ScrollPane conversationScrollPane;

    @FXML
    private TextArea messageInput;



    @FXML
    private ScrollPane newChatGUI;

    @FXML
    private Label newChatNameError;

    @FXML
    private TextField newChatName;

    @FXML
    private TextArea newChatOwnInfo;

    @FXML
    private Label newChatInputError;

    @FXML
    private TextArea newChatOtherInfo;


    @FXML
    private void initialize() {
        newChatNameError.setManaged(false);
        newChatInputError.setManaged(false);
        newChatNameError.setVisible(false);
        newChatInputError.setVisible(false);

        messageInput.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                if (event.isShiftDown() || event.isAltDown()) {
                    // Shift+Enter or Alt+Enter was pressed, insert a newline
                    messageInput.appendText("\n");
                } else {
                    // Only Enter was pressed, send the message
                    try {
                        handleSendMessage();
                    } catch (NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException | InvalidKeySpecException | BadPaddingException | InvalidKeyException | RemoteException e) {
                        throw new RuntimeException(e);
                    }
                    event.consume(); // Prevent the newline character from being added
                }
            }
        });
    }

    public void setGUIApplication(MainClient mainClient) {
        this.mainClient = mainClient;
    }

    @FXML
    private void handleSendMessage() throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException, RemoteException {
        String message = messageInput.getText();
        //remomve leadinig and trailing spaces and enters
        //message = message.trim().replaceAll("^\\n+|\\n+$", "");
        try {
            if (!message.isEmpty()) {
                mainClient.sendMessage(currentChatName, message);
                addMessage(currentChatName, "You", message);
            }
        } catch (NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException | InvalidKeySpecException | BadPaddingException | InvalidKeyException | RemoteException e) {
            throw new RuntimeException(e);
        }
        messageInput.clear();
    }

    public void addMessage(String chatName, String from, String message) {

        Platform.runLater(() -> {

            Text senderText = new Text(from + ": ");
            senderText.setStyle("-fx-font-weight: bold");

            Text messageText = new Text(message+"\n");

            conversations.get(chatName).getChildren().addAll(senderText, messageText);
            conversationScrollPane.setVvalue(1.0);  // Scroll to the bottom

        });
    }

    public void newChat() throws NoSuchAlgorithmException {

        Conversation tmpConversation = mainClient.startNewConversation();

        String sb = Base64.getEncoder().encodeToString(tmpConversation.getOwnKey().getEncoded()) + "\n\n" +
                tmpConversation.getOwnCell() + "\n\n" +
                tmpConversation.getOwnPreimageTag();

        newChatOwnInfo.setText(sb);

        chatGUI.setVisible(false);
        newChatGUI.setVisible(true);
    }

    public void cancelNewChat(){
        newChatGUI.setVisible(false);
        chatGUI.setVisible(true);

        mainClient.cancelNewConversation();

        newChatName.clear();
        newChatOtherInfo.clear();
        newChatNameError.setManaged(false);
        newChatInputError.setManaged(false);
        newChatNameError.setVisible(false);
        newChatInputError.setVisible(false);

    }

    public void submitNewChat() {
        newChatNameError.setManaged(false);
        newChatInputError.setManaged(false);
        newChatNameError.setVisible(false);
        newChatInputError.setVisible(false);

        String chatName = newChatName.getText();
        if(chatName.isEmpty()){
            newChatNameError.setText("Chat name cannot be empty");
            newChatNameError.setManaged(true);
            newChatNameError.setVisible(true);
            return;
        }
        if(conversations.containsKey(chatName)){
            newChatNameError.setText("This name is already in use");
            newChatNameError.setManaged(true);
            newChatNameError.setVisible(true);
            return;
        }
        String[] otherInfoArr = newChatOtherInfo.getText().split("\n");
        if(otherInfoArr.length!=5){
            newChatInputError.setManaged(true);
            newChatInputError.setVisible(true);
            return;
        }
        if(otherInfoArr[0].isEmpty() || otherInfoArr[2].isEmpty() || otherInfoArr[4].isEmpty()){
            newChatInputError.setManaged(true);
            newChatInputError.setVisible(true);
            return;
        }

        byte[] otherKeyByteArr = Base64.getDecoder().decode(otherInfoArr[0]);
        SecretKey otherKey = new SecretKeySpec(otherKeyByteArr, 0, otherKeyByteArr.length, "AES");

        int otherCell = Integer.parseInt(otherInfoArr[2]);
        String otherTag = otherInfoArr[4];

        mainClient.submitNewConversation(chatName, otherKey, otherCell,  otherTag);

        Platform.runLater(() -> {

            Button b = new Button();
            b.setText(chatName);
            b.setMaxWidth(Double.MAX_VALUE);
            b.setOnAction(this::selectChat);
            VBox.setMargin(b, new Insets(0, 0, 5, 0));
            conversationList.getChildren().add(b);

            TextFlow t = new TextFlow();
            conversations.put(chatName, t);

            b.fire();

            cancelNewChat();

        });
    }

    public void selectChat(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        clickedButton.getText();
        currentChatName = clickedButton.getText();

        conversationScrollPane.setContent(conversations.get(currentChatName));

        Text t1 = new Text("Huidige chat: ");
        t1.setStyle("-fx-font-weight: bold");
        Text t2 = new Text(clickedButton.getText());

        currentChatLabel.getChildren().clear();
        currentChatLabel.getChildren().addAll(t1,t2);
        conversationScrollPane.setVvalue(1.0);  // Scroll to the bottom
    }
}
