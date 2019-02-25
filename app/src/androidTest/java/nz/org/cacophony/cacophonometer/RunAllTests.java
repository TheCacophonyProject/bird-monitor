package nz.org.cacophony.cacophonometer;

/**
 * Created by Tim Hunt on 14-Mar-18.
 */



import org.junit.runner.RunWith;
import org.junit.runners.Suite;

// Runs all unit tests.
@RunWith(Suite.class)
@Suite.SuiteClasses({
//        DisableAirplaneMode.class,
//        RunAirplaneFlightModeToggleTests.class,
//        DisableAirplaneMode.class,
//        RunBasicGUITests.class,
//        RunSettingsOptions.class,
//        RunModeTestsInSpecifiedOrder.class,
//        //  RunRegisterDeviceOnProductionServerTests.class,
//        RunRegisterDeviceOnTestServerTests.class,
        RunCreateAccount.class,
        RunSignInUser.class,
        RunRegisterPhone.class,
        RunRecord.class,

      // RunRecordNowTests.class,

})
public class RunAllTests {


}