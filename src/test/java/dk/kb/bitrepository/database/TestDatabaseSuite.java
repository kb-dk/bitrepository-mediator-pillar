package dk.kb.bitrepository.database;

import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.junit.runners.Suite.SuiteClasses;

@SuiteClasses({
        TestDatabaseCalls.class
})
@Suite
@SuiteDisplayName("Test Database Suite")
public class TestDatabaseSuite {

}
