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


	public StringArrayClient(ClientConfig cConfig, HyperParameterConfig hConfig) throws Exception {

		this.cConfig = cConfig;
		this.hConfig = hConfig;

        // Getting the registry and leader access
        Registry registry = LocateRegistry.getRegistry(null);
        leaderStub = (RemoteStringArrayLeader) this.registry.lookup(this.cConfig.getServer());
    }


	void getArrayCapacity() {
		try {
			System.out.println(this.leaderStub.getCapacity());
		} catch (RemoteException e) {
			System.out.println("Failure to get array capacity");
		}
	}

    LongStream displacedTimeRange() {
        long unixTime = System.currentTimeMillis();
        Random random = new Random(unixTime);
        long endingOffset = (random.nextLong() % this.hConfig.getDeltaMS()) + unixTime;
        long beginningOffset = unixTime - (this.hConfig.getDeltaMS() - endingOffset);
        return LongStream.range(beginningOffset, endingOffset);
    }

	void fetchElementRead(int i) {
		try {
            Tuple<RemoteStringArray, Integer> rsa = this.leaderStub.fetchRemoteStringArray(i, this.cConfig.getId());
            this.element = rsa.first.get(rsa.second);
			System.out.println("Success to fetch element in R mode");
		} catch (RemoteException e) {
			System.out.println("Failure to fetch element in R mode");
		}
	}


	void fetchElementWrite(int i) {
		try {
			this.leaderStub.requestWriteLock(i, this.cConfig.getId(), this.displacedTimeRange());
            Tuple<RemoteStringArray, Integer> rsa = this.leaderStub.fetchRemoteStringArray(i, this.cConfig.getId());
            this.element = rsa.first.get(rsa.second);
			System.out.println("Success to fetch element in R/W mode");
		} catch (RemoteException e) {
			System.out.println("Failure to fetch element in R/W mode");
		}
	}

	void printElement(int i) {
        this.fetchElementRead(i);
		System.out.println(this.element);
	}

	void concatenate(String str, int i) {
		this.mutated_element = this.element + str;
		this.writeback(i);
	}

	void writeback(int i) {
		try {
            Tuple<RemoteStringArray, Integer> rsa = this.leaderStub.fetchRemoteStringArray(i, this.cConfig.getId());
            rsa.first.set(rsa.second, this.mutated_element);
            this.element = this.mutated_element;
		} catch (RemoteException e) {
			System.out.println("Failure to writeback");
		}
	}

	void releaseLock(int i) {
		try {
			this.leaderStub.releaseLock(i, this.cConfig.getId(), this.displacedTimeRange());
			this.element=null;
			this.mutated_element=null;
		} catch (RemoteException e) {
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
                return false;
            default:
                System.out.println("Command not found. Available commands are:");
                System.out.println("capacity    - Prints the capacity of the distributed string array.");
                System.out.println("print       - Reads and prints the element at the given index.");
                System.out.println("lock        - Locks the element at the given index for writing.");
                System.out.println("release     - Releases the lock for the element at the given index.");
                System.out.println("concatenate - Appends the given value to the value at the given index.");
        }
        return true;
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
			String line = input.nextLine();
            Boolean terminated = false;
			while (!terminated) {
				line = input.nextLine();
                terminated = arrClient.handleInput(line);
			}
			input.close();

        } catch(Exception e){
            e.printStackTrace();
        }

	}

}
