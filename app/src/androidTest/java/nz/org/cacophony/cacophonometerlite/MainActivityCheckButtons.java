package nz.org.cacophony.cacophonometerlite;


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
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class MainActivityCheckButtons {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void mainActivityPressRadioButtons() {
        ViewInteraction radioButton = onView(
                allOf(ViewMatchers.withId(R.id.offMode),
                        childAtPosition(
                                allOf(withId(R.id.chooseModeRadioButtons),
                                        childAtPosition(
                                                withId(R.id.relative_layout2),
                                                5)),
                                0),
                        isDisplayed()));
        radioButton.check(matches(isDisplayed()));

        ViewInteraction appCompatRadioButton = onView(
                allOf(withId(R.id.normalMode), withText("Normal Mode."),
                        childAtPosition(
                                allOf(withId(R.id.chooseModeRadioButtons),
                                        childAtPosition(
                                                withId(R.id.relative_layout2),
                                                5)),
                                1)));
        appCompatRadioButton.perform(scrollTo(), click());

        ViewInteraction appCompatRadioButton2 = onView(
                allOf(withId(R.id.normalModeOnline), withText("Normal Mode (But keep internet connection on)."),
                        childAtPosition(
                                allOf(withId(R.id.chooseModeRadioButtons),
                                        childAtPosition(
                                                withId(R.id.relative_layout2),
                                                5)),
                                2)));
        appCompatRadioButton2.perform(scrollTo(), click());

        ViewInteraction appCompatRadioButton3 = onView(
                allOf(withId(R.id.walkingMode), withText("Walking Mode (warning beeps, updates location, offline, records more often, ignores low battery)."),
                        childAtPosition(
                                allOf(withId(R.id.chooseModeRadioButtons),
                                        childAtPosition(
                                                withId(R.id.relative_layout2),
                                                5)),
                                3)));
        appCompatRadioButton3.perform(scrollTo(), click());

        ViewInteraction appCompatRadioButton4 = onView(
                allOf(withId(R.id.offMode), withText("Off - Will not override any settings."),
                        childAtPosition(
                                allOf(withId(R.id.chooseModeRadioButtons),
                                        childAtPosition(
                                                withId(R.id.relative_layout2),
                                                5)),
                                0)));
        appCompatRadioButton4.perform(scrollTo(), click());

    }

    @Test
    public void mainActivityCheckRecordNowButton() {
        onView(withId(R.id.recordNowButton)).check(matches(isDisplayed()));
        onView(withId(R.id.recordNowButton)).check(matches(isEnabled()));
//        onView(withId(R.id.recordNowButton)).perform(click()).check(matches(not(isEnabled()))); // Won't click it as it may affect later record now test - ie getting an extra recording file that confuses my count

    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

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
