package com.sk;

import com.sk.config.AppProperties;
import com.sk.server.DoseServer;


/**
 * Starting point for <bold>DOSE server</bold>.
 *
 * It will initialize {@link AppProperties} and load the <code>configuration properties file</code>.
 *
 * Set <code>dose.config.location</code> to change the <code>configuration.properties</code>
 * to explicitly set the file location. Otherwise, it will try to read from classpath.
 *
 * */
public class Bootstrap {

    public static void main(String[] args) throws Exception {
        String appPropsPath = "application.properties";

        if (null != System.getProperty("dose.config.location")) {
            appPropsPath = System.getProperty("dose.config.location");
        }

        if (null == appPropsPath) {
            throw new IllegalStateException("Unable to start server. Application configuration file is missing.");
        }
        AppProperties.getInstance().load(appPropsPath);
        new DoseServer().start();
    }
}
