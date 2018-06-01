package nz.org.cacophony.cacophonometer;

import android.content.Context;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.ViewInteraction;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Checkable;

import org.hamcrest.BaseMatcher;
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
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isNotChecked;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.isA;

/**
 * Created by Tim Hunt on 15-Mar-18.
 */

@SuppressWarnings("unchecked")
class HelperCode {

    public static void disableAirplaneMode(Context targetContext){
      // Assume settings already open
        checkRootAccessCheckBox();
        checkOnLineModeCheckBox();
        uncheckOfflineMode();
        Util.disableFlightMode(targetContext);
    }

    public static ViewAction setChecked(final boolean checked) {
        // https://stackoverflow.com/questions/37819278/android-espresso-click-checkbox-if-not-checked
        return new ViewAction() {
            @Override
            public BaseMatcher<View> getConstraints() {
                return new BaseMatcher<View>() {
                    @Override
                    public boolean matches(Object item) {
                        return isA(Checkable.class).matches(item);
                    }

                    @Override
                    public void describeMismatch(Object item, Description mismatchDescription) {}

                    @Override
                    public void describeTo(Description description) {}
                };
            }

            @Override
            public String getDescription() {
                return null;
            }

            @Override
            public void perform(UiController uiController, View view) {
                Checkable checkableView = (Checkable) view;
                checkableView.setChecked(checked);

            }
        };
    }


    public static void testCheckBox(int checkBoxId){

        // First check the box. This is a two step process as setChecked does NOT cause onclick code to fire, so first uncheck it then click it.
        onView(withId(checkBoxId)).perform(scrollTo(), HelperCode.setChecked(false));
        onView(withId(checkBoxId)).perform(scrollTo(), click());

        // Exit Settings screen, back to Main screen
        onView(allOf(withContentDescription("Navigate up"))).perform(click());

        // go back into settings to check that the box is checked
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onView(allOf(withId(R.id.title), withText("Settings"))).perform(click());
        onView(allOf(withId(checkBoxId))).check(matches(isChecked())); // https://developer.android.com/training/testing/espresso/basics.html

        // Now leave the app with the box unchecked
        onView(withId(checkBoxId)).perform(scrollTo(), HelperCode.setChecked(true));
        onView(withId(checkBoxId)).perform(scrollTo(), click());
        onView(allOf(withId(checkBoxId))).check(matches(isNotChecked()));
    }

//    public static void checkRootedCheckBoxAndDisableAirplaneMode(Context context){
//        openSettingsActivity();
//        checkRootAccessCheckBox();
//        if (!Util.isNetworkConnected(context)){
//            Util.disableFlightMode(context);
//            Util.waitForNetworkConnection(context, true);
//        }
//        assertTrue(Util.isNetworkConnected(context));
//
//        // Go back to main screen
//        returnToMainActivityScreen();
//    }

    public static void checkRootAccessCheckBox(){
        onView(withId(R.id.cbHasRootAccess)).perform(scrollTo(), HelperCode.setChecked(false));
        onView(withId(R.id.cbHasRootAccess)).perform(scrollTo(), click());
    }

    public static void checkOnLineModeCheckBox(){
        onView(withId(R.id.cbOnLineMode)).perform(scrollTo(), HelperCode.setChecked(false));
        onView(withId(R.id.cbOnLineMode)).perform(scrollTo(), click());
    }

    public static void unCheckOnLineModeCheckBox(){
        onView(withId(R.id.cbOnLineMode)).perform(scrollTo(), HelperCode.setChecked(true));
        onView(withId(R.id.cbOnLineMode)).perform(scrollTo(), click());
    }

// --Commented out by Inspection START (30-May-18 5:19 PM):
//    public static void unCheckRootAccessCheckBox(){
//        onView(withId(R.id.cbHasRootAccess)).perform(scrollTo(), HelperCode.setChecked(true));
//        onView(withId(R.id.cbHasRootAccess)).perform(scrollTo(), click());
//    }
// --Commented out by Inspection STOP (30-May-18 5:19 PM)

    public static void openSettingsActivity(){
        // Open settings
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onView(allOf(withId(R.id.title), withText("Settings"))).perform(click());
    }

    public static void openVitalsActivity(){
        // Open settings
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onView(allOf(withId(R.id.title), withText("Vitals"))).perform(click());
    }



    public static void returnToMainActivityScreen(){

        ViewInteraction appCompatImageButton = onView(
                allOf(withContentDescription("Navigate up"),
                        childAtPosition(
                                allOf(withId(R.id.my_toolbar),
                                        childAtPosition(
                                                withId(R.id.top_relative_layout),
                                                0)),
                                1),
                        isDisplayed()));
        appCompatImageButton.perform(click());
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

    public static boolean hasNetworkConnection(Context targetContext){
        Util.waitForNetworkConnection(targetContext, true);
        return Util.isNetworkConnected(targetContext);
    }

    public static boolean doesNOTHaveNetworkConnection(Context targetContext){
        Util.waitForNetworkConnection(targetContext, false);
        return !Util.isNetworkConnected(targetContext);
    }

    public static void uncheckOfflineMode(){
//        openSettingsActivity();

        onView(withId(R.id.cbOffLineMode)).perform(scrollTo(), HelperCode.setChecked(true));
        onView(withId(R.id.cbOffLineMode)).perform(scrollTo(), click());

        // Return to MainActivity screen
//        returnToMainActivityScreen();

    }

}
