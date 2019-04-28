package nz.org.cacophony.birdmonitor;

import android.content.Context;
import android.support.test.rule.ActivityTestRule;
import android.util.Log;
import nz.org.cacophony.birdmonitor.views.MainActivity;

import java.util.Date;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.*;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Created by Tim Hunt on 16-Mar-18.
 */

@SuppressWarnings("unused")
class CreateAccount {

    public static void createAccount(ActivityTestRule<MainActivity> mActivityTestRule) {

        setUpForCreateAccount(mActivityTestRule);

        create();

    }

    private static void setUpForCreateAccount(ActivityTestRule<MainActivity> mActivityTestRule) {

        Context targetContext = getInstrumentation().getTargetContext();
        Prefs prefs = new Prefs(targetContext);
        prefs.setInternetConnectionMode("normal");


        if (prefs.getDeviceName() == null) {
            // Welcome Dialog WILL be displayed - and SetupWizard will be running
            HelperCode.dismissWelcomeDialog();
        } else {
            // Main menu will be showing
            onView(withId(R.id.btnSetup)).perform(click());
        }

        HelperCode.signOutUser(prefs);
        HelperCode.useTestServerAndShortRecordings();
        nowSwipeLeft();// takes you to Create Account screen
    }


    private static void create() {

        try {
            // Create a uniqueish username
            Date now = new Date();
            String deviceName = Long.toString(now.getTime() / 1000);
            deviceName.substring(deviceName.length() - 8);
            onView(withId(R.id.etUsername)).perform(replaceText(deviceName), closeSoftKeyboard());
            onView(withId(R.id.etEmail)).perform(replaceText(deviceName + "@gmail.com"), closeSoftKeyboard());
            onView(withId(R.id.etPassword1)).perform(replaceText("Pppother1"), closeSoftKeyboard());
            onView(withId(R.id.etPassword2)).perform(replaceText("Pppother1"), closeSoftKeyboard());

            onView(withId(R.id.btnSignUp)).perform(click());

        } catch (Exception ex) {
            Log.e("CreateAccount", ex.getLocalizedMessage());
        }

        Log.e("CreateAccount", "Finished");

        String successMessage = "Success, you have successfully created a new user account\n\nSwipe to next screen to sign in.";
        onView(withId(R.id.tvMessagesCreateAccount)).check(matches(withText(successMessage)));
    }

    private static void nowSwipeLeft() {
        onView(withId(R.id.SetUpWizard)).perform(swipeLeft());
    }
}
