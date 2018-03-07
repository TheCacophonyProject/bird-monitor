package nz.org.cacophony.cacophonometerlite.MainActivityTests;


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

import nz.org.cacophony.cacophonometerlite.MainActivity;
import nz.org.cacophony.cacophonometerlite.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isChecked;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isNotChecked;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class OffModeRadioButton {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void offModeExists() {
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

    }

    @Test
    public void offModeSelected() {
        ViewInteraction radioButton = onView(
                allOf(withId(R.id.offMode),
                        childAtPosition(
                                allOf(withId(R.id.chooseModeRadioButtons),
                                        childAtPosition(
                                                withId(R.id.relative_layout2),
                                                5)),
                                0),
                        isDisplayed()));

        radioButton.check(matches(isChecked()));
//        radioButton.check(matches(isNotChecked()));
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
