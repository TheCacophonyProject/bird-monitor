package nz.org.cacophony.birdmonitor;

import android.content.Context;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.AndroidJUnit4;
import nz.org.cacophony.birdmonitor.views.MainActivity;
import org.junit.*;
import org.junit.runner.RunWith;

import static android.Manifest.permission.*;
import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.*;
import static nz.org.cacophony.birdmonitor.HelperCode.nowSwipeLeft;
import static nz.org.cacophony.birdmonitor.HelperCode.nowSwipeRight;
import static org.junit.Assert.*;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class GuiControlsTest {

    private Context targetContext;
    private Prefs prefs;

    @Rule
    public final ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(
            WRITE_EXTERNAL_STORAGE,
            RECORD_AUDIO,
            ACCESS_FINE_LOCATION,
            READ_PHONE_STATE);

    @Before
    public void setUpForGuiControls() {
        targetContext = getInstrumentation().getTargetContext();
        prefs = new Prefs(targetContext);
        prefs.setInternetConnectionMode("normal");
        prefs.setIsDisabled(false);

        if (prefs.getDeviceName() == null) {
            // Welcome Dialog WILL be displayed - and SetupWizard will be running
            HelperCode.dismissWelcomeDialog();
        }
    }

    @After
    public void tearDownForGuiControls() {
        Util.signOutUser(targetContext);
        prefs.setIsDisabled(true);
    }


    @Test
    public void enableOrDisableRecordingTest() {
        prefs.setIsDisabled(false);
        onView(withId(R.id.btnDisable)).perform(click());
        onView(withId(R.id.swDisable)).perform(click());
        onView(withId(R.id.btnFinished)).perform(click());

        assertTrue(prefs.getIsDisabled());

        // Now enable the app
        onView(withId(R.id.btnDisable)).perform(click());
        onView(withId(R.id.swDisable)).perform(click());
        onView(withId(R.id.btnFinished)).perform(click());

        assertFalse(prefs.getIsDisabled());
    }


    @Test
    public void internetConnectionTest() {
        prefs.setInternetConnectionMode("normal");
        onView(withId(R.id.btnAdvanced)).perform(click());
        nowSwipeLeft(); // takes you to Internet Connection

        // Check Normal selection
        onView(withId(R.id.rbNormal)).check(matches(isChecked()));
        String internetConnectionMode = prefs.getInternetConnectionMode();
        assertEquals("normal", internetConnectionMode);

        // Check Online
        onView(withId(R.id.rbOnline)).perform(click());
        onView(withId(R.id.rbOnline)).check(matches(isChecked()));
        internetConnectionMode = prefs.getInternetConnectionMode();
        assertEquals("online", internetConnectionMode);
        nowSwipeRight(); // leave page
        nowSwipeLeft();  // return to page
        onView(withId(R.id.rbOnline)).check(matches(isChecked())); // check Online is still selected
        internetConnectionMode = prefs.getInternetConnectionMode(); // and check still correct in preferences
        assertEquals("online", internetConnectionMode);

        // Offline
        onView(withId(R.id.rbOffline)).perform(click());
        onView(withId(R.id.rbOffline)).check(matches(isChecked()));
        internetConnectionMode = prefs.getInternetConnectionMode();
        assertEquals("offline", internetConnectionMode);
        nowSwipeRight(); // leave page
        nowSwipeLeft();  // return to page
        onView(withId(R.id.rbOffline)).check(matches(isChecked())); // check Online is still selected
        internetConnectionMode = prefs.getInternetConnectionMode(); // and check still correct in preferences
        assertEquals("offline", internetConnectionMode);

        prefs.setInternetConnectionMode("normal"); // finished testing interconnection mode so return to normal
    }

    @Test
    public void warningSoundTest() {
        prefs.setPlayWarningSound(false);

        onView(withId(R.id.btnAdvanced)).perform(click());
        HelperCode.nowSwipeLeftTimes(2);

        onView(withId(R.id.swPlayWarningSound)).check(matches(isNotChecked())); // warning sound should be off

        // Now turn warning sound on
        onView(withId(R.id.swPlayWarningSound)).perform(click());  // to enable play warning sound
        onView(withId(R.id.swPlayWarningSound)).check(matches(isChecked())); // confirm it is checked
        boolean playWarningSound = prefs.getPlayWarningSound(); // check prefs now indicate to play a warning sound
        assertTrue(playWarningSound);
        nowSwipeRight(); // leave page
        nowSwipeLeft();  // return to page
        // confirm that prefs and page are still correct
        onView(withId(R.id.swPlayWarningSound)).check(matches(isChecked()));
        playWarningSound = prefs.getPlayWarningSound();
        assertTrue(playWarningSound);


        // Turn warning sound back off
        onView(withId(R.id.swPlayWarningSound)).perform(click()); // turn sound off
        onView(withId(R.id.swPlayWarningSound)).check(matches(isNotChecked())); // warning sound should be off
        playWarningSound = prefs.getPlayWarningSound();
        assertFalse(playWarningSound);

        // Finished so leave warning sound off
    }

    @Test
    public void ignoreLowBatteryTest() {
        prefs.setIgnoreLowBattery(false);

        onView(withId(R.id.btnAdvanced)).perform(click());
        HelperCode.nowSwipeLeftTimes(3);

        onView(withId(R.id.swIgnoreLowBattery)).check(matches(isNotChecked())); // Ignore Low Battery should be off

        // Now turn warning sound on
        onView(withId(R.id.swIgnoreLowBattery)).perform(click());  // to enable Ignore Low Battery
        onView(withId(R.id.swIgnoreLowBattery)).check(matches(isChecked())); // confirm it is checked
        boolean ignoreLowBattery = prefs.getIgnoreLowBattery(); // check prefs now indicate to Ignore Low Battery
        assertTrue(ignoreLowBattery);
        nowSwipeRight(); // leave page
        nowSwipeLeft();  // return to page
        // confirm that prefs and page are still correct
        onView(withId(R.id.swIgnoreLowBattery)).check(matches(isChecked()));
        ignoreLowBattery = prefs.getIgnoreLowBattery();
        assertTrue(ignoreLowBattery);

        // Turn Ignore Low Battery back off
        onView(withId(R.id.swIgnoreLowBattery)).perform(click()); // turn Ignore Low Battery off
        onView(withId(R.id.swIgnoreLowBattery)).check(matches(isNotChecked())); // Ignore Low Battery should be off
        ignoreLowBattery = prefs.getIgnoreLowBattery();
        assertFalse(ignoreLowBattery);

        // Finished so leave Ignore Low Battery off
    }

    @Test
    public void frequencyRecordTest() {
        Util.setUseFrequentRecordings(targetContext, false);
        Util.setUseFrequentUploads(targetContext, false);

        onView(withId(R.id.btnAdvanced)).perform(click());
        HelperCode.nowSwipeLeftTimes(4);

        onView(withId(R.id.swRecordMoreOften)).check(matches(isNotChecked())); // Record more often should be off
        onView(withId(R.id.swUseFrequentUploads)).check(matches(isNotChecked())); // Upload after every recording should be off

        // Now turn record more often on
        onView(withId(R.id.swRecordMoreOften)).perform(click());
        onView(withId(R.id.swRecordMoreOften)).check(matches(isChecked())); // confirm it is checked
        // Check correct prefs variable was set and others were not set
        boolean recordMoreOften = prefs.getUseFrequentRecordings();
        assertTrue(recordMoreOften);

        boolean useFrequentUploads = prefs.getUseFrequentUploads();
        assertFalse(useFrequentUploads);

        boolean periodicallyUpdateGPS = prefs.getPeriodicallyUpdateGPS();
        assertFalse(periodicallyUpdateGPS);

        // Now leave the screen and return then check correct radio buttons are still checked

        nowSwipeRight(); // leave page
        nowSwipeLeft();  // return to page

        onView(withId(R.id.swRecordMoreOften)).check(matches(isChecked()));
        onView(withId(R.id.swUseFrequentUploads)).check(matches(isNotChecked()));

        // and check prefs are still correct
        recordMoreOften = prefs.getUseFrequentRecordings();
        assertTrue(recordMoreOften);

        useFrequentUploads = prefs.getUseFrequentUploads();
        assertFalse(useFrequentUploads);

        periodicallyUpdateGPS = prefs.getPeriodicallyUpdateGPS();
        assertFalse(periodicallyUpdateGPS);

        // Finished so leave all off
        onView(withId(R.id.swRecordMoreOften)).perform(click());
    }

    @Test
    public void frequencyUploadTest() {
        Util.setUseFrequentRecordings(targetContext, false);
        Util.setUseFrequentUploads(targetContext, false);

        onView(withId(R.id.btnAdvanced)).perform(click());
        HelperCode.nowSwipeLeftTimes(4);

        onView(withId(R.id.swRecordMoreOften)).check(matches(isNotChecked()));
        onView(withId(R.id.swUseFrequentUploads)).check(matches(isNotChecked()));

        onView(withId(R.id.swUseFrequentUploads)).perform(click());
        onView(withId(R.id.swUseFrequentUploads)).check(matches(isChecked())); // confirm it is checked
        // Check correct prefs variable was set and others were not set
        boolean recordMoreOften = prefs.getUseFrequentRecordings();
        assertFalse(recordMoreOften);

        boolean useFrequentUploads = prefs.getUseFrequentUploads();
        assertTrue(useFrequentUploads);

        boolean periodicallyUpdateGPS = prefs.getPeriodicallyUpdateGPS();
        assertFalse(periodicallyUpdateGPS);

        // Now leave the screen and return then check correct radio buttons are still checked

        nowSwipeRight(); // leave page
        nowSwipeLeft();  // return to page

        onView(withId(R.id.swRecordMoreOften)).check(matches(isNotChecked()));
        onView(withId(R.id.swUseFrequentUploads)).check(matches(isChecked()));

        // and check prefs are still correct
        recordMoreOften = prefs.getUseFrequentRecordings();
        assertFalse(recordMoreOften);

        useFrequentUploads = prefs.getUseFrequentUploads();
        assertTrue(useFrequentUploads);

        periodicallyUpdateGPS = prefs.getPeriodicallyUpdateGPS();
        assertFalse(periodicallyUpdateGPS);

        // Finished so leave all off
        onView(withId(R.id.swUseFrequentUploads)).perform(click());
    }

    @Test
    @Ignore("This feature of periodically updating GPS seems to no longer exist")
    public void frequencyGPSTest() {
        Util.setUseFrequentRecordings(targetContext, false);
        Util.setUseFrequentUploads(targetContext, false);

        onView(withId(R.id.btnAdvanced)).perform(click());
        HelperCode.nowSwipeLeftTimes(4);

        onView(withId(R.id.swRecordMoreOften)).check(matches(isNotChecked()));
        onView(withId(R.id.swUseFrequentUploads)).check(matches(isNotChecked()));

        // Check correct prefs variable was set and others were not set
        boolean recordMoreOften = prefs.getUseFrequentRecordings();
        assertFalse(recordMoreOften);

        boolean useFrequentUploads = prefs.getUseFrequentUploads();
        assertFalse(useFrequentUploads);

        boolean periodicallyUpdateGPS = prefs.getPeriodicallyUpdateGPS();
        assertTrue(periodicallyUpdateGPS);

        // Now leave the screen and return then check correct radio buttons are still checked

        nowSwipeRight(); // leave page
        nowSwipeLeft();  // return to page

        onView(withId(R.id.swRecordMoreOften)).check(matches(isNotChecked()));
        onView(withId(R.id.swUseFrequentUploads)).check(matches(isNotChecked()));

        // and check prefs are still correct
        recordMoreOften = prefs.getUseFrequentRecordings();
        assertFalse(recordMoreOften);

        useFrequentUploads = prefs.getUseFrequentUploads();
        assertFalse(useFrequentUploads);

        periodicallyUpdateGPS = prefs.getPeriodicallyUpdateGPS();
        assertTrue(periodicallyUpdateGPS);
    }

    @Test
    public void rootedTest() {
        prefs.setHasRootAccess(false);

        onView(withId(R.id.btnAdvanced)).perform(click());
        HelperCode.nowSwipeLeftTimes(5);

        onView(withId(R.id.swRooted)).check(matches(isNotChecked())); // warning sound should be off

        // Now turn on
        onView(withId(R.id.swRooted)).perform(click());  // to enable play warning sound
        onView(withId(R.id.swRooted)).check(matches(isChecked())); // confirm it is checked
        boolean isRooted = prefs.getHasRootAccess();
        assertTrue(isRooted);
        nowSwipeRight(); // leave page
        nowSwipeLeft();  // return to page
        // confirm that prefs and page are still correct
        onView(withId(R.id.swRooted)).check(matches(isChecked()));
        isRooted = prefs.getHasRootAccess();
        assertTrue(isRooted);


        // Turn off
        onView(withId(R.id.swRooted)).perform(click()); // turn off
        onView(withId(R.id.swRooted)).check(matches(isNotChecked())); //  should be off
        isRooted = prefs.getHasRootAccess();
        assertFalse(isRooted);

        // Finished so leave off
    }

}
