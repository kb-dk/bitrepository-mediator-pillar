package dk.kb.bitrepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.Callable;

@CommandLine.Command()
public class PillarMain implements Callable<Integer>{
    private static final Logger log = LoggerFactory.getLogger(PillarMain.class);

    @CommandLine.Parameters(index = "0", type = String.class, defaultValue = "src/test/resources/conf") // TODO remove defaults
    private String configPath;

    @CommandLine.Parameters(index = "1", type = String.class, defaultValue = "src/test/resources/conf/client-01.pem") // Default for now
    private String keyfilePath;

    @Override
    public Integer call() throws IOException {
        MediatorPillar pillar = MediatorComponentFactory.getInstance().createPillar(configPath, keyfilePath, "mediator");
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