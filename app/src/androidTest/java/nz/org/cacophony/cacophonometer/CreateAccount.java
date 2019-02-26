package nz.org.cacophony.cacophonometer;

import android.content.Context;
import android.support.test.rule.ActivityTestRule;
import android.util.Log;

import java.io.File;
import java.util.Date;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Tim Hunt on 16-Mar-18.
 */

@SuppressWarnings("unused")
class CreateAccount {

    private static Context targetContext;
    private static Prefs prefs;




    public static void createAccount(ActivityTestRule<MainActivity> mActivityTestRule) {


        setUpForCreateAccount(mActivityTestRule);

        create();

        tearDownForCreateAccount(mActivityTestRule);
    }

    private static void setUpForCreateAccount(ActivityTestRule<MainActivity> mActivityTestRule){

      //  mActivityTestRule.getActivity().registerEspressoIdlingResources();
        targetContext = getInstrumentation().getTargetContext();
        prefs = new Prefs(targetContext);
        prefs.setInternetConnectionMode("normal");


        if (prefs.getDeviceName() == null){
            // Welcome Dialog WILL be displayed - and SetupWizard will be running
            HelperCode.dismissWelcomeDialog();
        }else{
            // Main menu will be showing

            onView(withId(R.id.btnSetup)).perform(click());
        }

        HelperCode.signOutUser(prefs, targetContext);
        HelperCode.useTestServerAndShortRecordings(prefs, targetContext);
        nowSwipeLeft();// takes you to Create Account screen


    }

    private static void tearDownForCreateAccount(ActivityTestRule<MainActivity> mActivityTestRule) {

      //  mActivityTestRule.getActivity().unRegisterEspressoIdlingResources();

    }


    private static void create(){

        try {
            // Create a uniqueish username
            Date now = new Date();
            String deviceName = Long.toString(now.getTime()/1000);
            deviceName.substring(deviceName.length() - 8);
            onView(withId(R.id.etUsername)).perform(replaceText(deviceName), closeSoftKeyboard());
            onView(withId(R.id.etEmail)).perform(replaceText(deviceName + "@gmail.com"), closeSoftKeyboard());
            onView(withId(R.id.etPassword1)).perform(replaceText("Pppother1"), closeSoftKeyboard());
            onView(withId(R.id.etPassword2)).perform(replaceText("Pppother1"), closeSoftKeyboard());

            onView(withId(R.id.btnSignUp)).perform(click());

        }catch (Exception ex){
            Log.e("CreateAccount", ex.getLocalizedMessage());
        }


        Log.e("CreateAccount", "Finished");

     //   boolean userSignedIn = prefs.getUserSignedIn();

      //  assertEquals(userSignedIn, true);
        String successMessage = "Success, you have successfully created a new user account\n\nSwipe to next screen to sign in.";
        onView(withId(R.id.tvMessagesCreateAccount)).check(matches(withText(successMessage)));
       // nowSwipeLeft();
    }



    private static void nowSwipeLeft(){

        onView(withId(R.id.SetUpWizard)).perform(swipeLeft());


    }
}
