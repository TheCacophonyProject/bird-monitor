package nz.org.cacophony.cacophonometer;

import android.content.Context;
import android.support.test.rule.ActivityTestRule;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.io.File;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.action.ViewActions.swipeRight;
import static android.support.test.espresso.matcher.ViewMatchers.isChecked;
import static android.support.test.espresso.matcher.ViewMatchers.isNotChecked;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Tim Hunt on 16-Mar-18.
 */

@SuppressWarnings("unused")
class GuiControls {

    private static Context targetContext;
    private static Prefs prefs;
    private static File recordingsFolder;
    private static File[] recordingFiles;




    public static void guiControls(ActivityTestRule<MainActivity> mActivityTestRule) {
        setUpForGuiControls(mActivityTestRule);

        guiControls();

        tearDownForGuiControls(mActivityTestRule);
    }



    private static void setUpForGuiControls(ActivityTestRule<MainActivity> mActivityTestRule){

        //   mActivityTestRule.getActivity().registerEspressoIdlingResources();
        targetContext = getInstrumentation().getTargetContext();
        prefs = new Prefs(targetContext);
        prefs.setInternetConnectionMode("normal");
        prefs.setIsDisabled(false);

        if (prefs.getDeviceName() == null){
            // Welcome Dialog WILL be displayed - and SetupWizard will be running
            HelperCode.dismissWelcomeDialog();
        }

    }



    private static void tearDownForGuiControls(ActivityTestRule<MainActivity> mActivityTestRule) {

        Util.signOutUser(targetContext);
        prefs.setIsDisabled(true);

    }




    private static void guiControls(){

        walking();
        enableOrDisableRecording();
        internetConnection();
        warningSound();
        ignoreLowBattery();
        frequencyRecord();
        frequencyUpload();
        frequenyGPS();
        rooted();
    }

    private static void walking(){
        Util.setWalkingMode(targetContext, false);
        onView(withId(R.id.btnWalking)).perform(click());
        onView(withId(R.id.swWalking2)).perform(click());
        onView(withId(R.id.btnFinished)).perform(click());

        boolean allPrefsForWalkingSet = areAllPrefsForWalkingSet();

       assertEquals(true, allPrefsForWalkingSet);

       // Now turn walking off
        onView(withId(R.id.btnWalking)).perform(click());
        onView(withId(R.id.swWalking2)).perform(click());
        onView(withId(R.id.btnFinished)).perform(click());

        allPrefsForWalkingSet = areAllPrefsForWalkingSet();

        assertEquals(true, !allPrefsForWalkingSet);

    }

    private static void enableOrDisableRecording(){
        prefs.setIsDisabled(false);
        onView(withId(R.id.btnDisable)).perform(click());
        onView(withId(R.id.swDisable)).perform(click());
        onView(withId(R.id.btnFinished)).perform(click());

        boolean isDisabled = prefs.getIsDisabled();

        assertEquals(true, isDisabled);

        // Now enable the app
        onView(withId(R.id.btnDisable)).perform(click());
        onView(withId(R.id.swDisable)).perform(click());
        onView(withId(R.id.btnFinished)).perform(click());

        isDisabled = prefs.getIsDisabled();

        assertEquals(true, !isDisabled);

    }

    private static void internetConnection(){
        prefs.setInternetConnectionMode("normal");
        onView(withId(R.id.btnAdvanced)).perform(click());
        nowSwipeLeft(); // takes you to Internet Connection

        // Check Normal selection
        onView(withId(R.id.rbNormal)).check(matches(isChecked()));
        String internetConnectionMode = prefs.getInternetConnectionMode();
        assertEquals("normal", internetConnectionMode);

        // Check Online
        onView(withId(R.id.rbOnline)).perform(click());
        onView(withId(R.id.rbOnline)).check(matches(isChecked()));
        internetConnectionMode = prefs.getInternetConnectionMode();
        assertEquals("online", internetConnectionMode);
        nowSwipeRight(); // leave page
        nowSwipeLeft();  // return to page
        onView(withId(R.id.rbOnline)).check(matches(isChecked())); // check Online is still selected
        internetConnectionMode = prefs.getInternetConnectionMode(); // and check still correct in preferences
        assertEquals("online", internetConnectionMode);

        // Offline
        onView(withId(R.id.rbOffline)).perform(click());
        onView(withId(R.id.rbOffline)).check(matches(isChecked()));
        internetConnectionMode = prefs.getInternetConnectionMode();
        assertEquals("offline", internetConnectionMode);
        nowSwipeRight(); // leave page
        nowSwipeLeft();  // return to page
        onView(withId(R.id.rbOffline)).check(matches(isChecked())); // check Online is still selected
        internetConnectionMode = prefs.getInternetConnectionMode(); // and check still correct in preferences
        assertEquals("offline", internetConnectionMode);

        prefs.setInternetConnectionMode("normal"); // finished testing interconnection mode so return to normal
    }

