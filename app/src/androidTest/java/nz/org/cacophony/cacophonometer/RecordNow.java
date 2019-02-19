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
class RecordNow {

    private static Context targetContext;
    private static Prefs prefs;
    private static File recordingsFolder;
    private static File[] recordingFiles;

//    @Rule
//    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);



    public static void recordNowButtonAndSaveOnPhone(ActivityTestRule<MainActivity> mActivityTestRule) {
        // This test presses the Record Now button and checks that a recording has been saved on the phone

        // Set settings to do a short recording and DO not upload to server - can then see if a new recording file is saved on phone

        setUpForRecordNowButtonAndSaveOnPhone(mActivityTestRule);

        // Count the number of recording files before the recording, so can see if an extra one appears
        recordingsFolder = Util.getRecordingsFolder(targetContext);
        recordingFiles = recordingsFolder.listFiles();
        for (File file : recordingFiles){
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }
        int numberOfRecordingsBeforePressingRecord = recordingsFolder.listFiles().length;



//        // Check record now is enabled and press it, then check it goes to disabled
//        onView(withId(R.id.recordNowButton)).check(matches(isEnabled()));
//        onView(withId(R.id.recordNowButton)).perform(scrollTo()).perform(click()); // the record button also increments the recordNowIdlingResource
//
//        // onView(withId(R.id.recordNowButton)).check(matches(not(isEnabled()))); // this didn't work.  I decremented recordNowIdlingResource before enabling button but it seems setEnable button happens too quick (could put in a delay on the main code but that would it would surely be wrong to slow down the app for testing purposes????!!!!
//
//        onView(withId(R.id.recordNowButton)).check(matches(isEnabled())); // still worth checking that the button is re-enabled.
//
////        // So now check if there is a extra recording
////        int numberOfRecordingsAfterPressingRecord = recordingsFolder.listFiles().length;
////       assertEquals(numberOfRecordingsBeforePressingRecord + 1, numberOfRecordingsAfterPressingRecord);
//
//        // So now check if there is a extra recording
//        int numberOfRecordingsAfterPressingRecord = recordingsFolder.listFiles().length;
//        assertEquals(numberOfRecordingsBeforePressingRecord + 1, numberOfRecordingsAfterPressingRecord);
//
//        tearDownForRecordNowButtonAndSaveOnPhone(mActivityTestRule);
//




    }

//    public static void recordNowButtonAndSaveOnServer(ActivityTestRule<MainActivity> mActivityTestRule) {
//        // This test presses the Record Now button and checks that a recording has been saved on the SERVER (or at least the server returns an ID of the recording)
//        setUpForRecordNowButtonAndSaveOnServerTest(mActivityTestRule);
//
//        recordingsFolder = Util.getRecordingsFolder(targetContext);
//        recordingFiles = recordingsFolder.listFiles();
//        for (File file : recordingFiles){
//            //noinspection ResultOfMethodCallIgnored
//            file.delete();
//        }

//        // Check record now is enabled and press it.
//        onView(withId(R.id.recordNowButton)).perform(scrollTo()).check(matches(isEnabled()));
//        onView(withId(R.id.recordNowButton)).perform(scrollTo()).perform(click()); // the record button also increments the uploadingIdlingResource
//
//        onView(withId(R.id.recordNowButton)).check(matches(isEnabled())); // This test code needs to wait for recording to upload.  The IdlingResource check only works if this test code tries to access GUI component
//
//        long lastRecordingIdFromServer = prefs.getLastRecordIdReturnedFromServer();
//        assertTrue(lastRecordingIdFromServer >-1); // Don't know what the recording ID will be, so just check it exists
//
//        tearDownForRecordNowButtonAndSaveOnServer( mActivityTestRule);
//    }



    private static void setUpForRecordNowButtonAndSaveOnPhone(ActivityTestRule<MainActivity> mActivityTestRule){

        mActivityTestRule.getActivity().registerEspressoIdlingResources();
        targetContext = getInstrumentation().getTargetContext();
        prefs = new Prefs(targetContext);

        dismissWelcomeDialog();
        useTestServerAndShortRecordings();
        nowSwipeLeft();
        nowSwipeLeft(); // takes you to Sign In screen
        signIn();


//        // Open settings
//        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
//        onView(allOf(withId(R.id.title), withText("Settings"))).perform(click());
//
//        // select Use short recordings
//        onView(withId(R.id.cbShortRecordings)).perform(scrollTo(), HelperCode.setChecked(false));
//        onView(withId(R.id.cbShortRecordings)).perform(scrollTo(), click());
//
//        // select Use Offline mode
//        onView(withId(R.id.cbOffLineMode)).perform(scrollTo(), HelperCode.setChecked(false));
//        onView(withId(R.id.cbOffLineMode)).perform(scrollTo(), click());
//
//        // Go back to main screen
//        onView(withContentDescription("Navigate up")).perform(click());
    }

    private static void tearDownForRecordNowButtonAndSaveOnPhone(ActivityTestRule<MainActivity> mActivityTestRule) {

        // Open settings and turn off 'Offline Mode'
//        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
//        onView(allOf(withId(R.id.title), withText("Settings"))).perform(click());
//        onView(withId(R.id.cbOffLineMode)).perform(scrollTo(), HelperCode.setChecked(true));
//        onView(withId(R.id.cbOffLineMode)).perform(scrollTo(), click());
//
//        mActivityTestRule.getActivity().unRegisterEspressoIdlingResources();

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

    private static void dismissWelcomeDialog(){
        try{

            onView(allOf(withId(android.R.id.button1), withText("OK"))).perform(scrollTo(),click());
        }catch (Exception ex){
            Log.e("RecordNow", ex.getLocalizedMessage());
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
//        prefs.setUsername("timhot");
//        prefs.setUsernamePassword("Pppother1");

      //  onView(withId(R.id.etUserNameOrEmailInput)).perform(scrollTo(), click());
        onView(withId(R.id.etUserNameOrEmailInput)).perform(replaceText("timhot"), closeSoftKeyboard());

       // onView(withId(R.id.tilPassword)).perform(scrollTo(), click());
        onView(withId(R.id.etPasswordInput)).perform(replaceText("Pppother1"), closeSoftKeyboard());

        onView(withId(R.id.btnSignIn)).perform(scrollTo(), click());




    }

    private static void nowSwipeLeftOld(){
        ViewInteraction viewPager = onView(
                allOf(withId(R.id.container),
                        childAtPosition(
                                allOf(withId(R.id.SetUpWizard),
                                        childAtPosition(
                                                withId(android.R.id.content),
                                                0)),
                                1),
                        isDisplayed()));
        viewPager.perform(swipeLeft());
    }

    private static void nowSwipeLeft(){

        onView(withId(R.id.SetUpWizard)).perform(swipeLeft());

//        ViewInteraction viewPager = onView(
//                allOf(withId(R.id.container),
//                        childAtPosition(
//                                allOf(withId(R.id.SetUpWizard),
//                                        childAtPosition(
//                                                withId(android.R.id.content),
//                                                0)),
//                                1),
//                        isDisplayed()));
//        viewPager.perform(swipeLeft());
    }
}
