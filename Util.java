import java.util.Properties;
import java.io.FileInputStream;
import java.util.stream.LongStream;

public class Util {

    public static Properties loadPropertiesFromFile(String filename) throws Exception {
        Properties p = new Properties();
        FileInputStream fis = new FileInputStream(filename);
        p.load(fis);
        return p;
    }

}