    private static void warningSound(){
        prefs.setPlayWarningSound(false);
        nowSwipeLeft();  // to get to Warning sound page
        onView(withId(R.id.swPlayWarningSound)).check(matches(isNotChecked())); // warning sound should be off

        // Now turn warning sound on
        onView(withId(R.id.swPlayWarningSound)).perform(click());  // to enable play warning sound
        onView(withId(R.id.swPlayWarningSound)).check(matches(isChecked())); // confirm it is checked
        boolean playWarningSound = prefs.getPlayWarningSound(); // check prefs now indicate to play a warning sound
        assertEquals(true, playWarningSound);
        nowSwipeRight(); // leave page
        nowSwipeLeft();  // return to page
        // confirm that prefs and page are still correct
        onView(withId(R.id.swPlayWarningSound)).check(matches(isChecked()));
        playWarningSound = prefs.getPlayWarningSound();
        assertEquals(true, playWarningSound);


        // Turn warning sound back off
        onView(withId(R.id.swPlayWarningSound)).perform(click()); // turn sound off
        onView(withId(R.id.swPlayWarningSound)).check(matches(isNotChecked())); // warning sound should be off
        playWarningSound = prefs.getPlayWarningSound();
        assertEquals(false, playWarningSound);

        // Finished so leave warning sound off
    }

    private static void ignoreLowBattery(){
        prefs.setIgnoreLowBattery(false);
        nowSwipeLeft();  // to get to Ignore Low Battery screen
        onView(withId(R.id.swIgnoreLowBattery)).check(matches(isNotChecked())); // Ignore Low Battery should be off

        // Now turn warning sound on
        onView(withId(R.id.swIgnoreLowBattery)).perform(click());  // to enable Ignore Low Battery
        onView(withId(R.id.swIgnoreLowBattery)).check(matches(isChecked())); // confirm it is checked
        boolean ignoreLowBattery = prefs.getIgnoreLowBattery(); // check prefs now indicate to Ignore Low Battery
        assertEquals(true, ignoreLowBattery);
        nowSwipeRight(); // leave page
        nowSwipeLeft();  // return to page
        // confirm that prefs and page are still correct
        onView(withId(R.id.swIgnoreLowBattery)).check(matches(isChecked()));
        ignoreLowBattery = prefs.getIgnoreLowBattery();
        assertEquals(true, ignoreLowBattery);


        // Turn Ignore Low Battery back off
        onView(withId(R.id.swIgnoreLowBattery)).perform(click()); // turn Ignore Low Battery off
        onView(withId(R.id.swIgnoreLowBattery)).check(matches(isNotChecked())); // Ignore Low Battery should be off
        ignoreLowBattery = prefs.getIgnoreLowBattery();
        assertEquals(false, ignoreLowBattery);

        // Finished so leave Ignore Low Battery off
    }

