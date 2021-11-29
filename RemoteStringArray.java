import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.stream.LongStream;


public interface RemoteStringArray extends Remote {
	
	Integer getCapacity() throws RemoteException;

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
	String get(Integer i) throws RemoteException; 
	
    /** Copies str into the lth position only if client (indicated by client_id)
     * has a write lock. Returns true if successful and false if not
     * successful (e.g., client does not have the write lock). 
     *
     * @param str       String to write back to the server
     * @param l         Index being written to
     * @param client_id Id of client writing back to server
     */
	void set(Integer i, String str) throws RemoteException; 

}

