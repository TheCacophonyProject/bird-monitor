package nz.org.cacophony.birdmonitor;


import org.junit.Before;
import org.junit.Test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static nz.org.cacophony.birdmonitor.HelperCode.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RegisterPhoneTest extends TestBaseStartingOnSetupScreen {

    @Before
    public void setUpAndLogIn() throws InterruptedException {
        nowNavigateRightTimes(2);
        HelperCode.signInPrimaryTestUser();
        nowNavigateRight();
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

        nowNavigateRight(); // need to go to next screen and back so that Un-register button displays
        nowNavigateLeft();

        HelperCode.unRegisterPhone();

        assertFalse(Util.isPhoneRegistered(targetContext));

        onView(withId(R.id.tvMessagesRegister)).check(matches(withText("Success - Device is no longer registered")));
    }

}
