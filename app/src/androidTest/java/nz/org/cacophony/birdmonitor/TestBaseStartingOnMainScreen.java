package nz.org.cacophony.birdmonitor;

import org.junit.Before;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static nz.org.cacophony.birdmonitor.HelperCode.nowNavigateRight;
import static nz.org.cacophony.birdmonitor.HelperCode.nowNavigateRightTimes;

/**
 * This test class provides all the same initial state as {@link TestBaseStartingOnSetupScreen}.
 * In addition, these test cases will be logged in to the test user, device registered, and on the main screen.
 */
public abstract class TestBaseStartingOnMainScreen extends TestBaseStartingOnSetupScreen {

    @Before
    public final void goToMainScreen() throws InterruptedException {
        nowNavigateRightTimes(2);
        HelperCode.signInPrimaryTestUser();
        nowNavigateRight();
        HelperCode.registerPhone(prefs);
        nowNavigateRightTimes(3);
        onView(withId(R.id.btnFinished)).perform(click());
        Thread.sleep(500);
    }

}