    private static void frequencyRecord(){
        Util.setUseFrequentRecordings(targetContext, false);
        Util.setUseFrequentUploads(targetContext, false);
        Util.setPeriodicallyUpdateGPS(targetContext, false);

        nowSwipeLeft();  // to get to Frequency screen

        onView(withId(R.id.swRecordMoreOften)).check(matches(isNotChecked())); // Record more often should be off
        onView(withId(R.id.swUseFrequentUploads)).check(matches(isNotChecked())); // Upload after every recording should be off
        onView(withId(R.id.swPeriodicallyUpdateGPS)).check(matches(isNotChecked())); // Periodically update GPS often should be off

        // Now turn record more often on
        onView(withId(R.id.swRecordMoreOften)).perform(click());
        onView(withId(R.id.swRecordMoreOften)).check(matches(isChecked())); // confirm it is checked
        // Check correct prefs variable was set and others were not set
        boolean recordMoreOften = prefs.getUseFrequentRecordings();
        assertEquals(true, recordMoreOften);

        boolean useFrequentUploads = prefs.getUseFrequentUploads();
        assertEquals(false, useFrequentUploads);

        boolean periodicallyUpdateGPS = prefs.getPeriodicallyUpdateGPS();
        assertEquals(false, periodicallyUpdateGPS);

        // Now leave the screen and return then check correct radio buttons are still checked

        nowSwipeRight(); // leave page
        nowSwipeLeft();  // return to page

        onView(withId(R.id.swRecordMoreOften)).check(matches(isChecked()));
        onView(withId(R.id.swUseFrequentUploads)).check(matches(isNotChecked()));
        onView(withId(R.id.swPeriodicallyUpdateGPS)).check(matches(isNotChecked()));

        // and check prefs are still correct
         recordMoreOften = prefs.getUseFrequentRecordings();
        assertEquals(true, recordMoreOften);

         useFrequentUploads = prefs.getUseFrequentUploads();
        assertEquals(false, useFrequentUploads);

         periodicallyUpdateGPS = prefs.getPeriodicallyUpdateGPS();
        assertEquals(false, periodicallyUpdateGPS);

        // Finished so leave all off
        onView(withId(R.id.swRecordMoreOften)).perform(click());
    }

    private static void frequencyUpload(){
        nowSwipeRight(); // to go to screen before frequency

        Util.setUseFrequentRecordings(targetContext, false);
        Util.setUseFrequentUploads(targetContext, false);
        Util.setPeriodicallyUpdateGPS(targetContext, false);

        nowSwipeLeft();  // to get to Frequency screen

        onView(withId(R.id.swRecordMoreOften)).check(matches(isNotChecked()));
        onView(withId(R.id.swUseFrequentUploads)).check(matches(isNotChecked()));
        onView(withId(R.id.swPeriodicallyUpdateGPS)).check(matches(isNotChecked()));


        onView(withId(R.id.swUseFrequentUploads)).perform(click());
        onView(withId(R.id.swUseFrequentUploads)).check(matches(isChecked())); // confirm it is checked
        // Check correct prefs variable was set and others were not set
        boolean recordMoreOften = prefs.getUseFrequentRecordings();
        assertEquals(false, recordMoreOften);

        boolean useFrequentUploads = prefs.getUseFrequentUploads();
        assertEquals(true, useFrequentUploads);

        boolean periodicallyUpdateGPS = prefs.getPeriodicallyUpdateGPS();
        assertEquals(false, periodicallyUpdateGPS);

        // Now leave the screen and return then check correct radio buttons are still checked

        nowSwipeRight(); // leave page
        nowSwipeLeft();  // return to page

        onView(withId(R.id.swRecordMoreOften)).check(matches(isNotChecked()));
        onView(withId(R.id.swUseFrequentUploads)).check(matches(isChecked()));
        onView(withId(R.id.swPeriodicallyUpdateGPS)).check(matches(isNotChecked()));

        // and check prefs are still correct
        recordMoreOften = prefs.getUseFrequentRecordings();
        assertEquals(false, recordMoreOften);

        useFrequentUploads = prefs.getUseFrequentUploads();
        assertEquals(true, useFrequentUploads);

        periodicallyUpdateGPS = prefs.getPeriodicallyUpdateGPS();
        assertEquals(false, periodicallyUpdateGPS);

        // Finished so leave all off
        onView(withId(R.id.swUseFrequentUploads)).perform(click());
    }

