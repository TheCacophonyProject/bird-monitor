package nz.org.cacophony.cacophonometerlite;

import android.content.Context;
import android.support.test.rule.ActivityTestRule;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static org.junit.Assert.assertTrue;

/**
 * Created by Tim Hunt on 13-Apr-18.
 */

public class AirplaneModeToggleTest {
    static Context targetContext;
    static Prefs prefs;

    public static void disableAirplaneMode(ActivityTestRule<MainActivity> mActivityTestRule) {
        // Use this to recover AirDroid conncection in Airplane has been enabled
        setup(mActivityTestRule);
        HelperCode.openSettingsActivity();
        HelperCode.checkRootAccessCheckBox();
        HelperCode.checkOnLineModeCheckBox();
        disableAirplaneMode();
        tearDown(mActivityTestRule);
    }

    public static void testAirplaneModeTogglingOnRootedAndGreaterThanJellyBean(ActivityTestRule<MainActivity> mActivityTestRule) {
        setup(mActivityTestRule);

        //Don't know what state settings are in, so first enable required buttons and check can get network access (will repeat this after enabling disabling airplane mode)
        // First disable airplane mode
        HelperCode.openSettingsActivity();
        HelperCode.checkRootAccessCheckBox();
        HelperCode.checkOnLineModeCheckBox();
        disableAirplaneMode();
        assertTrue(HelperCode.hasNetworkConnection(targetContext));

        // Now enable airplane mode and check there is no network access
        HelperCode.unCheckOnLineModeCheckBox();
        enableAirplaneMode();
        assertTrue(HelperCode.doesNOTHaveNetworkConnection(targetContext));

        // Now disable airplane mode and check network access comes back on
        HelperCode.checkOnLineModeCheckBox();
        disableAirplaneMode();
        assertTrue(HelperCode.hasNetworkConnection(targetContext));


        tearDown(mActivityTestRule);
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

        tearDown(mActivityTestRule);


    }

    static void connectedToInternetTest() {
        HelperCode.checkRootAccessCheckBox(); // But this should not allow toggling of airplane mode - will give an error to the user when try to toggle airplane mode
        HelperCode.unCheckOnLineModeCheckBox();
        enableAirplaneMode();
        assertTrue(HelperCode.hasNetworkConnection(targetContext)); //Should still be connected

    }

    static void notConnectedToInternetTest() {
        HelperCode.checkRootAccessCheckBox(); // But this should not allow toggling of airplane mode - will give an error to the user when try to toggle airplane mode
        HelperCode.checkOnLineModeCheckBox();
        disableAirplaneMode();
        assertTrue(HelperCode.doesNOTHaveNetworkConnection(targetContext)); //Should still NOT be connected
    }



    public static void setup(ActivityTestRule<MainActivity> mActivityTestRule){

        mActivityTestRule.getActivity().registerEspressoIdlingResources();
        targetContext = getInstrumentation().getTargetContext();
        prefs = new Prefs(targetContext);
    }

    public static void tearDown(ActivityTestRule<MainActivity> mActivityTestRule){
        // Try to disable airplane mode if possible
        HelperCode.checkRootAccessCheckBox();
        HelperCode.checkOnLineModeCheckBox();
        disableAirplaneMode();
        mActivityTestRule.getActivity().unRegisterEspressoIdlingResources();

    }






    public static void disableAirplaneMode(){

        Util.disableFlightMode(targetContext);
    }



    public static void enableAirplaneMode(){
        Util.enableFlightMode(targetContext);
    }







}
