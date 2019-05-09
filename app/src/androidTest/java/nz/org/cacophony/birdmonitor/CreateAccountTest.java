package nz.org.cacophony.birdmonitor;

import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.*;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withSubstring;
import static nz.org.cacophony.birdmonitor.HelperCode.nowNavigateRight;

public class CreateAccountTest extends TestBaseStartingOnSetupScreen {

    private String uniqueId;

    @Before
    public void setUpForCreateAccount() {
        uniqueId = "cacophonometer_test_" + UUID.randomUUID();

        nowNavigateRight();// takes you to Create Account screen
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