package nz.org.cacophony.birdmonitor;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static nz.org.cacophony.birdmonitor.HelperCode.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RecordAndSaveTest extends TestBaseStartingOnSetupScreen {

    @Before
    public void setUpForRecord() throws InterruptedException {
        nowNavigateRightTimes(2);
        HelperCode.signInPrimaryTestUser();
        nowNavigateRight();
        HelperCode.registerPhone(prefs);

        nowNavigateRightTimes(2); // takes you to Test RecordAndSaveOnPhone
    }

    @After
    public void tearDownForRecord() {
        File recordingsFolder = Util.getRecordingsFolder(targetContext);
        for (File file : recordingsFolder.listFiles()) {
            file.delete();
        }
    }


    @Test
    public void recordAndSaveOnPhone() {
        // Need to put phone into offline mode so it doesn't try to upload the recording
        prefs.setInternetConnectionMode("offline");

        int numberOfRecordingsBeforeTestRecord = Util.getNumberOfRecordings(targetContext);

        onView(withId(R.id.btnRecordNow)).perform(click());

        int numberOfRecordingsAfterTestRecord = Util.getNumberOfRecordings(targetContext);

        assertEquals(numberOfRecordingsBeforeTestRecord + 1, numberOfRecordingsAfterTestRecord);
    }

    @Test
    public void recordAndSaveOnServer() throws InterruptedException {
        // Need to put phone into normal mode so it uploads the recording
        prefs.setInternetConnectionMode("normal");

        File recordingsFolder = Util.getRecordingsFolder(targetContext);
        for (File file : recordingsFolder.listFiles()) {
            file.delete();
        }

        prefs.setLastRecordIdReturnedFromServer(-1);

        onView(withId(R.id.btnRecordNow)).perform(click());

        awaitIdlingResources();

        long lastRecordingIdFromServer = prefs.getLastRecordIdReturnedFromServer();
        assertTrue(lastRecordingIdFromServer > -1); // Don't know what the recording ID will be, so just check it exists
    }

}
