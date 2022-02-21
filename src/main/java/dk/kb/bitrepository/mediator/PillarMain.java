package dk.kb.bitrepository.mediator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.util.Arrays;
import java.util.concurrent.Callable;

/**
 * The 'main' class for starting the mediator pillar through the commandline.
 */
@CommandLine.Command()
public class PillarMain implements Callable<Integer>{
    private static final Logger log = LoggerFactory.getLogger(PillarMain.class);

    @CommandLine.Parameters(index = "0", type = String.class, defaultValue = "src/test/resources/conf") // TODO remove defaults
    private String configPath;

    @CommandLine.Parameters(index = "1", type = String.class, defaultValue = "src/test/resources/conf/client-01.pem") // Default for now
    private String keyfilePath;

    /**
     * Part of the main method that instantiates the mediator pillar.
     *
     * @return exit code of the mediator pillar execution. // TODO will never exit normally so exit code makes no sense?
     */
    @Override
    public Integer call() {
        try {
            MediatorPillar pillar = MediatorComponentFactory.getInstance().createPillar(configPath, keyfilePath);
            log.info("Pillar started");
            synchronized(pillar) {
                pillar.wait(); // wait indefinitely to keep thread alive
            }
        } catch (Exception e) {
            log.error("Error starting pillar", e);
            return 1;
        }
        return 0;
    }

    /**
     * Start the commandline tool.
     *
     * @param args Commandline args.
     */
    public static void main(String... args) {
        System.out.println("Arguments passed by commandline is: " + Arrays.asList(args));
        CommandLine app = new CommandLine(new PillarMain());
        int exitCode = app.execute(args);                
        System.exit(exitCode);
    }            
}