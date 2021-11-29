import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.stream.LongStream;


public interface RemoteStringArrayLeader extends Remote {
	
    /** Fetches the maximum capacity contained by all connected servers.
     */
	Integer getCapacity() throws RemoteException;

    /** Requests a write lock from the leader. Write lock is granted following
     * the concurrency controls given in the Spanner paper.
     */
	boolean requestWriteLock(Integer l, Integer clientId, long time) throws RemoteException; 
	
    /** Releases a lock on an element in the distributed storage array.
     */
	void releaseLock(Integer l, Integer clientId, long time) throws RemoteException; 
	
    String get(Integer i, Integer clientId) throws RemoteException;

    void set(Integer i, String value, Integer clientId) throws RemoteException;
	
    /** Binds a RemoteStringArray to this leader.
     */
    void bind(String name, RemoteStringArray rsa) throws RemoteException;

    /** Unbinds a RemoteStringArray from this leader.
     */
    void unbind(String name) throws RemoteException;

}

