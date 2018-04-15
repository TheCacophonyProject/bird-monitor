package nz.org.cacophony.cacophonometerlite;

/**
 * Created by Tim Hunt on 14-Mar-18.
 */



import org.junit.runner.RunWith;
import org.junit.runners.Suite;

// Runs all unit tests.
@RunWith(Suite.class)
@Suite.SuiteClasses({

        RunAirplaneFlightModeToggleTests.class,
        RunBasicGUITests.class,
        RunSettingsOptions.class,
        RunModeTestsInSpecifiedOrder.class,
      //  RunRegisterDeviceOnProductionServerTests.class,
        RunRegisterDeviceOnTestServerTests.class,
        RunRecordNowTests.class,

})
public class RunAllTests {


}
