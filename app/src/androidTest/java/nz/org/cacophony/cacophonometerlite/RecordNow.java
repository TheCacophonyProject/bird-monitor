package nz.org.cacophony.cacophonometerlite;

import android.content.Context;
import android.support.test.espresso.Espresso;
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
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static nz.org.cacophony.cacophonometerlite.HelperCode.uncheckOfflineMode;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Tim Hunt on 16-Mar-18.
 */

public class RecordNow {

    static Context targetContext;
    static Prefs prefs;
    static File recordingsFolder;
   static File recordingFiles[];
   static int numberOfRecordingsBeforePressingRecord;


    public static void recordNowButtonAndSaveOnPhone(ActivityTestRule<MainActivity> mActivityTestRule) {
        // This test presses the Record Now button and checks that a recording has been saved on the phone

        // Set settings to do a short recording and DO not upload to server - can then see if a new recording file is saved on phone

        setUpForRecordNowButtonAndSaveOnPhone(mActivityTestRule);

        // select Use Offline mode
        onView(withId(R.id.cbOffLineMode)).perform(scrollTo(), HelperCode.setChecked(false));
        onView(withId(R.id.cbOffLineMode)).perform(scrollTo(), click());

        // Go back to main screen
        onView(withContentDescription("Navigate up")).perform(click());

        // Check record now is enabled and press it, then check it goes to disabled
        onView(withId(R.id.recordNowButton)).check(matches(isEnabled()));
        onView(withId(R.id.recordNowButton)).perform(scrollTo()).perform(click()); // the record button also increments the recordNowIdlingResource

       // onView(withId(R.id.recordNowButton)).check(matches(not(isEnabled()))); // this didn't work.  I decremented recordNowIdlingResource before enabling button but it seems setEnable button happens too quick (could put in a delay on the main code but that would it would surely be wrong to slow down the app for testing purposes????!!!!

        onView(withId(R.id.recordNowButton)).check(matches(isEnabled())); // still worth checking that the button is re-enabled.

//        // So now check if there is a extra recording
//        int numberOfRecordingsAfterPressingRecord = recordingsFolder.listFiles().length;
//       assertEquals(numberOfRecordingsBeforePressingRecord + 1, numberOfRecordingsAfterPressingRecord);

        // Open settings and turn off 'Offline Mode'
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onView(allOf(withId(R.id.title), withText("Settings"))).perform(click());
        onView(withId(R.id.cbOffLineMode)).perform(scrollTo(), HelperCode.setChecked(true));
        onView(withId(R.id.cbOffLineMode)).perform(scrollTo(), click());

        // So now check if there is a extra recording
        int numberOfRecordingsAfterPressingRecord = recordingsFolder.listFiles().length;
        assertEquals(numberOfRecordingsBeforePressingRecord + 1, numberOfRecordingsAfterPressingRecord);

    }

    public static void recordNowButtonAndSaveOnServer(ActivityTestRule<MainActivity> mActivityTestRule) {
        // This test presses the Record Now button and checks that a recording has been saved on the SERVER (or at least the server returns an ID of the recording)
        setUpForRecordNowButtonAndSaveOnServerTest(mActivityTestRule);

        // Check record now is enabled and press it.
        onView(withId(R.id.recordNowButton)).perform(scrollTo()).check(matches(isEnabled()));
        onView(withId(R.id.recordNowButton)).perform(scrollTo()).perform(click()); // the record button also increments the uploadingIdlingResource

        onView(withId(R.id.recordNowButton)).check(matches(isEnabled())); // This test code needs to wait for recording to upload.  The IdlingResource check only works if this test code tries to access GUI component

        long lastRecordingIdFromServer = prefs.getLastRecordIdReturnedFromServer();
        assertTrue(lastRecordingIdFromServer >-1);

                try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }



    public static void setUpForRecordNowButtonAndSaveOnPhone(ActivityTestRule<MainActivity> mActivityTestRule){
        Espresso.registerIdlingResources((mActivityTestRule.getActivity().getUploadingIdlingResource()));
        Espresso.registerIdlingResources((mActivityTestRule.getActivity().getRecordNowIdlingResource()));
        targetContext = getInstrumentation().getTargetContext();
//        Util.disableFlightMode(targetContext);
        HelperCode.checkRootedCheckBoxAndDisableAirplaneMode(getInstrumentation().getTargetContext());
        prefs = new Prefs(targetContext);
        // Count the number of recording files before the recording, so can see if an extra one appears
        recordingsFolder = Util.getRecordingsFolder(targetContext);
        recordingFiles = recordingsFolder.listFiles();
        for (File file : recordingFiles){
            file.delete();
        }
        numberOfRecordingsBeforePressingRecord = recordingsFolder.listFiles().length; // should be zero

        // Open settings
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onView(allOf(withId(R.id.title), withText("Settings"))).perform(click());

        // select Use short recordings
        onView(withId(R.id.cbShortRecordings)).perform(scrollTo(), HelperCode.setChecked(false));
        onView(withId(R.id.cbShortRecordings)).perform(scrollTo(), click());
    }

    public static void setUpForRecordNowButtonAndSaveOnServerTest(ActivityTestRule<MainActivity> mActivityTestRule){

        Espresso.registerIdlingResources((mActivityTestRule.getActivity().getUploadingIdlingResource()));
        Espresso.registerIdlingResources((mActivityTestRule.getActivity().getRecordNowIdlingResource()));
        targetContext = getInstrumentation().getTargetContext();
        //Make sure 'Offline Mode' in Settings is not checked
        uncheckOfflineMode(targetContext);
//        Util.disableFlightMode(targetContext);
////        HelperCode.hasNetworkConnection(targetContext)
////        assertTrue(HelperCode.hasNetworkConnection(targetContext));
//        Util.waitForNetworkConnection(targetContext, true);
//        assertTrue(Util.isNetworkConnected(targetContext));
        prefs = new Prefs(targetContext);

        recordingsFolder = Util.getRecordingsFolder(targetContext);
        recordingFiles = recordingsFolder.listFiles();
        for (File file : recordingFiles){
            file.delete();
        }
//        // Open settings
//        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
//        onView(allOf(withId(R.id.title), withText("Settings"))).perform(click());

//        // Make sure Offline mode is NOT selected
//        onView(withId(R.id.cbOffLineMode)).perform(scrollTo(), HelperCode.setChecked(true));
//        onView(withId(R.id.cbOffLineMode)).perform(scrollTo(), click());
//
//        prefs.setLastRecordIdReturnedFromServer(-1);
//
//        // Go back to main screen
//        onView(withContentDescription("Navigate up")).perform(click());
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
