package nz.org.cacophony.cacophonometerlite;



import org.junit.runner.RunWith;
import org.junit.runners.Suite;

// Runs all unit tests.
@RunWith(Suite.class)
@Suite.SuiteClasses({

        RegisterWithTestServer.class,
        RecordNowTest1.class,
        RecordNowTest2.class


})
public class RunRecordNowTests {

}