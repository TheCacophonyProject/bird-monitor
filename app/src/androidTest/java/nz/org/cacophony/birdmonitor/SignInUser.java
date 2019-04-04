package nz.org.cacophony.birdmonitor;

import android.content.Context;
import android.support.test.rule.ActivityTestRule;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static org.junit.Assert.assertEquals;

/**
 * Created by Tim Hunt on 16-Mar-18.
 */

@SuppressWarnings("unused")
class SignInUser {

    private static Context targetContext;
    private static Prefs prefs;

    public static void signInUser(ActivityTestRule<MainActivity> mActivityTestRule) {

        setUpForSignInUser(mActivityTestRule);

        signIn();

        tearDownForSignInUser(mActivityTestRule);
    }

    private static void setUpForSignInUser(ActivityTestRule<MainActivity> mActivityTestRule){

        targetContext = getInstrumentation().getTargetContext();
        prefs = new Prefs(targetContext);
        prefs.setInternetConnectionMode("normal");
        prefs.setUserSignedIn(false);

        if (prefs.getDeviceName() == null){
            // Welcome Dialog WILL be displayed - and SetupWizard will be running
            HelperCode.dismissWelcomeDialog();
        }else{
            // Main menu will be showing

            onView(withId(R.id.btnSetup)).perform(click());
        }

        HelperCode.useTestServerAndShortRecordings(prefs, targetContext);

        nowSwipeLeft();
        nowSwipeLeft(); // takes you to Sign In screen

    }

    private static void tearDownForSignInUser(ActivityTestRule<MainActivity> mActivityTestRule) {

    }


    private static void signIn(){

        HelperCode.signInUserTimhot(prefs);

        boolean userSignedIn = prefs.getUserSignedIn();

        assertEquals(userSignedIn, true);
        onView(withId(R.id.tvTitleMessageSignIn)).check(matches(withText("Signed In")));

    }

    private static void nowSwipeLeft(){

        onView(withId(R.id.SetUpWizard)).perform(swipeLeft());

    }
}
