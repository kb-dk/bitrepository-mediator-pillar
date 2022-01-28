package dk.kb.bitrepository;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SelectPackages({
        "dk.kb.bitrepository.crypto",
        "dk.kb.bitrepository.database",
        "dk.kb.bitrepository.mediator"
})
@SuiteDisplayName("Test Suite")
public class TestSuite {
}

