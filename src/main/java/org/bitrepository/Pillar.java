package org.bitrepository;

import dk.kb.util.yaml.YAML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Pillar {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private YAML config;

    public Pillar(YAML config) {
        log.debug("Running constructor HelloWorld()");
        this.config = config;
    }

    public void start() {
        System.out.println(config.getString("crypto.encryptionAlgo"));
    }
}