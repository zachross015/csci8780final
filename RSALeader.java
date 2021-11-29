import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.lang.Exception;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.stream.LongStream;

import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;


public class RSALeader implements RemoteStringArray {


    List<Tuple<String, RemoteStringArray>> boundRSAs;    
    List<List<Integer>> locks;
    List<List<LongStream>> uncertaintyLocks;

    private isLocked(Integer i, Integer clientId) {
        Tuple ij = this.getRSAIndices(i);
        return locks.get(ij.first).get(ij.second) == clientId;
    }

    private isUncertaintyLocked(Integer i, LongStream range) {
        Tuple ij = this.getRSAIndices(i);
        LongStream ul = uncertaintyLocks.get(ij.first).get(ij.second);
        return range.min().getAsLong() < uncertaintyLocks.max().getAsLong();
    }

    private Boolean isWriteLocked(Integer i, Integer clientId, LongStream range) {
        return isLocked(i, clientId) || isUncertaintyLocked(i, range);
    }

    public void requestWriteLock(Integer i, Integer clientId, LongStream range) throws RemoteException {
        Tuple ij = this.getRSAIndices(i);
        Integer lock = locks.get(ij.first).get(ij.second);
        if(lock != -1 && lock != clientId) {
            throw new RemoteException("Unable to grant RW lock: another client already has a lock on this element.");
        } else if(isUncertaintyLocked(i, range)) {
            throw new RemoteException("Unable to grant RW lock: time range is within the current uncertainty.");
        }
        uncertaintyLocks.get(ij.first).set(ij.second, range);
        locks.get(ij.first).set(ij.second, clientId);
        System.out.println("Lock granted for client " + clientId);
    }

    public void releaseLock(Integer i, Integer clientId) throws RemoteException {
        Tuple ij = this.getRSAIndices(i);
        if(locks.get(ij.first).get(ij.second) != clientId) {
            throw new RemoteException("Unable to release lock: client was not locked to this in the first place.");
        }
        locks.get(ij.first).set(ij.second, -1);
    }

    public void bind(String name, RemoteStringArray rsa) throws RemoteException {
        boundRSAs.add(new Tuple(name, rsa)); 
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


    private Tuple<Integer, Integer> getRSAIndices(Integer j) throws Exception {
        Integer k = 0;
        for (int i = 0; i < boundRSAs.size(); i++) {
            RemoteStringArray elem = boundRSAs.get(i).second;
            Integer n = elem.getCapacity(); 
            if(j < n) {
                return Tuple(k, j);
            }
            j -= n;
            k++;
        }
        throw new Exception("Index out of bounds");
    }

    private String getElement(Integer i, Integer j) {
        return boundRSAs.get(i).second.get(j);
    }

    private void setElement(Integer i, Integer j, String val) {
        boundRSAs.get(i).second.set(j, val);
    }

    public String get(Integer i, Integer clientId, LongStream tt) throws RemoteException {
        if(!this.isWriteLocked(i, clientId, tt)) {
            Tuple ij = this.getRSA(i);
            return getElement(ij.first, ij.second);
        }
        throw new RemoteException("Read request not granted. Try again.");
    }


    public void set(Integer i, String val, Integer clientId, LongStream tt) throws RemoteException {
        if(this.isWriteLocked(i, clientId, tt)) {
            Tuple ij = this.getRSA(i);
            return setElement(ij.first, ij.second, val);
        }
        throw new RemoteException("Write request not granted. Try again.");
    }


    public static void main(String[] args) throws Exception {



    }

}

