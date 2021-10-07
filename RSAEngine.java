import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.lang.Exception;

import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;


public class RSAEngine implements RemoteStringArray {

    private List<Integer> writeLocks;
    private List<Set<Integer>> readLocks;
    private List<String> array;
    private Integer capacity=0;
    static Registry registry=null;

    private boolean isWriteLocked(Integer i) {
        return !(writeLocks.get(i) == -1);
    }

    private void insertReadLock(Integer i, Integer client_id) {
        if (readLocks.get(i).contains(client_id)) {
           return; 
        }
        Set<Integer> locks = readLocks.get(i);
        locks.add(client_id);
        readLocks.set(i, locks); 
    }

    private void removeReadLock(Integer i, Integer client_id) {
        Set<Integer> locks = readLocks.get(i);
        locks.remove(client_id);
        readLocks.set(i, locks); 
    }

    /** This constructor creates an object with a string array capacity of n
     *
     *  @param n The capacity of the string array
     */
    public RSAEngine(Integer n) {
        readLocks = new ArrayList<Set<Integer>>(n);
        writeLocks = new ArrayList<Integer>(n);
        array = new ArrayList<String>(n);
        capacity=n;

        // Populate initial arrays for read stability
        while (writeLocks.size()<n) {
            writeLocks.add(-1);
            array.add("");
            readLocks.add(new HashSet());
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
        readLocks.add(l, new HashSet());
        writeLocks.add(l, -1);
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
        System.out.println("Requesting read lock for client " + client_id);
        if (!isWriteLocked(l)) {
            insertReadLock(l, client_id);
            System.out.println("Read lock granted.");
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
        System.out.println("Requesting write lock for client " + client_id);
        if (!isWriteLocked(l)) {
            writeLocks.set(l, client_id); 
            System.out.println("Write lock granted.");
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
        System.out.println("Releasing lock for client " + client_id);
        if(readLocks.get(l).contains(client_id)) {
            removeReadLock(l, client_id);
            System.out.println("Read lock removed.");
        }
        if(writeLocks.get(l) == client_id) {
            writeLocks.set(l, -1);
            System.out.println("Write lock removed.");
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
        if (!readLocks.get(l).contains(client_id) && !writeLocks.get(l).equals(client_id)) {
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
        if (writeLocks.get(l).equals(client_id)) {
            System.out.println("Write lock id: " + writeLocks.get(l) + " Client id: " + client_id);
            return array.get(l);
        }
        throw new RemoteException("Element could not be fetched since the client does not have a write lock."); 
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

