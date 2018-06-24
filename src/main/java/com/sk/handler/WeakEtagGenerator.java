package com.sk.handler;

import java.io.File;
import java.util.Base64;


/**
 * A singleton class to generate <b>weak etag</b>.
 * */
public class WeakEtagGenerator implements EtagGenerator {
    private static WeakEtagGenerator etagGenerator;

    private WeakEtagGenerator() {}

    public static EtagGenerator getInstance() {
        if (etagGenerator == null) {
            etagGenerator = new WeakEtagGenerator();
        }
        return etagGenerator;
    }

    /**
     * Weak etag implementation.
     * This is the default etag generator
     * It uses name, length and lastModified properties of {@link java.io.File}
     * to generate a weak etag. Etag will have <b><i>W/</i></b> prefix
     *
     * @param   file    file to use to generate weak etag
     * @return          weak etag
    */
    @Override
    public String generateEtag(File file) {
        StringBuilder sb = new StringBuilder(32);
        sb.append("W/\"");

        String name = file.getName();
        int length = name.length();
        long lhash = 0;
        for (int i = 0; i < length; i++)
            lhash = 31*lhash+name.charAt(i);

        encode(file.lastModified() ^ lhash, sb);
        encode(file.length() ^ lhash, sb);

        sb.append('"');
        return sb.toString();
    }

    private void encode(Long value, StringBuilder sb) {
        byte[] encode = Base64.getEncoder().encode(String.valueOf(value).getBytes());
        sb.append(encode);
    }
}
