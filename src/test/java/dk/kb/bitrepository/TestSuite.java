package dk.kb.bitrepository;

import dk.kb.bitrepository.database.TestDatabaseSuite;
import dk.kb.bitrepository.mediator.TestMediatorSuite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        TestDatabaseSuite.class,
        TestMediatorSuite.class,
})
public class TestSuite {

}
