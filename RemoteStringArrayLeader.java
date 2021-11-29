import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.stream.LongStream;


public interface RemoteStringArrayLeader extends Remote {
	
    /** Fetches the maximum capacity contained by all connected servers.
     */
	Integer getCapacity() throws RemoteException;


    String get(Integer i, Integer clientId, LongStream range) throws RemoteException;

    void set(Integer i, String value, Integer clientId, LongStream range) throws RemoteException;
	
    /** Binds a RemoteStringArray to this leader.
     */
    void bind(String name, RemoteStringArray rsa) throws RemoteException;

    /** Unbinds a RemoteStringArray from this leader.
     */
    void unbind(String name) throws RemoteException;

    void requestWriteLock(Integer i, Integer clientId, LongStream range) throws RemoteException;

    void releaseLock(Integer i, Integer clientId, LongStream range) throws RemoteException;

}

