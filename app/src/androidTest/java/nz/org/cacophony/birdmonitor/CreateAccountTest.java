package nz.org.cacophony.birdmonitor;

import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSubstring;
import static nz.org.cacophony.birdmonitor.HelperCode.nowNavigateRight;

public class CreateAccountTest extends TestBaseStartingOnSetupScreen {

    private static final String TEST_USERNAME_PREFIX = "cacophonometer_test_";
    private static final String TEST_EMAIL_DOMAIN = "@test.test";
    private static final String TEST_PASSWORD = "test_password";

    private String uniqueId;

    @Before
    public void setUpForCreateAccount() {
        uniqueId = TEST_USERNAME_PREFIX + UUID.randomUUID();

        nowNavigateRight();// takes you to Create Account screen
    }

    @Test
    public void createAccountTest() {
        onView(withId(R.id.etUsername)).perform(replaceText(uniqueId), closeSoftKeyboard());
        onView(withId(R.id.etEmail)).perform(replaceText(uniqueId + TEST_EMAIL_DOMAIN), closeSoftKeyboard());
        onView(withId(R.id.etPassword1)).perform(replaceText(TEST_PASSWORD), closeSoftKeyboard());
        onView(withId(R.id.etPassword2)).perform(replaceText(TEST_PASSWORD), closeSoftKeyboard());

        onView(withId(R.id.btnSignUp)).perform(click());

        onView(withId(R.id.tvMessagesCreateAccount)).check(matches(withSubstring("Success")));
    }
}