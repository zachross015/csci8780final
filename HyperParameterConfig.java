import java.util.Properties;

public class HyperParameterConfig {

    private Float probability;
    private Long gamma;
    private Long deltams;

    public HyperParameterConfig(String filename) throws Exception {
        Properties p = Util.loadPropertiesFromFile(filename);
        this.setProbability(Float.parseFloat(p.getProperty("hp.probability")));
        this.setGamma(Long.parseLong(p.getProperty("hp.gamma")));
        this.setDeltaMS(Long.parseLong(p.getProperty("hp.deltams")));
    }


    public void setProbability(Float probability) {
        this.probability = probability;
    }

    public Float getProbability() {
        return this.probability;
    }

    public void setGamma(Long gamma) {
        this.gamma = gamma;
    }

    public Long getGamma() {
        return this.gamma;
    }

    public void setDeltaMS(Long deltams) {
        this.deltams = deltams;
    }

    public Long getDeltaMS() {
        return this.deltams;
    }
}



