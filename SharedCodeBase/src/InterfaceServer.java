import javax.crypto.SecretKey;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public interface InterfaceServer extends Remote {

    int getBulletinBoardCells () throws RemoteException;
    void sendMessage(int cellIndex, String preimageTag, byte[] message) throws RemoteException, NoSuchAlgorithmException;
    ArrayList<String[]> receiveMessage(int cellIndex, String preimageTag, SecretKey symKeySender) throws RemoteException, NoSuchAlgorithmException;
}
