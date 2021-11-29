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
	boolean requestWriteLock(Integer l, Integer clientId, LongStream timeRange) throws RemoteException; 
	
    /** Releases a lock on an element in the distributed storage array.
     */
	void releaseLock(Integer l, Integer clientId, LongStream timeRange) throws RemoteException; 
	
    /** Fetches the remote string array for operations if and only if there are
     * no current write locks on the specified element. When fetched, it returns
     * the RSA along with the adjusted index of the element within that array.
     */
	Tuple<RemoteStringArray, Integer> fetchRemoteStringArray(Integer l, Integer clientId) throws RemoteException; 
	
    /** Binds a RemoteStringArray to this leader.
     */
    void bind(String name, RemoteStringArray rsa) throws RemoteException;

    /** Unbinds a RemoteStringArray from this leader.
     */
    void unbind(String name) throws RemoteException;

}

