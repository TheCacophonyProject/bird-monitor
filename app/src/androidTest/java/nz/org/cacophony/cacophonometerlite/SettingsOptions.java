package nz.org.cacophony.cacophonometerlite;

import android.support.test.espresso.Espresso;
import android.support.test.rule.ActivityTestRule;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isChecked;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

/**
 * Created by Tim Hunt on 15-Mar-18.
 */

public class SettingsOptions {

    public static void settingsOptions(ActivityTestRule<MainActivity> mActivityTestRule){

        Espresso.registerIdlingResources((mActivityTestRule.getActivity().getIdlingResource()));

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onView(allOf(withId(R.id.title), withText("Settings"))).perform(click());


    // Will check/uncheck each option box in turn and make sure that it has kept the correct value after the app is restarted.
    // Will do each check box separately in case the background code checks/save to the wrong prefs setting/parameter

        onView(withId(R.id.cbAlwaysUpdateGPS)).perform(scrollTo(), HelperCode.setChecked(!true));
        onView(withId(R.id.cbAlwaysUpdateGPS)).perform(scrollTo(), click());

//        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        onView(allOf(withContentDescription("Navigate up"))).perform(click());

        // go back into settings to check that the box is checked
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onView(allOf(withId(R.id.title), withText("Settings"))).perform(click());
        onView(allOf(withId(R.id.cbAlwaysUpdateGPS))).check(matches(isChecked())); // https://developer.android.com/training/testing/espresso/basics.html





        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
