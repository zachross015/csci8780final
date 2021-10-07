import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry; 
import java.rmi.registry.Registry;  
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

public class StringArrayClient {
	static RemoteStringArray stub = null;
	private int client_id=-1;
	private String element=null;

	public StringArrayClient(int client_id) {this.client_id=client_id;}
	
	void getArrayCapacity() {
		try {
			System.out.println(stub.getCapacity());
		} catch (RemoteException e) {
			System.out.println("Failure to get array capacity");			
			e.printStackTrace();
		}
	}
	
	void fetchElementRead(int i){
		try {
			if(!stub.requestReadLock(i, this.client_id)) {
				System.out.println("Failure to obtain read lock from server");
				return;
			}
			
			this.element=stub.fetchElementRead(i, this.client_id);
			System.out.println("Success to fetch element in R mode");
		} catch (RemoteException e) {
			e.printStackTrace();
			System.out.println("Failure to fetch element in R mode");
		}
	}
	
	void fetchElementWrite(int i){
			try {
				stub.requestWriteLock(i, this.client_id);
				this.element=stub.fetchElementWrite(i, this.client_id);
				System.out.println("Success in fetching element in R/W mode");
			} catch (RemoteException e) {
				e.printStackTrace();
				System.out.println("Failure to fetch element in R/W mode");
			}
	}
	
	void printElement(int i){
        System.out.println(this.element);
	}
	
	void concatenate(String str, int i) {
		this.element=this.element+str;
		this.writeback(i);
	}
	
	void writeback(int i){
		try {
			stub.writeBackElement(element, i, this.client_id);
		} catch (RemoteException e) {
			e.printStackTrace();
			System.out.println("Failure to writeback");
		}
	}
	
	void releaseLock(int i){
        try {
			stub.releaseLock(i, this.client_id);
		} catch (RemoteException e) {
			System.out.println("Failed to release lock -- check server connection");
			e.printStackTrace();
		}
	}

   public static void main(String[] args) {  

	   try {  
		 File file = new File(args[0]);
		 Scanner scan;
		 scan = new Scanner(file);
		 String name= scan.nextLine();
		 System.out.println(name);
		 //String host= scan.next();
		 int _client_id=Integer.parseInt(scan.nextLine());
		 System.out.println(_client_id);
		 scan.close();
    	 StringArrayClient client = new StringArrayClient(_client_id);
         
    	 // Getting the registry 
         Registry registry = LocateRegistry.getRegistry(null); 
    
         // Looking up the registry for the remote object 
         stub = (RemoteStringArray) registry.lookup(name); 
    
         // Small suite of debug tests
         int index=0;
         client.getArrayCapacity();
         client.fetchElementRead(index);
         client.printElement(index);
         client.releaseLock(index);

         client.fetchElementWrite(index);
         client.concatenate("test", index);
         client.releaseLock(index);

         client.fetchElementRead(index);
         client.printElement(index);
         client.releaseLock(index);

         client.fetchElementWrite(index+1);
         client.element="Second test";
         client.writeback(index+1);
         client.releaseLock(index+1);

         client.fetchElementRead(index+1);
         client.printElement(index+1);
         client.releaseLock(index+1);

         client.getArrayCapacity();
         
         //stub.insertArrayElement(9, "test");
         //System.out.println(stub.fetchElementRead(9,1));

         System.out.println("Remote method invoked"); 
      } catch (Exception e) {
         System.err.println("Client exception: " + e.toString()); 
         e.printStackTrace(); 
      } 
   } 
	
	
}

