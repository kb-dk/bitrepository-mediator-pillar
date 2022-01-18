package dk.kb.bitrepository.mediator;

import dk.kb.bitrepository.database.TestDatabaseCalls;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        TestMessageReceivedHandler.class
})
public class TestMediatorSuite {

}