    private static void frequenyGPS(){
        nowSwipeRight(); // to go to screen before frequency

        Util.setUseFrequentRecordings(targetContext, false);
        Util.setUseFrequentUploads(targetContext, false);
        Util.setPeriodicallyUpdateGPS(targetContext, false);

        nowSwipeLeft();  // to get to Frequency screen

        onView(withId(R.id.swRecordMoreOften)).check(matches(isNotChecked()));
        onView(withId(R.id.swUseFrequentUploads)).check(matches(isNotChecked()));
        onView(withId(R.id.swPeriodicallyUpdateGPS)).check(matches(isNotChecked()));


        onView(withId(R.id.swPeriodicallyUpdateGPS)).perform(click());
        onView(withId(R.id.swPeriodicallyUpdateGPS)).check(matches(isChecked())); // confirm it is checked
        // Check correct prefs variable was set and others were not set
        boolean recordMoreOften = prefs.getUseFrequentRecordings();
        assertEquals(false, recordMoreOften);

        boolean useFrequentUploads = prefs.getUseFrequentUploads();
        assertEquals(false, useFrequentUploads);

        boolean periodicallyUpdateGPS = prefs.getPeriodicallyUpdateGPS();
        assertEquals(true, periodicallyUpdateGPS);

        // Now leave the screen and return then check correct radio buttons are still checked

        nowSwipeRight(); // leave page
        nowSwipeLeft();  // return to page

        onView(withId(R.id.swRecordMoreOften)).check(matches(isNotChecked()));
        onView(withId(R.id.swUseFrequentUploads)).check(matches(isNotChecked()));
        onView(withId(R.id.swPeriodicallyUpdateGPS)).check(matches(isChecked()));

        // and check prefs are still correct
        recordMoreOften = prefs.getUseFrequentRecordings();
        assertEquals(false, recordMoreOften);

        useFrequentUploads = prefs.getUseFrequentUploads();
        assertEquals(false, useFrequentUploads);

        periodicallyUpdateGPS = prefs.getPeriodicallyUpdateGPS();
        assertEquals(true, periodicallyUpdateGPS);

        // Finished so leave all off
        onView(withId(R.id.swPeriodicallyUpdateGPS)).perform(click());
    }

    private static void rooted(){
        prefs.setHasRootAccess(false);
        nowSwipeLeft();  // to get to Rooted sound page
        onView(withId(R.id.swRooted)).check(matches(isNotChecked())); // warning sound should be off

        // Now turn on
        onView(withId(R.id.swRooted)).perform(click());  // to enable play warning sound
        onView(withId(R.id.swRooted)).check(matches(isChecked())); // confirm it is checked
        boolean isRooted = prefs.getHasRootAccess();
        assertEquals(true, isRooted);
        nowSwipeRight(); // leave page
        nowSwipeLeft();  // return to page
        // confirm that prefs and page are still correct
        onView(withId(R.id.swRooted)).check(matches(isChecked()));
        isRooted = prefs.getHasRootAccess();
        assertEquals(true, isRooted);


        // Turn off
        onView(withId(R.id.swRooted)).perform(click()); // turn off
        onView(withId(R.id.swRooted)).check(matches(isNotChecked())); //  should be off
        isRooted = prefs.getHasRootAccess();
        assertEquals(false, isRooted);

        // Finished so leave off

    }

    private static boolean areAllPrefsForWalkingSet(){
        boolean walkingMode = true; // if any of the following are false, then change walking mode to false

        if (!prefs.getInternetConnectionMode().equalsIgnoreCase("offline")) {
            walkingMode = false;
        }else if(!prefs.getUseFrequentRecordings()){
            walkingMode = false;
        }else if (!prefs.getIgnoreLowBattery()){
            walkingMode = false;
        }else if (!prefs.getPlayWarningSound()){
            walkingMode = false;
        }else if (!prefs.getPeriodicallyUpdateGPS()){
            walkingMode = false;
        }else if (!prefs.getIsDisableDawnDuskRecordings()){
            walkingMode = false;
        }else if (prefs.getUseFrequentUploads()){
            walkingMode = false;

        }

        return walkingMode;
    }





    private static void nowSwipeLeft(){
        onView(withId(R.id.SetUpWizard)).perform(swipeLeft());
    }

    private static void nowSwipeRight(){
        onView(withId(R.id.SetUpWizard)).perform(swipeRight());
    }


}
