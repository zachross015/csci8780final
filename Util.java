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

    // https://stackoverflow.com/questions/23561551/a-efficient-binomial-random-number-generator-code-in-java
    public static int bernoulli(double p) {
        double log_q = Math.log(1.0 - p);
        if(Math.log(Math.random()) < log_q) {
            return 1;
        } else {
            return 0;
        }
    }


}
