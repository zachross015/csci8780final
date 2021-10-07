import java.util.ArrayList;
import java.lang.Exception;

import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;


public class RSAEngine implements RemoteStringArray {

    private ArrayList<Integer> writeLocks;
    private ArrayList<Integer> readLocks;
    private ArrayList<String> array;
    private Integer capacity=0;
    static Registry registry = null;

    boolean isLocked(Integer i) {
        return !(readLocks.get(i) == -1 && writeLocks.get(i) == -1);
    }
	
    /** This constructor creates an object with a string array capacity of n
     *
     *  @param n The capacity of the string array
     */
	public RSAEngine(Integer n) {
        readLocks = new ArrayList<Integer>(n);        
        writeLocks = new ArrayList<Integer>(n);
        array = new ArrayList<String>(n);
        capacity=n;
        
        // Populate initial arrays for read stability
        while (readLocks.size()<n) {
        	readLocks.add(-1);
        	writeLocks.add(-1);
        	array.add("");
        }
	};
	
    /** Inserts str as the lth element of the string array. You can assume that
     * l is less than the capacity of the String array
     *
     * @param l   The index to insert the element to
     * @param str The string to be inserted
     */
	public void insertArrayElement(Integer l, String str) throws RemoteException {
		array.add(l, str);
		return;
	}
	
    /** Request read lock on lth element of the array. client_id indicates the
     * identifier of the client requesting the lock. Return true if lock is
     * granted and false otherwise.
     *
     * @param l         The index of the array to request a read lock on 
     * @param client_id Identifier for the client requesting a read lock
     * @return          True if the lock is granted, false otherwise
     */
	public boolean requestReadLock(Integer l, Integer client_id) throws RemoteException{
        if (!isLocked(l)) {
            readLocks.set(l, client_id); 
            return true;
        }
        return false;
	}

    /** Request write lock on lth element of the array. client_id indicates the
     * identifier of the client requesting the lock. Return true if lock is
     * granted and false otherwise.
     *
     * @param l         The index of the array to request a write lock on 
     * @param client_id Identifier for the client requesting a write lock
     * @return          True if the lock is granted, false otherwise
     */
	public boolean requestWriteLock(Integer l, Integer client_id) throws RemoteException{
        if (!isLocked(l)) {
            writeLocks.set(l, client_id); 
            return true;
        }
        return false;
	}
	
    /**  Release the read/write lock on lth element. client_id indicates the
     * identifier of the client requesting the lock. You can assume that only
     * clients holding a lock will seek to release it. 
     *
     * @param l         Index to release the lock on
     * @param client_id Id of client requesting release
     */
	public void releaseLock(Integer l, Integer client_id) throws RemoteException {
        if(readLocks.get(l) == client_id) {
            readLocks.set(l, -1);
        }
        if(writeLocks.get(l) == client_id) {
            readLocks.set(l, -1);
        }
        return;
	}
	
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
	public String fetchElementRead(Integer l, Integer client_id) throws RemoteException {
        if (readLocks.get(l) != client_id) {
            throw new RemoteException("Element could not be fetched since the client does not have a read lock."); 
        }
        return array.get(l);
	}
	
    /** Returns the String at the lth location in the read/write mode. Analogous
     * to the fetchElementRead method outlined above. 
     *
     * @param l         Index of the element which read/write access is being
     * granted
     * @param client_id Id of the client requesting access
     */
	public String fetchElementWrite(Integer l, Integer client_id) throws RemoteException {
        if (writeLocks.get(l) != client_id) {
            throw new RemoteException("Element could not be fetched since the client does not have a write lock."); 
        }
        return array.get(l);
	}
	
    /** Copies str into the lth position only if client (indicated by client_id)
     * has a write lock. Returns true if successful and false if not
     * successful (e.g., client does not have the write lock). 
     *
     * @param str       String to write back to the server
     * @param l         Index being written to
     * @param client_id Id of client writing back to server
     */
	public boolean writeBackElement(String str, Integer l, Integer client_id) throws RemoteException {
        if (writeLocks.get(l) == client_id) {
            array.set(l, str);
            return true;
        }
        return false;
	}

    /**
	 * @return the capacity
	 */
	public Integer getCapacity() throws RemoteException {
		return capacity;
	}

	public static void main(String[] args) throws Exception {
        
        if (args.length < 1) {
           //throw new Exception("No configuration file was given to the server."); 
        }



            String name = "RSA";

            RSAEngine rsae = new RSAEngine(10);
            Integer port = 0; // Set to 0 for an anonymous port
            

            //System.setProperty("java.rmi.server.hostname", "192.168.1.2");
            RemoteStringArray stub = (RemoteStringArray) UnicastRemoteObject.exportObject(rsae, port);
            
            // Get an existing registry if it exists at the host:port.
            // Otherwise, create a new one. This is here because I ran into an
            // issue where using getRegistry would result in a error thrown by
            // RMI for trying to attach to a registry that didn't exist.
            try {
                registry = LocateRegistry.getRegistry();
            } catch (Exception e) {
            	System.out.println("Failed to locate registry, creating new one");
                registry = LocateRegistry.createRegistry(port);
            }
            registry.bind(name, stub);
            System.out.println("RSAEngine bound");
            
    
    }

}

