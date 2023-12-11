import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;

public interface InterfaceServer extends Remote {

    int getBulletinBoardCells () throws RemoteException;
    void sendMessage(int cellIndex, String preimageTag, byte[] message) throws RemoteException, NoSuchAlgorithmException;
    byte[] receiveMessage(int cellIndex, String preimageTag) throws RemoteException, NoSuchAlgorithmException;
    void removeMessage(int cellIndex, String preimageTag) throws RemoteException, NoSuchAlgorithmException;
}
