package nz.org.cacophony.birdmonitor;

import android.content.Context;
import android.support.test.rule.ActivityTestRule;
import android.util.Log;
import nz.org.cacophony.birdmonitor.views.MainActivity;

import java.io.File;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Tim Hunt on 16-Mar-18.
 */

@SuppressWarnings("unused")
class Record {

    private static Context targetContext;
    private static Prefs prefs;
    private static File recordingsFolder;
    private static File[] recordingFiles;

    public static void RecordAndSaveOnPhone(ActivityTestRule<MainActivity> mActivityTestRule) {


        setUpForRecord(mActivityTestRule);

        recordAndSaveOnPhone();

        tearDownForRecord(mActivityTestRule);
    }

    public static void RecordAndSaveOnServer(ActivityTestRule<MainActivity> mActivityTestRule) {

        setUpForRecord(mActivityTestRule);

        recordAndSaveOnServer();

        tearDownForRecord(mActivityTestRule);
    }

    private static void setUpForRecord(ActivityTestRule<MainActivity> mActivityTestRule) {

        // Seems sometimes particualar idling resources are not idle e.g. [RECORD]


        targetContext = getInstrumentation().getTargetContext();
        prefs = new Prefs(targetContext);
        prefs.setInternetConnectionMode("normal");

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

        HelperCode.registerPhone(prefs);

        nowSwipeLeft(); // takes you to GPS
        prefs.setIsDisabled(false);
        nowSwipeLeft(); // takes you to Test RecordAndSaveOnPhone


    }

    private static void tearDownForRecord(ActivityTestRule<MainActivity> mActivityTestRule) {

        prefs.setInternetConnectionMode("normal");
        Util.signOutUser(targetContext);

        File recordingsFolder = Util.getRecordingsFolder(targetContext);
        for (File file : recordingsFolder.listFiles()) {
            file.delete();
        }

        prefs.setIsDisabled(false);

    }


    private static void recordAndSaveOnPhone() {
        // Need to put phone into offline mode so it doesn't try to upload the recording
        prefs.setInternetConnectionMode("offline");

        int numberOfRecordingsBeforeTestRecord = Util.getNumberOfRecordings(targetContext);


        onView(withId(R.id.btnRecordNow)).perform(click());
        prefs.setIsDisabled(true);

        int numberOfRecordingsAfterTestRecord = Util.getNumberOfRecordings(targetContext);

        assertEquals(numberOfRecordingsBeforeTestRecord + 1, numberOfRecordingsAfterTestRecord);
    }

    private static void recordAndSaveOnServer() {
        // Need to put phone into normal mode so it uploads the recording
        prefs.setInternetConnectionMode("normal");

        File recordingsFolder = Util.getRecordingsFolder(targetContext);
        for (File file : recordingsFolder.listFiles()) {
            file.delete();
        }

        prefs.setLastRecordIdReturnedFromServer(-1);

        onView(withId(R.id.btnRecordNow)).perform(click());
        prefs.setIsDisabled(true);
        try {
            Thread.sleep(10000); // Espresso says shouldn't do this, but I needed a way to wait for file to be uploaded.  Espresso only waits for counting resource to be idle IF/FOR simulated user interaction.
        } catch (Exception ex) {
            Log.e("Record", ex.getLocalizedMessage());
        }

        long lastRecordingIdFromServer = prefs.getLastRecordIdReturnedFromServer();
        assertTrue(lastRecordingIdFromServer > -1); // Don't know what the recording ID will be, so just check it exists
    }


    private static void nowSwipeLeft() {
        onView(withId(R.id.SetUpWizard)).perform(swipeLeft());
    }

}
