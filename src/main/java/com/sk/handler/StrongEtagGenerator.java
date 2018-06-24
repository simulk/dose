package com.sk.handler;

import com.sk.config.AppProperties;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * A singleton class to generate <b>strong etag</b>.
 *
 * Supports <code>MD5,SHA,SHA256</code> algorithms to generate strong etag.
 *
 * Use <code>strong.etag.hash.algorithm</code> to set the algorithm type.
 * Default is MD5 if <code>strong.etag.hash.algorithm</code> property not provided.
 *
 * @throws NoSuchAlgorithmException if <code>strong.etag.hash.algorithm</code> is invalid
 * */
public class StrongEtagGenerator implements EtagGenerator {
    private static StrongEtagGenerator etagGenerator;
    private MessageDigest messageDigest;

    private StrongEtagGenerator() throws NoSuchAlgorithmException {
        String hashAlgorithm = AppProperties.getProperty("strong.etag.hash.algorithm", "MD5", String.class);
        messageDigest = MessageDigest.getInstance(hashAlgorithm.toUpperCase());
    }

    public static EtagGenerator getInstance() throws NoSuchAlgorithmException {
        if (etagGenerator == null) {
            etagGenerator = new StrongEtagGenerator();
        }
        return etagGenerator;
    }

    /**
     * Strong etag implementation.
     * It will create hash of {@link java.io.File}
     * Default hash algorithm is <b>MD5</b>. Other supported algorithms are <b>SHA</b> and <b>SHA256</b>
     *
     * @param   file    file to use to generate strong etag
     * @return          strong etag
     */
    @Override
    public String generateEtag(File file) throws IOException {
        messageDigest.update(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
        byte[] digest = messageDigest.digest();
        return String.format("\"%s\"", DatatypeConverter.printHexBinary(digest));
    }

}
