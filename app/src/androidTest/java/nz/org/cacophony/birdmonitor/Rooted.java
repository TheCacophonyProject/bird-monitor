package nz.org.cacophony.birdmonitor;

import android.content.Context;
import android.support.test.rule.ActivityTestRule;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static org.junit.Assert.assertTrue;

/**
 * Created by Tim Hunt on 16-Mar-18.
 */

@SuppressWarnings("unused")
class Rooted {

    private static Context targetContext;
    private static Prefs prefs;


    public static void rooted(ActivityTestRule<MainActivity> mActivityTestRule) {
        setUpForRooted(mActivityTestRule);

        rooted();

        tearDownForRooted(mActivityTestRule);
    }



    private static void setUpForRooted(ActivityTestRule<MainActivity> mActivityTestRule){

        targetContext = getInstrumentation().getTargetContext();
        prefs = new Prefs(targetContext);
        prefs.setInternetConnectionMode("normal");
        prefs.setIsDisabled(true); // stops recording upsetting test

        if (prefs.getDeviceName() == null){
            // Welcome Dialog WILL be displayed - and SetupWizard will be running
            HelperCode.dismissWelcomeDialog();
        }

    }



    private static void tearDownForRooted(ActivityTestRule<MainActivity> mActivityTestRule) {
        // Leave with network access

        // Turn off airplane mode
        Util.disableFlightMode(targetContext);

        // Now check we have a network connection again
       boolean hasNetworkConnection = Util.waitForNetworkConnection(targetContext, true);
        assertTrue(hasNetworkConnection);

        // To prevent other tests failing due to phone going into airplane mode, change hasRoot to no
        prefs.setHasRootAccess(false);

        prefs.setIsDisabled(true);

    }


    private static void rooted(){
        // idlingresource is not a suitable way of waiting for network connection in these tests,
        // because the test doesn't actually interact with the GUI which is what idlingresource is
        // supposed to be used with.
        // Code already had the method waitForNetworkConnection which proved ideal here.

        // assume phone has been rooted
        prefs.setHasRootAccess(true);

        //First check have we have a network connection
        boolean hasNetworkConnection = Util.isNetworkConnected(targetContext);

        // order of tests depends on whether there is a network connection when test starts

        if (hasNetworkConnection){

            // Turn on airplane mode
            Util.enableFlightMode(targetContext);

            // Now check we do NOT have a network connection
            boolean doesNotHaveNetworkConnection = Util.waitForNetworkConnection(targetContext, false);
            assertTrue(doesNotHaveNetworkConnection);

            // Turn off airplane mode
            Util.disableFlightMode(targetContext);

            // Now check we have a network connection again
            hasNetworkConnection = Util.waitForNetworkConnection(targetContext, true);
            assertTrue(hasNetworkConnection);

        }else{ // did not have a network connection, so airplane mode is probably on

            // Turn off airplane mode
            Util.disableFlightMode(targetContext);

            // Now check we have a network connection
            hasNetworkConnection = Util.waitForNetworkConnection(targetContext, true);
            assertTrue(hasNetworkConnection);

            // Now check we can turn on airplane mode
            Util.enableFlightMode(targetContext);
            // Now check we do NOT have a network connection
            boolean doesNotHaveNetworkConnection = Util.waitForNetworkConnection(targetContext, false);
            assertTrue(doesNotHaveNetworkConnection);

        }
    }
}
