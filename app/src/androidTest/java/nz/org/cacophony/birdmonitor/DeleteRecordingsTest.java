package nz.org.cacophony.birdmonitor;

import android.content.Context;
import android.support.test.espresso.IdlingRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.AndroidJUnit4;
import org.junit.*;
import org.junit.runner.RunWith;

import static android.Manifest.permission.*;
import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static nz.org.cacophony.birdmonitor.HelperCode.nowSwipeLeft;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class DeleteRecordingsTest {

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

    @BeforeClass
    public static void registerIdlingResource() {
        IdlingRegistry.getInstance().register(IdlingResourceForEspressoTesting.recordIdlingResource);
        IdlingRegistry.getInstance().register(IdlingResourceForEspressoTesting.signInIdlingResource);
        IdlingRegistry.getInstance().register(IdlingResourceForEspressoTesting.anyWebRequestResource);
    }

    @AfterClass
    public static void unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(IdlingResourceForEspressoTesting.recordIdlingResource);
        IdlingRegistry.getInstance().unregister(IdlingResourceForEspressoTesting.signInIdlingResource);
        IdlingRegistry.getInstance().unregister(IdlingResourceForEspressoTesting.anyWebRequestResource);
    }

    @Before
    public void setUpForDeleteAllRecordings() throws InterruptedException {
        targetContext = getInstrumentation().getTargetContext();
        prefs = new Prefs(targetContext);

        prefs.setInternetConnectionMode("normal");

        // Need to make sure app isn't disabled, so 'RECORD NOW' button is visible/enabled
        prefs.setIsDisabled(true);


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

        // Need to put phone into offline mode so it doesn't try to upload the recording
        prefs.setInternetConnectionMode("offline");

        onView(withId(R.id.btnRecordNow)).perform(click());

        // Need to stop app trying to make more recordings as this stuffs up the upload files idling resource count
        prefs.setIsDisabled(true);

        onView(withId(R.id.btnFinished)).perform(click());

        onView(withId(R.id.btnAdvanced)).perform(click());
    }

    @After
    public void tearDownForDeleteAllRecordings() {
        prefs.setInternetConnectionMode("normal");
        Util.signOutUser(targetContext);
        prefs.setIsDisabled(false);
    }


    @Test
    public void deleteRecordingsTest() {
        int numberOfRecordingsBeforeDelete = Util.getNumberOfRecordings(targetContext);
        assertTrue(numberOfRecordingsBeforeDelete > 0);

        onView(withId(R.id.btnDeleteAllRecordings)).perform(click());
        HelperCode.dismissDialogWithYes();

        int numberOfRecordingsAfterDelete = Util.getNumberOfRecordings(targetContext);
        assertEquals(0, numberOfRecordingsAfterDelete);

        onView(withId(R.id.tvMessagesManageRecordings)).check(matches(withText("All recordings on the phone have been deleted.")));
    }
}
