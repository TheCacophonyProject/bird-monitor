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
import org.hamcrest.core.IsInstanceOf;
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
public class SetupRegister1 {


    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);



    @Test
    public void setupRegister1() {

      // Register with idling couunter
        // https://developer.android.com/training/testing/espresso/idling-resource.html
        https://stackoverflow.com/questions/25470210/using-espresso-idling-resource-with-multiple-activities // this gave me idea to use an inteface

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

       // Unregister device (in may or may not be currently registerd)
        ViewInteraction unRegisterButton = onView(
                allOf(withId(R.id.setupUnregister), withText("unregister"),
                        childAtPosition(
                                allOf(withId(R.id.relative_layout2),
                                        childAtPosition(
                                                withId(R.id.mainScrollView),
                                                0)),
                                5)));
        unRegisterButton.perform(scrollTo(), click());

        // check it has un registerd
        ViewInteraction textViewRegisteredMessage = onView(
                allOf(withId(R.id.setupRegisterStatus), withText("Device not registered."),
                        childAtPosition(
                                allOf(withId(R.id.relative_layout2),
                                        childAtPosition(
                                                withId(R.id.mainScrollView),
                                                0)),
                                2),
                        isDisplayed()));

        textViewRegisteredMessage.check(matches(withText("Device not registered.")));

//        // don't check for toast just yet
//
//        // Un register again, should get a 'Not currenlty registered....' message
//        unRegisterButton.perform(scrollTo(), click());
//
//        // Now look for the Toast message
//        onView(withText("Not currently registered - so can not unregister :-(")).inRoot(new ToastMatcher())
//                .check(matches(isDisplayed()));
//


        // now register
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

        ViewInteraction appCompatButton2 = onView(
                allOf(withId(R.id.setupRegisterButton), withText("Register"),
                        childAtPosition(
                                allOf(withId(R.id.relative_layout2),
                                        childAtPosition(
                                                withId(R.id.mainScrollView),
                                                0)),
                                4)));
        appCompatButton2.perform(scrollTo(), click());

        // check it has registerd
         textViewRegisteredMessage = onView(
                allOf(withId(R.id.setupRegisterStatus), withText("Registered in group: tim1"),
                        childAtPosition(
                                allOf(withId(R.id.relative_layout2),
                                        childAtPosition(
                                                withId(R.id.mainScrollView),
                                                0)),
                                2),
                        isDisplayed()));

        textViewRegisteredMessage.check(matches(withText("Registered in group: tim1")));

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//        // now un register and check for toast and text box message
//        unRegisterButton.perform(scrollTo(), click());
//
//        // testing for the following toast failed - I think as the toast was queued behing other toasts
////        onView(withText("Success - Device is no longer registered")).inRoot(new ToastMatcher())
////                .check(matches(isDisplayed()));
//
//        textViewRegisteredMessage.check(matches(withText("Device not registered.")));

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        ViewInteraction appCompatTextView3 = onView(
                allOf(withId(R.id.title), withText("Vitals"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.support.v7.view.menu.ListMenuItemView")),
                                        0),
                                0),
                        isDisplayed()));
        appCompatTextView3.perform(click());

        ViewInteraction textViewAppVitals = onView(
                allOf(withId(R.id.appVitalsText), withText("App Vitals"),
                        childAtPosition(
                                allOf(withId(R.id.relative_layout2),
                                        childAtPosition(
                                                IsInstanceOf.<View>instanceOf(android.widget.ScrollView.class),
                                                0)),
                                1),
                        isDisplayed()));
        textViewAppVitals.check(matches(withText("App Vitals")));

        onView(withId(R.id.appPermissionText)).check(matches(withText(R.string.required_permissions_true)));

        // give time to look at screen
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        ViewInteraction textViewRequiredPermissions = onView(
//                allOf(withId(R.id.appPermissionText), withText(R.string.required_permissions_true),
//                        childAtPosition(
//                                allOf(withId(R.id.relative_layout2),
//                                        childAtPosition(
//                                                IsInstanceOf.<View>instanceOf(android.widget.ScrollView.class),
//                                                0)),
//                                2),
//                        isDisplayed()));
//       // textViewRequiredPermissions.check(matches(withText("Required Permissions: ✔")));
//        textViewRequiredPermissions.check(matches(withText(R.string.required_permissions_true)));

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
