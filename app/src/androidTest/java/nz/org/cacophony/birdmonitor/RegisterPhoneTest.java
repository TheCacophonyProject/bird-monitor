package nz.org.cacophony.birdmonitor;


import android.content.Context;
import android.support.test.espresso.IdlingRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.AndroidJUnit4;
import org.junit.*;
import org.junit.runner.RunWith;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static nz.org.cacophony.birdmonitor.HelperCode.nowSwipeLeft;
import static nz.org.cacophony.birdmonitor.HelperCode.nowSwipeRight;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class RegisterPhoneTest {

    private Context targetContext;
    private Prefs prefs;

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(ACCESS_FINE_LOCATION);

    @Rule
    public final ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @BeforeClass
    public static void registerIdlingResource() {
        IdlingRegistry.getInstance().register(IdlingResourceForEspressoTesting.createAccountIdlingResource);
        IdlingRegistry.getInstance().register(IdlingResourceForEspressoTesting.registerPhoneIdlingResource);
        IdlingRegistry.getInstance().register(IdlingResourceForEspressoTesting.anyWebRequestResource);
    }

    @AfterClass
    public static void unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(IdlingResourceForEspressoTesting.createAccountIdlingResource);
        IdlingRegistry.getInstance().unregister(IdlingResourceForEspressoTesting.registerPhoneIdlingResource);
        IdlingRegistry.getInstance().unregister(IdlingResourceForEspressoTesting.anyWebRequestResource);
    }


    @Before
    public void setUpAndLogIn() throws InterruptedException {
        targetContext = getInstrumentation().getTargetContext();
        prefs = new Prefs(targetContext);
        prefs.setInternetConnectionMode("normal");
        prefs.setIsDisabled(false);

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
    }

    @After
    public void tearDownAndLogout() {
        Util.signOutUser(targetContext);
        prefs.setIsDisabled(true);
    }

    @Test
    public void registerPhoneTest() throws InterruptedException {
        HelperCode.registerPhone(prefs);

        assertTrue(Util.isPhoneRegistered(targetContext));

        onView(withId(R.id.tvMessagesRegister)).check(matches(withText("Success - Your phone has been registered with the server :-)" + " Swipe to next screen.")));
    }

    @Test
    public void unRegisterPhoneTest() throws InterruptedException {
        HelperCode.registerPhone(prefs);

        nowSwipeLeft(); // need to go to next screen and back so that Un-register button displays
        nowSwipeRight();

        HelperCode.unRegisterPhone();

        assertFalse(Util.isPhoneRegistered(targetContext));

        onView(withId(R.id.tvMessagesRegister)).check(matches(withText("Success - Device is no longer registered")));
    }

}
