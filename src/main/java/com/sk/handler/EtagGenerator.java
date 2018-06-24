package com.sk.handler;

import java.io.File;
import java.io.IOException;

public interface EtagGenerator {
    String generateEtag(File file) throws IOException;
}
