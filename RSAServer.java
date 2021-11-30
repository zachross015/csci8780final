import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
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


public class RSAServer implements RemoteStringArray {

    private List<String> array;

	private ServerConfig config;


    /** This constructor creates an object with a string array capacity of n
     *
     *  @param n The capacity of the string array
     */
    public RSAServer(ServerConfig config) {
        this.config = config;
        this.array = new ArrayList<String>(this.config.getCapacity());
        for (int i = 0; i < this.config.getElements().size(); i++) {
            this.array.add(this.config.getElements().get(i)); 
        }
        for (int i = 0; i < this.config.getCapacity() - this.config.getElements().size(); i++) {
            this.array.add("");
        }
    };

    public String get(Integer i) throws RemoteException {
        return array.get(i);
    }

    /** Copies str into the lth position only if client (indicated by client_id)
     * has a write lock. Returns true if successful and false if not
     * successful (e.g., client does not have the write lock). 
     *
     * @param str       String to write back to the server
     * @param l         Index being written to
     * @param client_id Id of client writing back to server
     */
    public void set(Integer i, String str) throws RemoteException {
        array.set(i, str);
    }

    /**
     * @return the capacity
     */
    public Integer getCapacity() throws RemoteException {
        return this.config.getCapacity();
    }

    public void startServer() throws RemoteException, AlreadyBoundException, NotBoundException {
        RemoteStringArray stub = (RemoteStringArray) UnicastRemoteObject.exportObject(this, this.config.getPort());
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
        System.out.println("RSAServer created");
        System.out.println(Arrays.toString(registry.list()));

        RemoteStringArrayLeader leader = (RemoteStringArrayLeader) registry.lookup(this.config.getLeader());
        leader.bind(this.config.getName(), this);
        System.out.println("RSAServer successfully bound to leader");
    }

    public void closeServer() throws RemoteException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry();
        registry.unbind(this.config.getName());
        System.out.println("Server unbound from registry");

        RemoteStringArrayLeader leader = (RemoteStringArrayLeader) registry.lookup(this.config.getLeader());
        leader.unbind(this.config.getName());
        System.out.println("Server unbound from leader");
    }

    public static void main(String[] args) throws Exception {

        if (args.length < 1) {
            throw new Exception("usage: java RSAServer [config file]"); 
        }

        ServerConfig config = new ServerConfig(args[0]);
        RSAServer rsae = new RSAServer(config);

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

