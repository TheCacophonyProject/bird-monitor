package nz.org.cacophony.birdmonitor;

import android.content.Context;
import android.support.test.rule.ActivityTestRule;
import nz.org.cacophony.birdmonitor.views.MainActivity;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertTrue;

/**
 * Created by Tim Hunt on 16-Mar-18.
 */

@SuppressWarnings("unused")
class SignInUser {

    private static Prefs prefs;

    public static void signInUser(ActivityTestRule<MainActivity> mActivityTestRule) {

        setUpForSignInUser(mActivityTestRule);

        signIn();

        tearDownForSignInUser(mActivityTestRule);
    }

    private static void setUpForSignInUser(ActivityTestRule<MainActivity> mActivityTestRule) {

        Context targetContext = getInstrumentation().getTargetContext();
        prefs = new Prefs(targetContext);
        prefs.setInternetConnectionMode("normal");
        prefs.setUserSignedIn(false);

        if (prefs.getDeviceName() == null) {
            // Welcome Dialog WILL be displayed - and SetupWizard will be running
            HelperCode.dismissWelcomeDialog();
        } else {
            // Main menu will be showing

            onView(withId(R.id.btnSetup)).perform(click());
        }

        HelperCode.useTestServerAndShortRecordings();

        nowSwipeLeft();
        nowSwipeLeft(); // takes you to Sign In screen

    }

    private static void tearDownForSignInUser(ActivityTestRule<MainActivity> mActivityTestRule) {

    }


    private static void signIn() {

        HelperCode.signInUserTimhot();

        boolean userSignedIn = prefs.getUserSignedIn();

        assertTrue(userSignedIn);
        onView(withId(R.id.tvTitleMessageSignIn)).check(matches(withText("Signed In")));

    }

    private static void nowSwipeLeft() {

        onView(withId(R.id.SetUpWizard)).perform(swipeLeft());

    }
}
