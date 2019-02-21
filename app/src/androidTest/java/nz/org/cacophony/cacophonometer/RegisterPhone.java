package nz.org.cacophony.cacophonometer;

import android.content.Context;
import android.support.test.espresso.DataInteraction;
import android.support.test.rule.ActivityTestRule;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.TextView;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.io.File;
import java.util.Date;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
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

    private static void setUpForRegisterPhone(ActivityTestRule<MainActivity> mActivityTestRule){

        mActivityTestRule.getActivity().registerEspressoIdlingResources();
        targetContext = getInstrumentation().getTargetContext();
        prefs = new Prefs(targetContext);

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
        HelperCode.signIn(prefs);
try {
    Thread.sleep(1000); // had to put in sleep, as could not work out how to consistently get groups to display before testing code tries to choose a group
}catch (Exception ex){

}
        nowSwipeLeft(); // takes you to Groups screen



    }

    private static void tearDownForRegisterPhone(ActivityTestRule<MainActivity> mActivityTestRule) {

        Util.signOutUser(targetContext);
        mActivityTestRule.getActivity().unRegisterEspressoIdlingResources();

    }


    private static void registerPhone(){

        try {
            Thread.sleep(1000); // had to put in sleep, as could not work out how to consistently get groups to display before testing code tries to choose a group

            DataInteraction appCompatTextView = onData(anything())
                    .inAdapterView(allOf(withId(R.id.lvGroups),
                            childAtPosition(
                                    withClassName(is("android.support.constraint.ConstraintLayout")),
                                    1)))
                    .atPosition(0); // just uses the first group in the list
            appCompatTextView.perform(click());

            // App automatically moves to Register Phone screen
            // Now enter the device name

            // Create a unique device name
            Date now = new Date();

            String deviceName = Long.toString(now.getTime()/1000);

            onView(withId(R.id.etDeviceNameInput)).perform(replaceText(deviceName), closeSoftKeyboard());


            onView(withId(R.id.btnRegister)).perform(click());



        }catch (Exception ex){
            Log.e("RegisterPhone", ex.getLocalizedMessage());
        }


        Log.e("RegisterPhone", "Finished");

        boolean phoneRegistered = Util.isPhoneRegistered(targetContext);

//        assertEquals(phoneRegistered, true);
        assertEquals(true, phoneRegistered);

        onView(withId(R.id.tvMessagesRegister)).check(matches(withText("Success - Your phone has been registered with the server :-)" + " Swipe to next screen.")));



    }



    private static void nowSwipeLeft(){

        onView(withId(R.id.SetUpWizard)).perform(swipeLeft());


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
