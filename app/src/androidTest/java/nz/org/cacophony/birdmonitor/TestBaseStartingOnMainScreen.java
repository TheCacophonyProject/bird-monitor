package nz.org.cacophony.birdmonitor;

import org.junit.Before;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static nz.org.cacophony.birdmonitor.HelperCode.nowNavigateRight;
import static nz.org.cacophony.birdmonitor.HelperCode.nowNavigateRightTimes;

public abstract class TestBaseStartingOnMainScreen extends TestBaseStartingOnSetupScreen {

    @Before
    public final void goToMainScreen() throws InterruptedException {
        nowNavigateRightTimes(2);
        HelperCode.signInUserTimhot();
        nowNavigateRight();
        HelperCode.registerPhone(prefs);
        nowNavigateRightTimes(3);
        onView(withId(R.id.btnFinished)).perform(click());
        Thread.sleep(500);
    }

}
