package nz.org.cacophony.birdmonitor;

import android.content.Context;
import android.support.test.espresso.IdlingRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import nz.org.cacophony.birdmonitor.views.MainActivity;
import org.junit.*;
import org.junit.runner.RunWith;

import java.util.UUID;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.*;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withSubstring;
import static nz.org.cacophony.birdmonitor.HelperCode.nowSwipeLeft;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class CreateAccountTest {

    private String uniqueId;

    @Rule
    public final ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @BeforeClass
    public static void registerIdlingResource() {
        IdlingRegistry.getInstance().register(IdlingResourceForEspressoTesting.createAccountIdlingResource);
        IdlingRegistry.getInstance().register(IdlingResourceForEspressoTesting.registerPhoneIdlingResource);
        IdlingRegistry.getInstance().register(IdlingResourceForEspressoTesting.signInIdlingResource);
        IdlingRegistry.getInstance().register(IdlingResourceForEspressoTesting.anyWebRequestResource);
    }

    @AfterClass
    public static void unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(IdlingResourceForEspressoTesting.createAccountIdlingResource);
        IdlingRegistry.getInstance().unregister(IdlingResourceForEspressoTesting.registerPhoneIdlingResource);
        IdlingRegistry.getInstance().unregister(IdlingResourceForEspressoTesting.signInIdlingResource);
        IdlingRegistry.getInstance().unregister(IdlingResourceForEspressoTesting.anyWebRequestResource);
    }

    @Before
    public void setUpForCreateAccount() {
        uniqueId = "cacophonometer_test_" + UUID.randomUUID();

        Context targetContext = getInstrumentation().getTargetContext();
        Prefs prefs = new Prefs(targetContext);
        prefs.setInternetConnectionMode("normal");

        if (prefs.getDeviceName() == null) {
            // Welcome Dialog WILL be displayed - and SetupWizard will be running
            HelperCode.dismissWelcomeDialog();
        } else {
            // Main menu will be showing
            onView(withId(R.id.btnSetup)).perform(click());
        }

        HelperCode.signOutUser(prefs);
        HelperCode.useTestServerAndShortRecordings();
        nowSwipeLeft();// takes you to Create Account screen
    }

    @Test
    public void createAccountTest() {
        onView(withId(R.id.etUsername)).perform(replaceText(uniqueId), closeSoftKeyboard());
        onView(withId(R.id.etEmail)).perform(replaceText(uniqueId + "@test.test"), closeSoftKeyboard());
        onView(withId(R.id.etPassword1)).perform(replaceText("Pppother1"), closeSoftKeyboard());
        onView(withId(R.id.etPassword2)).perform(replaceText("Pppother1"), closeSoftKeyboard());

        onView(withId(R.id.btnSignUp)).perform(click());

        onView(withId(R.id.tvMessagesCreateAccount)).check(matches(withSubstring("Success")));
    }
}