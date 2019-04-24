package nz.org.cacophony.birdmonitor;

import android.content.Context;
import android.util.Log;

import java.util.Date;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static nz.org.cacophony.birdmonitor.IdlingResourceForEspressoTesting.recordIdlingResource;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

class HelperCode {

    public static void useTestServerAndShortRecordings() {

        Context targetContext = getInstrumentation().getTargetContext();
        Prefs prefs = new Prefs(targetContext);

        if (!prefs.getUseTestServer()) {
            Util.setUseTestServer(targetContext, true);
            prefs.setUseShortRecordings(true);
        }
    }

    public static void signOutUser(Prefs prefs) {

        prefs.setUsername(null);
        prefs.setUsernamePassword(null);
        prefs.setUserSignedIn(false);
    }

    public static void dismissWelcomeDialog() {
        try {

            try {
                while (!recordIdlingResource.isIdleNow()) {
                    recordIdlingResource.decrement();
                }
            } catch (Exception ex) {
                Log.e("Record", ex.getLocalizedMessage());
            }
            onView(allOf(withId(android.R.id.button1), withText("OK"))).perform(scrollTo(), click());
        } catch (Exception ex) {
            Log.e("HelperCode", ex.getLocalizedMessage());
        }
    }

    public static void dismissDialogWithYes() {
        try {
            onView(allOf(withId(android.R.id.button1), withText("YES"))).perform(scrollTo(), click());
        } catch (Exception ex) {
            Log.e("HelperCode", ex.getLocalizedMessage());
        }
    }

    public static void signInUserTimhot() {

        try {

            Thread.sleep(1000); // had to put in sleep, as the GUI was replacing the username after I set it below

            onView(withId(R.id.etUserNameOrEmailInput)).perform(replaceText("timhot"), closeSoftKeyboard());

            onView(withId(R.id.etPasswordInput)).perform(replaceText("Pppother1"), closeSoftKeyboard());
            onView(withId(R.id.btnSignIn)).perform(click());

        } catch (Exception ex) {
            Log.e("SignInUser", ex.getLocalizedMessage());
        }
    }


    public static void registerPhone(Prefs prefs) {

        try {
            Thread.sleep(1000); // had to put in sleep, as could not work out how to consistently get groups to display before testing code tries to choose a group

            onData(allOf(is(instanceOf(String.class)), is("tim1"))).perform(click());

            // App automatically moves to Register Phone screen
            // Now enter the device name

            // Create a unique device name
            Date now = new Date();

            String deviceName = Long.toString(now.getTime() / 1000);
            prefs.setLastDeviceNameUsedForTesting(deviceName); // save the device name so can find recordings for it later

            onView(withId(R.id.etDeviceNameInput)).perform(replaceText(deviceName), closeSoftKeyboard());
            onView(withId(R.id.btnRegister)).perform(click());

        } catch (Exception ex) {
            Log.e("RegisterPhone", ex.getLocalizedMessage());
        }
    }

    public static void unRegisterPhone() {

        try {
            Thread.sleep(1000);
            onView(withId(R.id.btnUnRegister)).perform(click());
            Thread.sleep(1000);
            dismissDialogWithYes();
        } catch (Exception ex) {
            Log.e("RegisterPhone", ex.getLocalizedMessage());
        }
    }
}
