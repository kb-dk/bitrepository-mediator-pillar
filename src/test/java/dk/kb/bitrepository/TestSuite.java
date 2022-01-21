package dk.kb.bitrepository;

import dk.kb.bitrepository.database.TestDatabaseSuite;
import dk.kb.bitrepository.mediator.TestMediatorSuite;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.junit.runners.Suite.SuiteClasses;

@SuiteClasses({
        TestDatabaseSuite.class,
        TestMediatorSuite.class,
})
@Suite
@SuiteDisplayName("Test Suite")
public class TestSuite {

}
