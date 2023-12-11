import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
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
    public void sendMessage(int cellIndex, String tag, byte[] message) throws RemoteException {
        bulletinBoard.get(cellIndex).put(tag, message);
    }

    @Override
    public byte[] receiveMessage(int cellIndex, String preimageTag) throws RemoteException, NoSuchAlgorithmException {
        return bulletinBoard.get(cellIndex).remove(cryptographicHash.hashPreimageTag(preimageTag));
    }
}
