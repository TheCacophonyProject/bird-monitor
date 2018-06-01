package nz.org.cacophony.cacophonometer;

import android.content.Context;
import android.support.test.rule.ActivityTestRule;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static org.junit.Assert.assertTrue;

/**
 * Created by Tim Hunt on 13-Apr-18.
 */

class AirplaneModeToggleTest {
    private static Context targetContext;

    public static void disableAirplaneMode(ActivityTestRule<MainActivity> mActivityTestRule) {
        // Use this to recover AirDroid connection if Airplane has been enabled
        setup(mActivityTestRule);
        HelperCode.openSettingsActivity();
        HelperCode.disableAirplaneMode(targetContext);
        HelperCode.returnToMainActivityScreen();
    }

    public static void testAirplaneModeTogglingOnRootedAndGreaterThanJellyBean(ActivityTestRule<MainActivity> mActivityTestRule) {
        setup(mActivityTestRule);

        //Don't know what state settings are in, so first enable required buttons and check can get network access (will repeat this after enabling disabling airplane mode)
        // First disable airplane mode
        HelperCode.openSettingsActivity();

        HelperCode.disableAirplaneMode(targetContext);
        assertTrue(HelperCode.hasNetworkConnection(targetContext));

        // Now enable airplane mode and check there is no network access
        HelperCode.unCheckOnLineModeCheckBox();
        enableAirplaneMode();
        assertTrue(HelperCode.doesNOTHaveNetworkConnection(targetContext));

        // Now disable airplane mode and check network access comes back on
        HelperCode.checkOnLineModeCheckBox();
        disableAirplaneMode();
        assertTrue(HelperCode.hasNetworkConnection(targetContext));

        HelperCode.returnToMainActivityScreen();

    }

    public static void testAirplaneModeTogglingOnNonRootedAndGreaterThanJellyBean(ActivityTestRule<MainActivity> mActivityTestRule) {

    // The point of this test is that if the phone hasn't been rooted, then enabling or disabling airplane mode will not change the state of the network connection.

        setup(mActivityTestRule);
        HelperCode.openSettingsActivity();

        boolean connectedToInternet = HelperCode.hasNetworkConnection(targetContext);

        if (connectedToInternet){
            connectedToInternetTest();
        }else {
            notConnectedToInternetTest();
        }
        HelperCode.returnToMainActivityScreen();
    }

    private static void connectedToInternetTest() {
        HelperCode.checkRootAccessCheckBox(); // But this should not allow toggling of airplane mode - will give an error to the user when try to toggle airplane mode
        HelperCode.unCheckOnLineModeCheckBox();
        enableAirplaneMode();
        assertTrue(HelperCode.hasNetworkConnection(targetContext)); //Should still be connected
    }

    private static void notConnectedToInternetTest() {
        HelperCode.checkRootAccessCheckBox(); // But this should not allow toggling of airplane mode - will give an error to the user when try to toggle airplane mode
        HelperCode.checkOnLineModeCheckBox();
        disableAirplaneMode();
        assertTrue(HelperCode.doesNOTHaveNetworkConnection(targetContext)); //Should still NOT be connected
    }

    private static void setup(ActivityTestRule<MainActivity> mActivityTestRule){

        targetContext = getInstrumentation().getTargetContext();
        Prefs prefs = new Prefs(targetContext);
    }

    private static void disableAirplaneMode(){
        Util.disableFlightMode(targetContext);
    }

    private static void enableAirplaneMode(){
        Util.enableFlightMode(targetContext);
    }
}
