package nz.org.cacophony.cacophonometerlite;



import org.junit.runner.RunWith;
import org.junit.runners.Suite;

// Runs all unit tests.
@RunWith(Suite.class)
@Suite.SuiteClasses({

        RegisterWithTestServer.class,
        RunRecordNowTest1.class,
        RunRecordNowTest2.class


})
public class RunRecordNowTests {

}