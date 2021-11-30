import java.util.Properties;
import java.util.List;
import java.util.Arrays;
import java.io.FileInputStream;

public class ServerConfig {

    private String name;
    private Integer capacity;
    private List<String> elements;
    private Integer port;
    private String leader;

    public ServerConfig(String filename) throws Exception {
        Properties p = Util.loadPropertiesFromFile(filename);
        this.setName(p.getProperty("server.name"));
        this.setCapacity(Integer.parseInt(p.getProperty("server.capacity")));
        this.setElements(Arrays.asList(p.getProperty("server.elements").split(" ")));
        this.setPort(Integer.parseInt(p.getProperty("server.port")));
        this.setLeader(p.getProperty("server.leader"));

        System.out.println("Server Configuration Initialized");
        System.out.println("Name: " + this.getName());
        System.out.println("Capacity: " + this.getCapacity());
        System.out.println("Elements: " + Arrays.toString(elements.toArray()));
        System.out.println("Port: " + this.getPort());
        System.out.println("Leader: " + this.getLeader());
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() { 
        return this.name;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity; 
    }

    public Integer getCapacity() {
        return this.capacity;
    }

    public void setElements(List<String> elements) {
        this.elements = elements;
    }

    public List<String> getElements() {
        return this.elements;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Integer getPort() {
        return this.port;
    }

    public void setLeader(String leader) {
        this.leader = leader;
    }

    public String getLeader() {
        return this.leader;
    }

}

