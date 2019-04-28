package nz.org.cacophony.birdmonitor;

import android.content.Context;
import android.support.test.rule.ActivityTestRule;
import nz.org.cacophony.birdmonitor.views.MainActivity;

import java.io.File;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.*;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Tim Hunt on 16-Mar-18.
 */

@SuppressWarnings("unused")
class RegisterPhone {

    private static Context targetContext;
    private static Prefs prefs;
    private static File recordingsFolder;
    private static File[] recordingFiles;

    public static void registerPhone(ActivityTestRule<MainActivity> mActivityTestRule) {
        setUpForRegisterPhone(mActivityTestRule);
        registerPhone();
        tearDownForRegisterPhone(mActivityTestRule);
    }

    public static void unRegisterPhone(ActivityTestRule<MainActivity> mActivityTestRule) throws InterruptedException {
        setUpForUnRegisterPhone(mActivityTestRule);
        unRegisterPhone();
        tearDownForUnRegisterPhone(mActivityTestRule);
    }

    private static void setUpForRegisterPhone(ActivityTestRule<MainActivity> mActivityTestRule) {

        targetContext = getInstrumentation().getTargetContext();
        prefs = new Prefs(targetContext);
        prefs.setInternetConnectionMode("normal");
        prefs.setIsDisabled(false);

        if (prefs.getDeviceName() == null) {
            // Welcome Dialog WILL be displayed - and SetupWizard will be running
            HelperCode.dismissWelcomeDialog();
        } else {
            // Main menu will be showing
            onView(withId(R.id.btnSetup)).perform(click());
        }

        Util.unregisterPhone(targetContext);
        Util.signOutUser(targetContext);

        nowSwipeLeft();
        nowSwipeLeft(); // takes you to Sign In screen, which should be showing that user is signed in

        // Need to sign in
        HelperCode.signInUserTimhot();
        try {
            Thread.sleep(1000); // had to put in sleep, as could not work out how to consistently get groups to display before testing code tries to choose a group
        } catch (Exception ignored) {

        }
        nowSwipeLeft(); // takes you to Groups screen

    }

    private static void setUpForUnRegisterPhone(ActivityTestRule<MainActivity> mActivityTestRule) throws InterruptedException {

        targetContext = getInstrumentation().getTargetContext();
        prefs = new Prefs(targetContext);
        prefs.setInternetConnectionMode("normal");
        prefs.setIsDisabled(false);

        if (prefs.getDeviceName() == null) {
            // Welcome Dialog WILL be displayed - and SetupWizard will be running
            HelperCode.dismissWelcomeDialog();
        } else {
            // Main menu will be showing
            onView(withId(R.id.btnSetup)).perform(click());
        }

        Util.unregisterPhone(targetContext);
        Util.signOutUser(targetContext);
        nowSwipeLeft();
        nowSwipeLeft(); // takes you to Sign In screen, which should be showing that user is signed in

        // Need to sign in
        HelperCode.signInUserTimhot();
        Thread.sleep(1000); // had to put in sleep, as could not work out how to consistently get groups to display before testing code tries to choose a group
        nowSwipeLeft(); // takes you to Groups screen

        registerPhone();

        nowSwipeLeft(); // need to go to next screen and back so that Un-register button displays
        nowSwipeRight();

    }

    private static void tearDownForRegisterPhone(ActivityTestRule<MainActivity> mActivityTestRule) {

        Util.signOutUser(targetContext);
        prefs.setIsDisabled(true);

    }

    private static void tearDownForUnRegisterPhone(ActivityTestRule<MainActivity> mActivityTestRule) {

        Util.signOutUser(targetContext);
        prefs.setIsDisabled(true);

    }


    private static void registerPhone() {

        HelperCode.registerPhone(prefs);

        boolean phoneRegistered = Util.isPhoneRegistered(targetContext);

        assertTrue(phoneRegistered);

        onView(withId(R.id.tvMessagesRegister)).check(matches(withText("Success - Your phone has been registered with the server :-)" + " Swipe to next screen.")));

    }

    private static void unRegisterPhone() {


        HelperCode.unRegisterPhone();

        boolean phoneRegistered = Util.isPhoneRegistered(targetContext);

        assertFalse(phoneRegistered);

        onView(withId(R.id.tvMessagesRegister)).check(matches(withText("Success - Device is no longer registered")));

    }


    private static void nowSwipeLeft() {
        onView(withId(R.id.SetUpWizard)).perform(swipeLeft());
    }

    private static void nowSwipeRight() {
        onView(withId(R.id.SetUpWizard)).perform(swipeRight());
    }

}
