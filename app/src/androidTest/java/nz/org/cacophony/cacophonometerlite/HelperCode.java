package nz.org.cacophony.cacophonometerlite;

import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.view.View;
import android.widget.Checkable;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isChecked;
import static android.support.test.espresso.matcher.ViewMatchers.isNotChecked;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.isA;

/**
 * Created by Tim Hunt on 15-Mar-18.
 */

public class HelperCode {

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
}
