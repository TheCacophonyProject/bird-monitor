package nz.org.cacophony.cacophonometer;

import android.content.Context;
import android.support.test.rule.ActivityTestRule;
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
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.action.ViewActions.swipeRight;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
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

    public static void unRegisterPhone(ActivityTestRule<MainActivity> mActivityTestRule) {
        setUpForUnRegisterPhone(mActivityTestRule);
        unRegisterPhone();
        tearDownForUnRegisterPhone(mActivityTestRule);
    }

    private static void setUpForRegisterPhone(ActivityTestRule<MainActivity> mActivityTestRule){

     //   mActivityTestRule.getActivity().registerEspressoIdlingResources();
        targetContext = getInstrumentation().getTargetContext();
        prefs = new Prefs(targetContext);
        prefs.setInternetConnectionMode("normal");
        prefs.setIsDisabled(false);

        if (prefs.getDeviceName() == null){
            // Welcome Dialog WILL be displayed - and SetupWizard will be running
            HelperCode.dismissWelcomeDialog();
        }else{
            // Main menu will be showing

            onView(withId(R.id.btnSetup)).perform(click());
        }



        Util.unregisterPhone(targetContext);
        Util.signOutUser(targetContext);

        nowSwipeLeft();
        nowSwipeLeft(); // takes you to Sign In screen, which should be showing that user is signed in

        // Need to sign in
        HelperCode.signInUserTimhot(prefs);
try {
    Thread.sleep(1000); // had to put in sleep, as could not work out how to consistently get groups to display before testing code tries to choose a group
}catch (Exception ex){

}
        nowSwipeLeft(); // takes you to Groups screen



    }

    private static void setUpForUnRegisterPhone(ActivityTestRule<MainActivity> mActivityTestRule){

        targetContext = getInstrumentation().getTargetContext();
        prefs = new Prefs(targetContext);
        prefs.setInternetConnectionMode("normal");
        prefs.setIsDisabled(false);

        if (prefs.getDeviceName() == null){
            // Welcome Dialog WILL be displayed - and SetupWizard will be running
            HelperCode.dismissWelcomeDialog();
        }else{
            // Main menu will be showing

            onView(withId(R.id.btnSetup)).perform(click());
        }

        Util.unregisterPhone(targetContext);
        Util.signOutUser(targetContext);
        nowSwipeLeft();
        nowSwipeLeft(); // takes you to Sign In screen, which should be showing that user is signed in

        // Need to sign in
        HelperCode.signInUserTimhot(prefs);
        try {
            Thread.sleep(1000); // had to put in sleep, as could not work out how to consistently get groups to display before testing code tries to choose a group
        }catch (Exception ex){

        }
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


    private static void registerPhone(){

        HelperCode.registerPhone(prefs);

        boolean phoneRegistered = Util.isPhoneRegistered(targetContext);

//        assertEquals(phoneRegistered, true);
        assertEquals(true, phoneRegistered);

        onView(withId(R.id.tvMessagesRegister)).check(matches(withText("Success - Your phone has been registered with the server :-)" + " Swipe to next screen.")));

    }

    private static void unRegisterPhone(){


        HelperCode.unRegisterPhone(prefs);

        boolean phoneRegistered = Util.isPhoneRegistered(targetContext);

//        assertEquals(phoneRegistered, true);
        assertEquals(false, phoneRegistered);

        onView(withId(R.id.tvMessagesRegister)).check(matches(withText("Success - Device is no longer registered")));


    }


    private static void nowSwipeLeft(){
        onView(withId(R.id.SetUpWizard)).perform(swipeLeft());
    }

    private static void nowSwipeRight(){
        onView(withId(R.id.SetUpWizard)).perform(swipeRight());
    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }
}
