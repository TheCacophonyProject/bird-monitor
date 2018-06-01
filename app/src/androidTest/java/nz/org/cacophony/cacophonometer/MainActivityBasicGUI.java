package nz.org.cacophony.cacophonometer;


import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isChecked;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.allOf;

@SuppressWarnings("SameParameterValue")
@LargeTest
@RunWith(AndroidJUnit4.class)
public class MainActivityBasicGUI {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void birdLogoExists() {
        ViewInteraction imageView = onView(
                allOf(childAtPosition(
                        allOf(ViewMatchers.withId(R.id.my_toolbar),
                                childAtPosition(
                                        withId(R.id.top_relative_layout),
                                        0)),
                        0),
                        isDisplayed()));
        imageView.check(matches(isDisplayed()));

    }

    @Test
    public void offModeExists() {

        onView(withId(R.id.offMode)).perform(scrollTo()).check(matches(isDisplayed()));
    }

    @Test
    public void offModeSelected() {

        onView(withId(R.id.offMode)).perform(scrollTo()).check(matches(isChecked()));
    }

    @Test
    public void mainActivityGUIExists1() {


        onView(withId(R.id.offMode)).perform(scrollTo()).check(matches(isDisplayed()));


        onView(withId(R.id.normalMode)).perform(scrollTo()).check(matches(isDisplayed()));


        onView(withId(R.id.normalModeOnline)).perform(scrollTo()).check(matches(isDisplayed()));



        onView(withId(R.id.walkingMode)).perform(scrollTo()).check(matches(isDisplayed()));


        onView(withId(R.id.recordNowButton)).perform(scrollTo()).check(matches(isDisplayed()));
    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, @SuppressWarnings("SameParameterValue") final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }
}
