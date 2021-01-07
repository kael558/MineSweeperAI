package main;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;

public class GetConfig {
    Properties prop;

    private static GetConfig instance;

    public static GetConfig getInstance(){
        if (instance == null)
            instance = new GetConfig();
        return instance;
    }

    public GetConfig()  {
        String propFileName = "config.properties";
        prop = new Properties();
        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
            if (inputStream != null) {
                prop.load(inputStream);
            } else {
                throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
            }
            inputStream.close();
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }
    }

    public String getPropertyAsString(String property){
        return prop.getProperty(property);
    }

    public int getPropertyAsInt(String property){
        return Integer.parseInt(prop.getProperty(property));
    }

    public double getPropertyAsDouble(String property){
        return Double.parseDouble(prop.getProperty(property));
    }
}
