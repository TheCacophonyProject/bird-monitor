package nz.org.cacophony.birdmonitor;

import android.content.Context;

import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.idling.CountingIdlingResource;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.AndroidJUnit4;

import nz.org.cacophony.birdmonitor.views.MainActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import java.util.Map;

import static android.Manifest.permission.*;
import static androidx.test.InstrumentationRegistry.getInstrumentation;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

/**
 * This test class provides a base for any UI test. It resets the state to fully reset on the setup screen before each test.
 * The default initial state is on the setup screen, logged out, with automatic recording disabled, test server and short recordings enabled.
 */
@LargeTest
@RunWith(AndroidJUnit4.class)
public abstract class TestBaseStartingOnSetupScreen {

    Context targetContext;
    Prefs prefs;

    private Map<String, ?> prefsBackup;

    @Rule
    public final GrantPermissionRule permissionRule = GrantPermissionRule.grant(
            WRITE_EXTERNAL_STORAGE,
            RECORD_AUDIO,
            ACCESS_FINE_LOCATION,
            READ_PHONE_STATE);

    @Rule
    public final ActivityTestRule<MainActivity> initialActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Before
    public final void baseInit() {
        initIdlingResources();

        targetContext = getInstrumentation().getTargetContext();
        prefs = new Prefs(targetContext);

        prefsBackup = HelperCode.backupPrefs(targetContext);

        goToSetupPage(); // Need to do this before altering the settings otherwise we may expect different popup than is actually present

        prefs.setInternetConnectionMode("normal");
        prefs.setAutomaticRecordingsDisabled(true);
        HelperCode.signOutUserAndDevice(prefs);
        HelperCode.useTestServerAndShortRecordings();
        prefs.setVeryAdvancedSettingsEnabled(false);
    }

    @After
    public final void baseTearDown() {
        resetIdlingResources();
        HelperCode.restorePrefs(targetContext, prefsBackup);
    }

    private void goToSetupPage() {
        if (prefs.getDeviceName() == null) {
            // Welcome Dialog WILL be displayed - and SetupWizard will be running
            HelperCode.dismissWelcomeDialog();
        } else {
            // Main menu will be showing
            onView(withId(R.id.btnSetup)).perform(click());
        }
    }

    private void initIdlingResources() {
        for (CountingIdlingResource idlingResource : IdlingResourceForEspressoTesting.allIdlingResources) {
            while (!idlingResource.isIdleNow()) {
                idlingResource.decrement();
            }
            IdlingRegistry.getInstance().register(idlingResource);
        }
    }

    private void resetIdlingResources() {
        for (CountingIdlingResource idlingResource : IdlingResourceForEspressoTesting.allIdlingResources) {
            while (!idlingResource.isIdleNow()) {
                idlingResource.decrement();
            }
            IdlingRegistry.getInstance().unregister(idlingResource);
        }
    }

}
