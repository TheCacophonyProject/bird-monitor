package nz.org.cacophony.birdmonitor;

import android.os.Looper;

import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import tools.fastlane.screengrab.Screengrab;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSubstring;
import static nz.org.cacophony.birdmonitor.HelperCode.nowNavigateRight;
import static nz.org.cacophony.birdmonitor.HelperCode.nowNavigateRightTimes;

public class ScreenshotGen extends TestBaseStartingOnSetupScreen {
    private static final String TEST_USERNAME_PREFIX = "cacophonometer_test_";
    private static final String TEST_EMAIL_DOMAIN = "@test.test";
    private static final String TEST_PASSWORD = "test_password";
    private String uniqueId;

    @Before
    public void setUpForScreenshots() {
        uniqueId = TEST_USERNAME_PREFIX + UUID.randomUUID();
    }
    @Test
    public void welcomeMessage(){
        onView(withId(R.id.button_help)).perform(click());
        Screengrab.screenshot("welcomeMessage");
    }

    @Test
    public void createUserSceenshot() throws InterruptedException {
        nowNavigateRight();
        Screengrab.screenshot("register");
        nowNavigateRightTimes(1);
        HelperCode.signInPrimaryTestUser();
        nowNavigateRight();
        HelperCode.registerPhone(prefs);
        RecordAndUpload.isRecording = false;
        nowNavigateRightTimes(3);
        Screengrab.screenshot("recTest");
        onView(withId(R.id.btnFinished)).perform(click());
        prefs.setDeviceName("Kokako");
        prefs.setAutomaticRecordingsDisabled(false);
        prefs.setUseTestServer(false);
        onView(withId(R.id.btnStatistics)).perform(click());
        Screengrab.screenshot("vitals");
        prefs.setUseTestServer(true);
        pressBack();
        Screengrab.screenshot("mainMenu");
        onView(withId(R.id.btnAdvanced)).perform(click());
        Screengrab.screenshot("recordingsFragment");
        nowNavigateRightTimes(2);
        Screengrab.screenshot("warningSound");
        pressBack();
        onView(withId(R.id.btnDisable)).perform(click());
        Screengrab.screenshot("disableRecordings");
        prefs.setAutomaticRecordingsDisabled(true);
    }



}
