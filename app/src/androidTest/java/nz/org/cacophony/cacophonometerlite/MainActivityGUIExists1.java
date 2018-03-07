package nz.org.cacophony.cacophonometerlite;


import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class MainActivityGUIExists1 {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void mainActivityGUIExists1() {
        ViewInteraction radioButton = onView(
                allOf(withId(R.id.offMode),
                        childAtPosition(
                                allOf(withId(R.id.chooseModeRadioButtons),
                                        childAtPosition(
                                                withId(R.id.relative_layout2),
                                                5)),
                                0),
                        isDisplayed()));
        radioButton.check(matches(isDisplayed()));

        ViewInteraction radioButton2 = onView(
                allOf(withId(R.id.normalMode),
                        childAtPosition(
                                allOf(withId(R.id.chooseModeRadioButtons),
                                        childAtPosition(
                                                withId(R.id.relative_layout2),
                                                5)),
                                1),
                        isDisplayed()));
        radioButton2.check(matches(isDisplayed()));

        ViewInteraction radioButton3 = onView(
                allOf(withId(R.id.normalModeOnline),
                        childAtPosition(
                                allOf(withId(R.id.chooseModeRadioButtons),
                                        childAtPosition(
                                                withId(R.id.relative_layout2),
                                                5)),
                                2),
                        isDisplayed()));
        radioButton3.check(matches(isDisplayed()));

        ViewInteraction radioButton4 = onView(
                allOf(withId(R.id.walkingMode),
                        childAtPosition(
                                allOf(withId(R.id.chooseModeRadioButtons),
                                        childAtPosition(
                                                withId(R.id.relative_layout2),
                                                5)),
                                3),
                        isDisplayed()));
        radioButton4.check(matches(isDisplayed()));

        ViewInteraction button = onView(
                allOf(withId(R.id.recordNowButton),
                        childAtPosition(
                                allOf(withId(R.id.relative_layout2),
                                        childAtPosition(
                                                IsInstanceOf.<View>instanceOf(android.widget.ScrollView.class),
                                                0)),
                                7),
                        isDisplayed()));
        button.check(matches(isDisplayed()));

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
