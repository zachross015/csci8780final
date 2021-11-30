import java.util.Properties;
import java.util.List;
import java.io.FileInputStream;

public class LeaderConfig {

    private String name;
    private Integer port;

    public LeaderConfig(String filename) throws Exception {
        Properties p = Util.loadPropertiesFromFile(filename);
        this.setName(p.getProperty("leader.name"));
        this.setPort(Integer.parseInt(p.getProperty("leader.port")));

        System.out.println("Leader Configuration Initialized");
        System.out.println("Name: " + this.getName());
        System.out.println("Port: " + this.getPort());
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() { 
        return this.name;
    }
    
    public void setPort(Integer port) {
        this.port = port;
    }

    public Integer getPort() {
        return this.port;
    }

}

