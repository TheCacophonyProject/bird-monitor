package nz.org.cacophony.cacophonometerlite;

import android.content.Context;
import android.support.test.espresso.Espresso;
import android.support.test.rule.ActivityTestRule;
import android.util.Log;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertTrue;

/**
 * Created by Tim Hunt on 13-Apr-18.
 */

public class AirplaneModeToggleTest {
    static Context targetContext;
    static Prefs prefs;

    public static void testAirplaneModeTogglingOnRootedAndGreaterThanJellyBean(ActivityTestRule<MainActivity> mActivityTestRule) {
        setup(mActivityTestRule);

        //Check all works with RootAccess Check box Checked
        HelperCode.openSettingsActivity();

        checkRootAccessCheckBox();
        disableAirplaneMode();
        assertTrue(HelperCode.hasNetworkConnection(targetContext));
        enableAirplaneMode();
        assertTrue(HelperCode.doesNOTHaveNetworkConnection(targetContext));
        disableAirplaneMode();
        assertTrue(HelperCode.hasNetworkConnection(targetContext));

        //Check all works as expected with RootAccess Check box ***NOT*** Checked
        // Have assumed that there is still a network connection (from code above)
        unCheckRootAccessCheckBox();
        enableAirplaneMode();
        assertTrue(HelperCode.hasNetworkConnection(targetContext));
        checkRootAccessCheckBox();
        enableAirplaneMode();
       // Util.waitForNetworkConnection(targetContext, false);
        assertTrue(HelperCode.doesNOTHaveNetworkConnection(targetContext));
        unCheckRootAccessCheckBox();
        disableAirplaneMode();
        assertTrue(HelperCode.doesNOTHaveNetworkConnection(targetContext));
    }

    public static void testAirplaneModeTogglingOnNonRootedAndGreaterThanJellyBean(ActivityTestRule<MainActivity> mActivityTestRule) {

        System.out.println("testAirplaneModeTogglingOnNonRootedAndGreaterThanJellyBean started");
        Log.e("testAirplaneModeTogglingOnNonRootedAndGreaterThanJellyBean", "started");
        setup(mActivityTestRule);


        HelperCode.openSettingsActivity();

        boolean connectedToInternet = HelperCode.hasNetworkConnection(targetContext);

        if (connectedToInternet){
            Log.e("testAirplaneModeTogglingOnNonRootedAndGreaterThanJellyBean", "connectedToInternet");
            System.out.println("Running Airplane Mode is OFF Tests");
            doConnectedToInternetTests();
        }else {
            Log.e("testAirplaneModeTogglingOnNonRootedAndGreaterThanJellyBean", "NOT connectedToInternet");
            System.out.println("Running Airplane Mode is ON Tests");
            doNotConnectedToInternetTests();
        }
//        System.out.println("A Human needs to Toggle Airplane Mode");
//        // need to wait for human to physically toggle airplane mode
//        while (connectedToInternet == hasNetworkConnection()){
//            try {
//                System.out.println("A Human needs to Toggle Airplane Mode");
//                Thread.sleep(5000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//
//        if (connectedToInternet){
//            doConnectedToInternetTests();
//        }else {
//         //   doNotConnectedToInternetTests();
//            doNotConnectedToInternetTests();
//        }


    }

    static void doConnectedToInternetTests(){
        checkRootAccessCheckBox();
        enableAirplaneMode();
        assertTrue(HelperCode.hasNetworkConnection(targetContext));
        unCheckRootAccessCheckBox();
        enableAirplaneMode();
        assertTrue(HelperCode.hasNetworkConnection(targetContext));
    }

    static void doNotConnectedToInternetTests(){
        checkRootAccessCheckBox();
        disableAirplaneMode();
        assertTrue(HelperCode.doesNOTHaveNetworkConnection(targetContext));
        unCheckRootAccessCheckBox();
        disableAirplaneMode();
        assertTrue(HelperCode.doesNOTHaveNetworkConnection(targetContext));// check this one?
    }


    public static void setup(ActivityTestRule<MainActivity> mActivityTestRule){
        Espresso.registerIdlingResources((mActivityTestRule.getActivity().getToggleAirplaneModeIdlingResource()));
        targetContext = getInstrumentation().getTargetContext();
        prefs = new Prefs(targetContext);
    }

//    public static void openSettingsActivity(){
//        // Open settings
//        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
//        onView(allOf(withId(R.id.title), withText("Settings"))).perform(click());
//    }

    public static void checkRootAccessCheckBox(){
        onView(withId(R.id.cbHasRootAccess)).perform(scrollTo(), HelperCode.setChecked(false));
        onView(withId(R.id.cbHasRootAccess)).perform(scrollTo(), click());
    }

    public static void unCheckRootAccessCheckBox(){
        onView(withId(R.id.cbHasRootAccess)).perform(scrollTo(), HelperCode.setChecked(true));
        onView(withId(R.id.cbHasRootAccess)).perform(scrollTo(), click());
    }

    public static void disableAirplaneMode(){
        Util.disableFlightMode(targetContext);
    }

//    public static boolean hasNetworkConnection(){
//         Util.waitForNetworkConnection(targetContext, true);
//        return Util.isNetworkConnected(targetContext);
//    }
//
//    public static boolean doesNOTHaveNetworkConnection(){
//        Util.waitForNetworkConnection(targetContext, false);
//        return !Util.isNetworkConnected(targetContext);
//    }

    public static void enableAirplaneMode(){
        Util.enableFlightMode(targetContext);
    }







}
