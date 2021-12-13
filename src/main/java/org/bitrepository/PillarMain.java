package org.bitrepository;

import java.util.Arrays;
import java.util.concurrent.Callable;


import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.SettingsProvider;
import org.bitrepository.common.settings.XMLFileSettingsLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.kb.util.yaml.YAML;
import picocli.CommandLine;

@CommandLine.Command()
public class PillarMain implements Callable<Integer>{
        
    private static final Logger log = LoggerFactory.getLogger(PillarMain.class);

    @CommandLine.Parameters(index = "0", type = String.class, defaultValue = "src/test/resources/conf") // TODO remove defaults
    private String configPath;

    @CommandLine.Parameters(index = "1", type = String.class, defaultValue = "src/test/resources/conf/client-01.pem") // Default for now
    private String keyfilePath;

    @Override
    public Integer call() throws Exception {
        YAML intermediatorConfig = new YAML(configPath + "/intermediatorConfig.yaml"); // TODO probably move
        SettingsProvider settingsProvider = new SettingsProvider(new XMLFileSettingsLoader(configPath), null);
        Settings settings = settingsProvider.getSettings();
        Configuration configuration = new Configuration(intermediatorConfig, settings);

        IntermediatorPillar pillar = new IntermediatorPillar(configuration);
        pillar.start();
        return 0;
    }
    
    
    public static void main(String... args) {
        System.out.println("Arguments passed by commandline is: " + Arrays.asList(args));
        CommandLine app = new CommandLine(new PillarMain());
        int exitCode = app.execute(args);                
        System.exit(exitCode);
    }            
}