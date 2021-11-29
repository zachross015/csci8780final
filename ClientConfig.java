import java.util.Properties;
import java.io.FileInputStream;

public class ClientConfig {

    private String server;
    private Integer id;

    public ClientConfig(String filename) throws Exception {
        Properties p = Util.loadPropertiesFromFile(filename);
        this.setServer(p.getProperty("client.server"));
        this.setId(Integer.parseInt(p.getProperty("client.id")));
    }


    public void setServer(String server) {
        this.server = server;
    }

    public String getServer() { 
        return this.server;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return this.id;
    }

}

