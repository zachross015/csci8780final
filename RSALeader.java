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

    public Boolean isWriteLocked(Integer i) {
        return false;
    }

    public Boolean hasWriteLock(Integer i, Integer clientId) {
        // TODO: Implement this
        return false; 
    }

    public void bind(String name, RemoteStringArray rsa) throws RemoteException {
        boundRSAs.add(new Tuple(name, rsa)); 
    }

    public void unbind(String name) throws RemoteException {
        for (int i = 0; i < boundRSAs.size(); i++) {
            if (boundRSAs.get(i).first.equals(name)) {
                boundRSAs.remove(i);
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

    Tuple<RemoteStringArray, Integer> fetchRemoteStringArray(Integer i, Integer clientId) throws RemoteException, Exception {
        if (!this.isWriteLocked(i) || this.hasWriteLock(i, clientId)) {
            Integer j = i;
            for (Integer k = 0; k < boundRSAs.size(); k++) {
                Integer n = boundRSAs.get(k).second.getCapacity();
                if (j - n < 0) {
                   return new Tuple(boundRSAs.get(k).second, j); 
                }
                j -= n;
            }
        } 
        throw new RemoteException("Failed to fetch remote string array: element is write locked.");
    }

    public static void main(String[] args) throws Exception {



    }

}

