package nz.org.cacophony.cacophonometerlite.SettingsActivityTests;


import android.support.test.espresso.Espresso;
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

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.pressImeActionButton;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class SetupAndVitals1 {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void setupAndVitals1() {
        Espresso.registerIdlingResources((mActivityTestRule.getActivity().getIdlingResource()));
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        ViewInteraction appCompatTextView = onView(
                allOf(ViewMatchers.withId(R.id.title), withText("Settings"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.support.v7.view.menu.ListMenuItemView")),
                                        0),
                                0),
                        isDisplayed()));
        appCompatTextView.perform(click());

        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.setupGroupNameInput),
                        childAtPosition(
                                allOf(withId(R.id.relative_layout2),
                                        childAtPosition(
                                                withId(R.id.mainScrollView),
                                                0)),
                                3)));
        appCompatEditText.perform(scrollTo(), replaceText("tim1"), closeSoftKeyboard());

        ViewInteraction appCompatEditText2 = onView(
                allOf(withId(R.id.setupGroupNameInput), withText("tim1"),
                        childAtPosition(
                                allOf(withId(R.id.relative_layout2),
                                        childAtPosition(
                                                withId(R.id.mainScrollView),
                                                0)),
                                3)));
        appCompatEditText2.perform(pressImeActionButton());

        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.setupRegisterButton), withText("Register"),
                        childAtPosition(
                                allOf(withId(R.id.relative_layout2),
                                        childAtPosition(
                                                withId(R.id.mainScrollView),
                                                0)),
                                4)));
        appCompatButton.perform(scrollTo(), click());

        ViewInteraction textView = onView(
                allOf(withId(R.id.setupRegisterStatus), withText("Registered in group: tim1"),
                        childAtPosition(
                                allOf(withId(R.id.relative_layout2),
                                        childAtPosition(
                                                withId(R.id.mainScrollView),
                                                0)),
                                2),
                        isDisplayed()));
        textView.check(matches(withText("Registered in group: tim1")));

//        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
//
//        ViewInteraction appCompatTextView2 = onView(
//                allOf(withId(R.id.title), withText("Vitals"),
//                        childAtPosition(
//                                childAtPosition(
//                                        withClassName(is("android.support.v7.view.menu.ListMenuItemView")),
//                                        0),
//                                0),
//                        isDisplayed()));
//        appCompatTextView2.perform(click());
//
//        ViewInteraction textView2 = onView(
//                allOf(withId(R.id.appVitalsText), withText("App Vitals"),
//                        childAtPosition(
//                                allOf(withId(R.id.relative_layout2),
//                                        childAtPosition(
//                                                IsInstanceOf.<View>instanceOf(android.widget.ScrollView.class),
//                                                0)),
//                                1),
//                        isDisplayed()));
//        textView2.check(matches(withText("App Vitals")));

//        ViewInteraction textView3 = onView(
//                allOf(withId(R.id.appPermissionText), withText("Required Permissions: ✔"),
//                        childAtPosition(
//                                allOf(withId(R.id.relative_layout2),
//                                        childAtPosition(
//                                                IsInstanceOf.<View>instanceOf(android.widget.ScrollView.class),
//                                                0)),
//                                2),
//                        isDisplayed()));
//        textView3.check(matches(withText("Required Permissions: ✔")));
//
//        ViewInteraction textView3 = onView(
//                allOf(withId(R.id.mainRegisteredStatus), withText("Registered: ✔"),
//                        childAtPosition(
//                                allOf(withId(R.id.relative_layout2),
//                                        childAtPosition(
//                                                IsInstanceOf.<View>instanceOf(android.widget.ScrollView.class),
//                                                0)),
//                                3),
//                        isDisplayed()));
//        textView3.check(matches(withText("Registered: ✔")));
//
//        ViewInteraction textView4 = onView(
//                allOf(withId(R.id.loggedInText), withText("Logged in to server: ✔"),
//                        childAtPosition(
//                                allOf(withId(R.id.relative_layout2),
//                                        childAtPosition(
//                                                IsInstanceOf.<View>instanceOf(android.widget.ScrollView.class),
//                                                0)),
//                                5),
//                        isDisplayed()));
//        textView4.check(matches(withText("Logged in to server: ✔")));
//
//        ViewInteraction textView5 = onView(
//                allOf(withId(R.id.deviceIDText), withText("Device ID: 280"),
//                        childAtPosition(
//                                allOf(withId(R.id.relative_layout2),
//                                        childAtPosition(
//                                                IsInstanceOf.<View>instanceOf(android.widget.ScrollView.class),
//                                                0)),
//                                6),
//                        isDisplayed()));
//        textView5.check(matches(withText("Device ID: 280")));

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
