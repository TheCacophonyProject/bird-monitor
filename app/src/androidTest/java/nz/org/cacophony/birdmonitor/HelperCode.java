package nz.org.cacophony.birdmonitor;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.test.espresso.Espresso;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.*;
import static android.support.test.espresso.contrib.ViewPagerActions.scrollLeft;
import static android.support.test.espresso.contrib.ViewPagerActions.scrollRight;
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

    public static Map<String, ?> backupPrefs(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(Prefs.PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getAll();
    }

    public static void restorePrefs(Context context, Map<String, ?> backup) {
        SharedPreferences preferences = context.getSharedPreferences(Prefs.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        for (Map.Entry<String, ?> preference : backup.entrySet()) {
            if (preference.getValue() instanceof String) {
                editor.putString(preference.getKey(), (String) preference.getValue());
            } else if (preference.getValue() instanceof Boolean) {
                editor.putBoolean(preference.getKey(), (Boolean) preference.getValue());
            } else if (preference.getValue() instanceof Float) {
                editor.putFloat(preference.getKey(), (Float) preference.getValue());
            } else if (preference.getValue() instanceof Integer) {
                editor.putInt(preference.getKey(), (Integer) preference.getValue());
            } else if (preference.getValue() instanceof Long) {
                editor.putLong(preference.getKey(), (Long) preference.getValue());
            } else if (preference.getValue() instanceof Set) {
                editor.putStringSet(preference.getKey(), (Set<String>) preference.getValue());
            } else {
                throw new IllegalArgumentException("A value in the backup did not match the expected types, was: " + preference.getValue().getClass());
            }
        }
        editor.apply();
    }

    public static void signOutUserAndDevice(Prefs prefs) {
        prefs.setUsername(null);
        prefs.setUsernamePassword(null);
        prefs.setUserSignedIn(false);

        prefs.setGroupName(null);
        prefs.setDevicePassword(null);
        prefs.setDeviceName(null);
        prefs.setDeviceToken(null);
    }

    public static void dismissWelcomeDialog() {
        while (!recordIdlingResource.isIdleNow()) {
            recordIdlingResource.decrement();
        }
        onView(allOf(withId(android.R.id.button1), withText("OK"))).perform(scrollTo(), click());
    }

    public static void dismissDialogWithYes() {
        onView(allOf(withId(android.R.id.button1), withText("YES"))).perform(scrollTo(), click());
    }

    public static void signInUserTimhot() throws InterruptedException {
        Thread.sleep(1000); // had to put in sleep, as the GUI was replacing the username after I set it below

        onView(withId(R.id.etUserNameOrEmailInput)).perform(replaceText("timhot"), closeSoftKeyboard());

        onView(withId(R.id.etPasswordInput)).perform(replaceText("Pppother1"), closeSoftKeyboard());
        onView(withId(R.id.btnSignIn)).perform(click());
        Thread.sleep(1000);
    }


    public static void registerPhone(Prefs prefs) throws InterruptedException {
        Thread.sleep(1000); // had to put in sleep, as could not work out how to consistently get groups to display before testing code tries to choose a group

        onData(allOf(is(instanceOf(String.class)), is("tim1"))).perform(click());

        // App automatically moves to Register Phone screen
        // Now enter the device name

        // Create a unique device name
        String deviceName  = "cacophonometer_test_device_" + UUID.randomUUID();
        prefs.setLastDeviceNameUsedForTesting(deviceName); // save the device name so can find recordings for it later

        onView(withId(R.id.etDeviceNameInput)).perform(replaceText(deviceName), closeSoftKeyboard());
        onView(withId(R.id.btnRegister)).perform(click());
        Thread.sleep(1000);
    }

    public static void unRegisterPhone() throws InterruptedException {
        Thread.sleep(1000);
        onView(withId(R.id.btnUnRegister)).perform(click());
        Thread.sleep(1000);
        dismissDialogWithYes();
    }

    public static void awaitIdlingResources() throws InterruptedException {
        Espresso.onIdle();
        Thread.sleep(500);
        Espresso.onIdle();
    }

    public static void nowNavigateRight() {
        onView(withId(R.id.container)).perform(scrollRight());
    }

    public static void nowNavigateRightTimes(int times) {
        for (int i = 0; i < times; i++) {
            nowNavigateRight();
        }
    }

    public static void nowNavigateLeft() {
        onView(withId(R.id.container)).perform(scrollLeft());
    }

}
