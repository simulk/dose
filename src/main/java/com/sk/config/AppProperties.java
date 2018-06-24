package com.sk.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


/**
 * A singleton class to read <b>properties</b> from <code>properties configuration file</code>.
 *
 * Supports <code>MD5,SHA,SHA256</code> algorithms to generate strong etag.
 *
 * Use <code>strong.etag.hash.algorithm</code> to set the algorithm type.
 * Default is MD5 if <code>strong.etag.hash.algorithm</code> property not provided.
 *
 * @throws NoSuchAlgorithmException if <code>strong.etag.hash.algorithm</code> is invalid
 * */
public class AppProperties {
    private static Properties properties;
    private static AppProperties appProperties;

    private AppProperties() {
        properties = new Properties();
    }

    public static AppProperties getInstance() {
        if (appProperties == null) {
            appProperties = new AppProperties();
        }
        return appProperties;
    }

    public void load(String appPropsPath) throws IOException {
        if (null == appPropsPath || appPropsPath.isEmpty()) {
            throw new IllegalArgumentException("Path to configuration file is null or empty");
        }

        InputStream appPropsFileStream = AppProperties.class.getClassLoader().getResourceAsStream(appPropsPath);
        if (null == appPropsFileStream) {
            throw new IllegalArgumentException("Unable to find configuration file");
        }
        properties.load(appPropsFileStream);
    }

    public static <T> T getProperty(String key, Class<T> clazz) {
        return getProperty(key, null, clazz);
    }

    public static <T> T getProperty(String key, T defaultValue, Class<T> clazz) {
        T property = clazz.cast(properties.getProperty(key));

        return null == property ? defaultValue : property;
    }

    public static Boolean getBoolean(String key, String defaultValue) {
        return Boolean.valueOf(getProperty(key, defaultValue, String.class));
    }
}
