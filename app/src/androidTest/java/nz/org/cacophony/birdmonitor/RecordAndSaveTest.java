package nz.org.cacophony.birdmonitor;

import android.content.Context;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.AndroidJUnit4;
import nz.org.cacophony.birdmonitor.views.MainActivity;
import org.junit.*;
import org.junit.runner.RunWith;

import java.io.File;

import static android.Manifest.permission.*;
import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static nz.org.cacophony.birdmonitor.HelperCode.nowSwipeLeft;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class RecordAndSaveTest {

    private Context targetContext;
    private Prefs prefs;

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(
            WRITE_EXTERNAL_STORAGE,
            RECORD_AUDIO,
            ACCESS_FINE_LOCATION,
            READ_PHONE_STATE);

    @Rule
    public final ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @BeforeClass
    public static void registerIdlingResource() {
        IdlingRegistry.getInstance().register(IdlingResourceForEspressoTesting.createAccountIdlingResource);
        IdlingRegistry.getInstance().register(IdlingResourceForEspressoTesting.registerPhoneIdlingResource);
        IdlingRegistry.getInstance().register(IdlingResourceForEspressoTesting.uploadFilesIdlingResource);
        IdlingRegistry.getInstance().register(IdlingResourceForEspressoTesting.recordIdlingResource);
        IdlingRegistry.getInstance().register(IdlingResourceForEspressoTesting.signInIdlingResource);
        IdlingRegistry.getInstance().register(IdlingResourceForEspressoTesting.anyWebRequestResource);
    }

    @AfterClass
    public static void unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(IdlingResourceForEspressoTesting.createAccountIdlingResource);
        IdlingRegistry.getInstance().unregister(IdlingResourceForEspressoTesting.registerPhoneIdlingResource);
        IdlingRegistry.getInstance().unregister(IdlingResourceForEspressoTesting.uploadFilesIdlingResource);
        IdlingRegistry.getInstance().unregister(IdlingResourceForEspressoTesting.recordIdlingResource);
        IdlingRegistry.getInstance().unregister(IdlingResourceForEspressoTesting.signInIdlingResource);
        IdlingRegistry.getInstance().unregister(IdlingResourceForEspressoTesting.anyWebRequestResource);
    }

    @Before
    public void setUpForRecord() throws InterruptedException {
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
        Thread.sleep(1000); // had to put in sleep, as could not work out how to consistently get groups to display before testing code tries to choose a group

        nowSwipeLeft(); // takes you to Groups screen

        HelperCode.registerPhone(prefs);

        nowSwipeLeft(); // takes you to GPS
        prefs.setIsDisabled(false);
        nowSwipeLeft(); // takes you to Test RecordAndSaveOnPhone
    }

    @After
    public void tearDownForRecord() {
        prefs.setInternetConnectionMode("normal");
        Util.signOutUser(targetContext);

        File recordingsFolder = Util.getRecordingsFolder(targetContext);
        for (File file : recordingsFolder.listFiles()) {
            file.delete();
        }

        prefs.setIsDisabled(false);
    }


    @Test
    public void recordAndSaveOnPhone() {
        // Need to put phone into offline mode so it doesn't try to upload the recording
        prefs.setInternetConnectionMode("offline");

        int numberOfRecordingsBeforeTestRecord = Util.getNumberOfRecordings(targetContext);

        onView(withId(R.id.btnRecordNow)).perform(click());
        prefs.setIsDisabled(true);

        int numberOfRecordingsAfterTestRecord = Util.getNumberOfRecordings(targetContext);

        assertEquals(numberOfRecordingsBeforeTestRecord + 1, numberOfRecordingsAfterTestRecord);
    }

    @Test
    public void recordAndSaveOnServer() {
        // Need to put phone into normal mode so it uploads the recording
        prefs.setInternetConnectionMode("normal");

        File recordingsFolder = Util.getRecordingsFolder(targetContext);
        for (File file : recordingsFolder.listFiles()) {
            file.delete();
        }

        prefs.setLastRecordIdReturnedFromServer(-1);

        onView(withId(R.id.btnRecordNow)).perform(click());
        prefs.setIsDisabled(true);

        Espresso.onIdle();

        long lastRecordingIdFromServer = prefs.getLastRecordIdReturnedFromServer();
        assertTrue(lastRecordingIdFromServer > -1); // Don't know what the recording ID will be, so just check it exists
    }

}
