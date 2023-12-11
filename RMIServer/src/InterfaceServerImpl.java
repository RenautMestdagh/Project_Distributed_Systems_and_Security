import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
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
    public byte[] receiveMessage(int cellIndex, String preimageTag) throws RemoteException, NoSuchAlgorithmException {
        return bulletinBoard.get(cellIndex).get(cryptographicHash(preimageTag));
    }

    public void removeMessage(int cellIndex, String preimageTag) throws RemoteException, NoSuchAlgorithmException {
        bulletinBoard.get(cellIndex).remove(cryptographicHash(preimageTag));
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
