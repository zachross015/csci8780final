import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import java.util.Scanner;
import java.util.stream.LongStream;
import java.util.Random;

import java.io.File;
import java.io.FileNotFoundException;

public class StringArrayClient {

    private Registry registry;
	private RemoteStringArrayLeader leaderStub;

	private ClientConfig cConfig;
    private HyperParameterConfig hConfig;

	private String element = null;
	private String mutated_element = null;

    private long lastError = 0;
    private long start = 0;
    private long end = 0;


	public StringArrayClient(ClientConfig cConfig, HyperParameterConfig hConfig) throws Exception {

		this.cConfig = cConfig;
		this.hConfig = hConfig;

        // Getting the registry and leader access
        this.registry = LocateRegistry.getRegistry();
        this.leaderStub = (RemoteStringArrayLeader) this.registry.lookup(this.cConfig.getServer());
    }


	void getArrayCapacity() {
		try {
			System.out.println(this.leaderStub.getCapacity());
		} catch (RemoteException e) {
            System.out.println(e.getMessage());
			System.out.println("Failure to get array capacity");
		}
	}

    void calculateDisplacedTime() {
        long unixTime = System.currentTimeMillis();
        long error = lastError + (hConfig.getGamma() * Util.bernoulli(hConfig.getProbability()));
        LongStream tt = LongStream.range(unixTime - error, unixTime + error);
        this.start = unixTime - error;
        this.end = unixTime + error;
        lastError = error;
    }

	void fetchElementRead(int i) throws RemoteException {
        this.calculateDisplacedTime();
        this.element = this.leaderStub.get(i, this.cConfig.getId(), this.start, this.end);
        System.out.println("Success to fetch element in R mode");
	}


	void fetchElementWrite(int i) throws RemoteException {
        this.calculateDisplacedTime();
        this.leaderStub.requestWriteLock(i, this.cConfig.getId(), this.start, this.end);
        this.element = this.leaderStub.get(i, this.cConfig.getId(), this.start, this.end);
        System.out.println("Success to fetch element in R/W mode");
	}

	void printElement(int i) {
        try {
            this.fetchElementRead(i);
            System.out.println(this.element);
        } catch(RemoteException e) {
            System.out.println(e.getMessage());
        }
	}

	void concatenate(String str, int i) {
		this.mutated_element = this.element + str;
		this.writeback(i);
	}

	void writeback(int i) {
		try {
            this.calculateDisplacedTime();
            this.leaderStub.set(i, this.mutated_element, this.cConfig.getId(), this.start, this.end);
            this.element = this.mutated_element;
		} catch (RemoteException e) {
            System.out.println(e.getMessage());
			System.out.println("Failure to writeback");
		}
	}

	void releaseLock(int i) {
		try {
            this.calculateDisplacedTime();
			this.leaderStub.releaseLock(i, this.cConfig.getId(), this.start, this.end);
			this.element=null;
			this.mutated_element=null;
		} catch (RemoteException e) {
            System.out.println(e.getMessage());
			System.out.println("Failed to release lock -- check server connection");
		}
	}

    RemoteStringArray connectTo(String name) throws Exception {
        return (RemoteStringArray) this.registry.lookup(name);
    }

    Boolean handleInput(String input) throws Exception {
        String[] commands = input.split(" ");     
        switch(commands[0]) {
            case "capacity":
                this.getArrayCapacity();
                break;
            case "lock":
                if(commands.length < 2){
                    System.out.println("Insufficient arguments. usage: lock [index]");
                    break;
                }
                this.fetchElementWrite(Integer.parseInt(commands[1]));
                break;
            case "print":
                if(commands.length < 2){
                    System.out.println("Insufficient arguments. usage: print [index]");
                    break;
                }
                this.printElement(Integer.parseInt(commands[1]));
                break;
            case "concatenate":
                if(commands.length < 3){
                    System.out.println("Insufficient arguments. usage: concatenate [value] [index]");
                    break;
                }
                this.concatenate(commands[2], Integer.parseInt(commands[1]));
                break;
            case "release":
                if(commands.length<2){
                    System.out.println("Insufficient arguments. usage: release [index]");
                    break;
                }
                this.releaseLock(Integer.parseInt(commands[1]));
                break;
            case "quit":
                return true;
            default:
                System.out.println("Command not found. Available commands are:");
                System.out.println("capacity    - Prints the capacity of the distributed string array.");
                System.out.println("print       - Reads and prints the element at the given index.");
                System.out.println("lock        - Locks the element at the given index for writing.");
                System.out.println("release     - Releases the lock for the element at the given index.");
                System.out.println("concatenate - Appends the given value to the value at the given index.");
        }
        return false;
    }


	public static void main(String[] args) {

        if (args.length < 2) {
            System.out.println("usage: java StringArrayClient [config file] [hyperparameter file]");
            return;
        }

        try {

            // Load properties
            ClientConfig clientConfig = new ClientConfig(args[0]);
            HyperParameterConfig hpConfig = new HyperParameterConfig(args[1]);

            StringArrayClient arrClient = new StringArrayClient(clientConfig, hpConfig);

			Scanner input = new Scanner(System.in);
			String line;
            Boolean terminated = false;
            System.out.println("Available commands are:");
            System.out.println("capacity    - Prints the capacity of the distributed string array.");
            System.out.println("print       - Reads and prints the element at the given index.");
            System.out.println("lock        - Locks the element at the given index for writing.");
            System.out.println("release     - Releases the lock for the element at the given index.");
            System.out.println("concatenate - Appends the given value to the value at the given index.");
			while (!terminated) {
                try {
				line = input.nextLine();
                terminated = arrClient.handleInput(line);
                } catch(RemoteException e) {
                    System.out.println(e.getMessage());
                }
			}
			input.close();

        } catch(Exception e){
            System.out.println(e.getMessage());
        }

	}

}
