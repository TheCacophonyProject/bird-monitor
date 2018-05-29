package nz.org.cacophony.cacophonometerlite;

import android.support.test.rule.ActivityTestRule;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isChecked;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertEquals;

/**
 * Created by Tim Hunt on 16-Mar-18.
 */

public class ChooseModeTestsCode {

    public static void modeTest1(ActivityTestRule<MainActivity> mActivityTestRule) {
        // Put into Off mode (not method modeTest was written to 'move' from one button to another but testAirplaneModeTogglingOnRootedAndGreaterThanJellyBean just needs to stay on one button so the same button is sent twice
        modeTest(mActivityTestRule, R.id.offMode, "off", R.id.offMode, "off");
           }

    public static void modeTest2(ActivityTestRule<MainActivity> mActivityTestRule) {
        // Check still in Off mode, then put into Normal mode
        modeTest(mActivityTestRule, R.id.offMode, "off", R.id.normalMode, "normal");
    }

    public static void modeTest3(ActivityTestRule<MainActivity> mActivityTestRule) {
        // Check still in Normal mode, then put into Normal keep internet on mode
        modeTest(mActivityTestRule, R.id.normalMode, "normal", R.id.normalModeOnline, "normalOnline");
    }

    public static void modeTest4(ActivityTestRule<MainActivity> mActivityTestRule) {
        // Check still in 'Normal keep internet on mode', then put into Walking mode
        modeTest(mActivityTestRule, R.id.normalModeOnline, "normalOnline", R.id.walkingMode, "walking");
    }

    public static void modeTest5(ActivityTestRule<MainActivity> mActivityTestRule) {
        // Check still in Walking mode and then put back to Off mode
        modeTest(mActivityTestRule, R.id.walkingMode, "walking", R.id.offMode, "off");

//        try {
//            Thread.sleep(5000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

    }

    private static void modeTest(ActivityTestRule<MainActivity> mActivityTestRule, int idOfFirstGuiRadioButton, String expectedModeStringFromPrefsInitialRadioButton, int idOfSecondGuiRadioButton, String expectedModeStringFromPrefsSecondRadioButton){
    //    Espresso.registerIdlingResources((mActivityTestRule.getActivity().getRegisterIdlingResource()));
        setup(mActivityTestRule);

        Prefs prefs = new Prefs(getInstrumentation().getTargetContext());

        onView(withId(idOfFirstGuiRadioButton)).check(matches(isChecked()));
        String mode = prefs.getMode();
        assertEquals(mode, expectedModeStringFromPrefsInitialRadioButton);

        onView(withId(idOfSecondGuiRadioButton)).perform(scrollTo()).perform(click());
        onView(withId(idOfSecondGuiRadioButton)).perform(scrollTo()).check(matches(isChecked()));
        mode = prefs.getMode();
        assertEquals(mode, expectedModeStringFromPrefsSecondRadioButton);
        tearDown(mActivityTestRule);
    }

    public static void setup(ActivityTestRule<MainActivity> mActivityTestRule){
        mActivityTestRule.getActivity().registerEspressoIdlingResources();
    }

    public static void tearDown(ActivityTestRule<MainActivity> mActivityTestRule){
        mActivityTestRule.getActivity().unRegisterEspressoIdlingResources();

    }

}
