package org.bitrepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Pillar {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private Configuration config;

    public Pillar(Configuration config) {
        log.debug("Running constructor HelloWorld()");
        this.config = config;
    }

    public void start() {
        System.out.println(config.getCryptoAlgorithm());
    }
}