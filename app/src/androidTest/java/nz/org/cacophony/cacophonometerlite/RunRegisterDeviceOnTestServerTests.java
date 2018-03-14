package nz.org.cacophony.cacophonometerlite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Created by Tim Hunt on 14-Mar-18.
 */


// Runs all unit tests.
@RunWith(Suite.class)
@Suite.SuiteClasses({

        RegisterWithTestServer.class,
        CheckVitalsTestServer.class

})
public class RunRegisterDeviceOnTestServerTests {


}