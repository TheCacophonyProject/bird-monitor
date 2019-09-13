package nz.org.cacophony.birdmonitor;

import org.junit.Before;
import org.junit.Test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static nz.org.cacophony.birdmonitor.HelperCode.nowNavigateRightTimes;
import static org.junit.Assert.assertTrue;

public class SignInUserTest extends TestBaseStartingOnSetupScreen {

    @Before
    public void setUpForSignInUser() {
        nowNavigateRightTimes(2);
    }

    @Test
    public void signInTest() throws InterruptedException {
        HelperCode.signInPrimaryTestUser();

        boolean userSignedIn = prefs.getUserSignedIn();

        assertTrue(userSignedIn);
        onView(withId(R.id.tvTitleMessageSignIn)).check(matches(withText("Signed In")));
    }
}
