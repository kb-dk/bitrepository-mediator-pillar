package dk.kb.bitrepository.mediator;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SelectPackages({
        "dk.kb.bitrepository.mediator.communication",
        "dk.kb.bitrepository.mediator.crypto",
        "dk.kb.bitrepository.mediator.database",
        "dk.kb.bitrepository.mediator.utils"
})
@SuiteDisplayName("Test Suite")
public class TestSuite {
}

