package nz.org.cacophony.birdmonitor;

import org.junit.Before;
import org.junit.Test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static nz.org.cacophony.birdmonitor.HelperCode.awaitIdlingResources;
import static nz.org.cacophony.birdmonitor.HelperCode.nowNavigateRight;
import static nz.org.cacophony.birdmonitor.HelperCode.nowNavigateRightTimes;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class UploadRecordingsTest extends TestBaseStartingOnSetupScreen {

    @Before
    public void setUpForUploadAllRecordings() throws InterruptedException {
        nowNavigateRightTimes(2);
        HelperCode.signInPrimaryTestUser();
        nowNavigateRight();
        HelperCode.registerPhone(prefs);

        nowNavigateRightTimes(2); // takes you to Test RecordAndSaveOnPhone

        // Need to put phone into offline mode so it doesn't try to upload the recording
        prefs.setInternetConnectionMode("offline");

        onView(withId(R.id.btnRecordNow)).perform(click());

        onView(withId(R.id.btnFinished)).perform(click());

        onView(withId(R.id.btnAdvanced)).perform(click());

        prefs.setLastRecordIdReturnedFromServer(-1);
    }

    @Test
    public void uploadRecordings() throws InterruptedException {
        int numberOfRecordingsBeforeUpload = Util.getNumberOfRecordings(targetContext);
        assertTrue(numberOfRecordingsBeforeUpload > 0);
        prefs.setInternetConnectionMode("normal");

        onView(withId(R.id.btnUploadFiles)).perform(click());

        awaitIdlingResources();

        int numberOfRecordingsAfterDelete = Util.getNumberOfRecordings(targetContext);
        assertEquals(0, numberOfRecordingsAfterDelete);

        long lastRecordIdReturnedFromServer = prefs.getLastRecordIdReturnedFromServer();

        assertTrue(lastRecordIdReturnedFromServer > 0);

        onView(withId(R.id.tvMessagesManageRecordings)).check(matches(withText("Recordings have been uploaded to the server.")));
    }

}
