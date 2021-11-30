import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.lang.Exception;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.stream.LongStream;

import java.rmi.RemoteException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;


public class RSALeader implements RemoteStringArrayLeader {

    LeaderConfig config;

    List<Tuple<String, RemoteStringArray>> boundRSAs;    
    List<List<Integer>> locks;
    List<List<Long>> uncertaintyLocks;

    private Boolean isUnlockedForClient(Integer i, Integer clientId) throws RemoteException, ArrayIndexOutOfBoundsException {
        Tuple<Integer, Integer> ij = this.getRSAIndices(i);
        Integer lock = locks.get(ij.first).get(ij.second);
        return lock.equals(clientId) || lock.equals(-1);
    }

    private Boolean isUncertaintyLocked(Integer i, Long start) throws RemoteException, ArrayIndexOutOfBoundsException {
        Tuple<Integer, Integer> ij = this.getRSAIndices(i);
        Long ul = uncertaintyLocks.get(ij.first).get(ij.second);
        return start.compareTo(ul) < 0;
    }

    public void requestWriteLock(Integer i, Integer clientId, Long start, Long end) throws RemoteException {
            Tuple<Integer, Integer> ij = this.getRSAIndices(i);
            Integer lock = locks.get(ij.first).get(ij.second);
            if(!isUnlockedForClient(i, clientId) && !lock.equals(-1)) {
                throw new RemoteException("Unable to grant RW lock: another client already has a lock on this element.");
            } else if(isUncertaintyLocked(i, start)) {
                throw new RemoteException("Unable to grant RW lock: time range is within the current uncertainty.");
            }
            uncertaintyLocks.get(ij.first).set(ij.second, end);
            locks.get(ij.first).set(ij.second, clientId);
            System.out.println("Lock granted for index " + i + ". Timestamp: [" + start + " " + end + "]");
    }

    public void releaseLock(Integer i, Integer clientId, Long start, Long end) throws RemoteException {
        Tuple<Integer, Integer> ij = this.getRSAIndices(i);
        if(!locks.get(ij.first).get(ij.second).equals(clientId)) {
            throw new RemoteException("Unable to release lock: client was not locked to this in the first place.");
        }
        uncertaintyLocks.get(ij.first).set(ij.second, end);
        locks.get(ij.first).set(ij.second, -1);
        System.out.println("Lock released for index " + i + ". Timestamp: [" + start + " " + end + "]");
    }

    public void bind(String name, RemoteStringArray rsa) throws RemoteException {
        Tuple<String, RemoteStringArray> item = new Tuple<String, RemoteStringArray>(name, rsa);

        boundRSAs.add(item); 
        Integer n = item.second.getCapacity();
        List<Integer> inLocks = new ArrayList<Integer>(n);
        List<Long> inUncertaintyLocks = new ArrayList<Long>(n);
        for (int i = 0; i < n; i++) {
            inLocks.add(-1);
            inUncertaintyLocks.add(new Long(0)); 
        }
        locks.add(inLocks);
        uncertaintyLocks.add(inUncertaintyLocks);

        System.out.println(name + " successfully bound.");
    }


    public void unbind(String name) throws RemoteException {
        for (int i = 0; i < boundRSAs.size(); i++) {
            if (boundRSAs.get(i).first.equals(name)) {
                boundRSAs.remove(i);
                locks.remove(i);
                uncertaintyLocks.remove(i);
                System.out.println(name + " was successfully unbound.");
                break;
            }
        } 
        System.out.println(name + " was not found in the group.");
    }


    /**
     * @return the capacity
     */
    public Integer getCapacity() throws RemoteException {
        Integer capacity = 0;
        for (int i = 0; i < boundRSAs.size(); i++) {
            capacity += boundRSAs.get(i).second.getCapacity(); 
        }
        return capacity;
    }


    private Tuple<Integer, Integer> getRSAIndices(int j) throws RemoteException {
        int k = 0;
        for (int i = 0; i < boundRSAs.size(); i++) {
            RemoteStringArray elem = boundRSAs.get(i).second;
            int n = elem.getCapacity(); 
            if(j < n) {
                return new Tuple<Integer, Integer>(k, j);
            }
            j -= n;
            k++;
        }
        throw new ArrayIndexOutOfBoundsException("Index out of bounds");
    }

    private String getElement(Integer i, Integer j) throws RemoteException {
        return boundRSAs.get(i).second.get(j);
    }

    private void setElement(Integer i, Integer j, String val) throws RemoteException {
        boundRSAs.get(i).second.set(j, val);
    }

    public String get(Integer i, Integer clientId, Long start, Long end) throws RemoteException {
        System.out.println("Attempting to get index " + i + " for client " + clientId + ". Timestamp: [" + start + " " + end + "]"); 
        try {
            if(isUnlockedForClient(i, clientId)) {
                Tuple<Integer, Integer> ij = this.getRSAIndices(i);
                return getElement(ij.first, ij.second);
            }
            if(isUncertaintyLocked(i, start)) {
                throw new RemoteException("Read request not granted since the given time stamp lies within the uncertainty interval. Timestamp: [" + start + " " + end + "]");
            }
            throw new RemoteException("Read request not granted since the element is locked. Try again.");
        } catch(Exception e){
            System.out.println(e.getMessage());
            throw new RemoteException("An error occurred when attempting `get`.");
        }
    }


    public void set(Integer i, String val, Integer clientId, Long start, Long end) throws RemoteException {
        System.out.println("Attempting to set index " + i + " for client " + clientId + ". Timestamp: [" + start + " " + end + "]"); 
        if(isUnlockedForClient(i, clientId)) {
            Tuple<Integer, Integer> ij = this.getRSAIndices(i);
            setElement(ij.first, ij.second, val);
            return;
        }
        throw new RemoteException("Write request not granted since the client does not have a lock. Try again.");
    }


    public void startServer() throws RemoteException, AlreadyBoundException, NotBoundException {
        RemoteStringArrayLeader stub = (RemoteStringArrayLeader) UnicastRemoteObject.exportObject(this, this.config.getPort());
        Registry registry = null;

        // Get an existing registry if it exists at the host:port.
        // Otherwise, create a new one. This is here because I ran into an
        // issue where using getRegistry would result in a error thrown by
        // RMI for trying to attach to a registry that didn't exist.
        try {
            registry = LocateRegistry.getRegistry();
        } catch (Exception e) {
            System.out.println("Failed to locate registry, creating new one");
            registry = LocateRegistry.createRegistry(this.config.getPort());
        }
        registry.bind(this.config.getName(), stub);
        System.out.println("RSALeader created");
    }

    public void closeServer() throws RemoteException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry();
        registry.unbind(this.config.getName());
    }

    public RSALeader(LeaderConfig config) {
        this.config = config;
        this.boundRSAs = new ArrayList<Tuple<String, RemoteStringArray>>();
        this.locks = new ArrayList<List<Integer>>();
        this.uncertaintyLocks = new ArrayList<List<Long>>();
    }

    public static void main(String[] args) throws Exception {

        if (args.length < 1) {
            throw new Exception("usage: java RSAServer [config file]"); 
        }

        LeaderConfig config = new LeaderConfig(args[0]);
        RSALeader rsae = new RSALeader(config);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    rsae.closeServer();
                } catch(Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        });

        rsae.startServer();

    }

}

