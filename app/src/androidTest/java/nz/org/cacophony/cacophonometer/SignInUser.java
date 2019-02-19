package nz.org.cacophony.cacophonometer;

import android.content.Context;
import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.io.File;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Tim Hunt on 16-Mar-18.
 */

@SuppressWarnings("unused")
class SignInUser {

    private static Context targetContext;
    private static Prefs prefs;
    private static File recordingsFolder;
    private static File[] recordingFiles;

//    @Rule
//    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);



    public static void signInUser(ActivityTestRule<MainActivity> mActivityTestRule) {
        // This test presses the Record Now button and checks that a recording has been saved on the phone

        // Set settings to do a short recording and DO not upload to server - can then see if a new recording file is saved on phone

        setUpForSignInUser(mActivityTestRule);

        signIn();

        tearDownForSignInUser(mActivityTestRule);
    }

    private static void setUpForSignInUser(ActivityTestRule<MainActivity> mActivityTestRule){

        mActivityTestRule.getActivity().registerEspressoIdlingResources();
        targetContext = getInstrumentation().getTargetContext();
        prefs = new Prefs(targetContext);

        prefs.setUsername("");
        prefs.setUsernamePassword("");
        prefs.setUserSignedIn(false);

        dismissWelcomeDialog();
        useTestServerAndShortRecordings();
        nowSwipeLeft();
        nowSwipeLeft(); // takes you to Sign In screen

    }

    private static void tearDownForSignInUser(ActivityTestRule<MainActivity> mActivityTestRule) {

         mActivityTestRule.getActivity().unRegisterEspressoIdlingResources();

    }



    private static void dismissWelcomeDialog(){
        try{

            onView(allOf(withId(android.R.id.button1), withText("OK"))).perform(scrollTo(),click());
        }catch (Exception ex){
            Log.e("SignInUser", ex.getLocalizedMessage());
        }
    }



    private static void useTestServerAndShortRecordings(){
        // User Test server.

        //  mActivityTestRule.getActivity().registerEspressoIdlingResources();
        targetContext = getInstrumentation().getTargetContext();
        prefs = new Prefs(targetContext);

        Util.setUseTestServer(targetContext, true);
        prefs.setUseShortRecordings(true);
    }

    private static void signIn(){

        try {
            onView(withId(R.id.etUserNameOrEmailInput)).perform(replaceText("timhot"), closeSoftKeyboard());

            onView(withId(R.id.etPasswordInput)).perform(replaceText("Pppother1"), closeSoftKeyboard());
            onView(withId(R.id.btnSignIn)).perform(click());

        }catch (Exception ex){
            Log.e("SignInUser", ex.getLocalizedMessage());
        }


        Log.e("SignInUser", "Finished");

        boolean userSignedIn = prefs.getUserSignedIn();

        assertEquals(userSignedIn, true);
        onView(withId(R.id.tvTitleMessage)).
    }



    private static void nowSwipeLeft(){

        onView(withId(R.id.SetUpWizard)).perform(swipeLeft());


    }
}
