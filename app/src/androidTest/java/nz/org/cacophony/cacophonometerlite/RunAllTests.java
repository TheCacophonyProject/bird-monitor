package nz.org.cacophony.cacophonometerlite;

/**
 * Created by Tim Hunt on 14-Mar-18.
 */



import org.junit.runner.RunWith;
import org.junit.runners.Suite;

// Runs all unit tests.
@RunWith(Suite.class)
@Suite.SuiteClasses({

        RunBasicGUITests.class,
        RunRegisterDeviceOnTestServerTests.class


})
public class RunAllTests {


}
