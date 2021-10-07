import java.rmi.Remote;
import java.rmi.RemoteException;


public interface RemoteStringArray extends Remote {
	
	Integer getCapacity() throws RemoteException;

    /** Inserts str as the lth element of the string array. You can assume that
     * l is less than the capacity of the String array
     *
     * @param l   The index to insert the element to
     * @param str The string to be inserted
     */
	void insertArrayElement(Integer l, String str) throws RemoteException; 
	
    /** Request read lock on lth element of the array. client_id indicates the
     * identifier of the client requesting the lock. Return true if lock is
     * granted and false otherwise.
     *
     * @param l         The index of the array to request a read lock on 
     * @param client_id Identifier for the client requesting a read lock
     * @return          True if the lock is granted, false otherwise
     */
	boolean requestReadLock(Integer l, Integer client_id) throws RemoteException; 

    /** Request write lock on lth element of the array. client_id indicates the
     * identifier of the client requesting the lock. Return true if lock is
     * granted and fasle otherwise.
     *
     * @param l         The index of the array to request a write lock on 
     * @param client_id Identifier for the client requesting a write lock
     * @return          True if the lock is granted, false otherwise
     */
	boolean requestWriteLock(Integer l, Integer client_id) throws RemoteException; 
	
    /**  Release the read/write lock on lth element. client_id indicates the
     * identifier of the client requesting the lock. You can assume that only
     * clients holding a lock will seek to release it. 
     *
     * @param l         Index to release the lock on
     * @param client_id Id of client requesting release
     */
	void releaseLock(Integer l, Integer client_id) throws RemoteException; 
	
    /** Returns the String at the lth location in the read-only mode. Depending
     * on your design, you can issue a read lock to the client (if possible) as
     * a part of this method. Alternately, you can implement a separate method
     * for obtaining the read lock which has to be successfully executed by the
     * client for this method to succeed. Failure can be indicated by raising an
     * exception or returning a null object (design decision left to you).
     *
     * @param l         Index of the element which read-only access is being
     * granted
     * @param client_id Id of the client requesting access
     */
	String fetchElementRead(Integer l, Integer client_id) throws RemoteException; 
	
    /** Returns the String at the lth location in the read/write mode. Analogous
     * to the fetchElementRead method outlined above. 
     *
     * @param l         Index of the element which read/write access is being
     * granted
     * @param client_id Id of the client requesting access
     */
	String fetchElementWrite(Integer l, Integer client_id) throws RemoteException; 
	
    /** Copies str into the lth position only if client (indicated by client_id)
     * has a write lock. Returns true if successful and false if not
     * successful (e.g., client does not have the write lock). 
     *
     * @param str       String to write back to the server
     * @param l         Index being written to
     * @param client_id Id of client writing back to server
     */
	boolean writeBackElement(String str, Integer l, Integer client_id) throws RemoteException; 

}

