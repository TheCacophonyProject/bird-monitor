package nz.org.cacophony.cacophonometer;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Created by Tim Hunt on 14-Mar-18.
 */


// Runs all unit tests.
@RunWith(Suite.class)
@Suite.SuiteClasses({

        RegisterWithProductionServer.class,
        CheckVitalsProductionServer.class



})
public class RunRegisterDeviceOnProductionServerTests {


}