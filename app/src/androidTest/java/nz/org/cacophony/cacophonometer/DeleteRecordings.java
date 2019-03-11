package nz.org.cacophony.cacophonometer;

import android.content.Context;
import android.support.test.rule.ActivityTestRule;
import java.io.File;
import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;

/**
 * Created by Tim Hunt on 16-Mar-18.
 */

@SuppressWarnings("unused")
class DeleteRecordings {

    private static Context targetContext;
    private static Prefs prefs;
    private static File recordingsFolder;
    private static File[] recordingFiles;


    public static void deleteRecordings(ActivityTestRule<MainActivity> mActivityTestRule) {
        setUpForDeleteAllRecordings(mActivityTestRule);
        deleteAllRecordings();
        tearDownForDeleteAllRecordings(mActivityTestRule);
    }

    private static void setUpForDeleteAllRecordings(ActivityTestRule<MainActivity> mActivityTestRule){

        targetContext = getInstrumentation().getTargetContext();
        prefs = new Prefs(targetContext);
        prefs.setInternetConnectionMode("normal");


        if (prefs.getDeviceName() == null){
            // Welcome Dialog WILL be displayed - and SetupWizard will be running
            HelperCode.dismissWelcomeDialog();
        }else{
            // Main menu will be showing
            onView(withId(R.id.btnSetup)).perform(click());
        }

        Util.unregisterPhone(targetContext);
        Util.signOutUser(targetContext);

        nowSwipeLeft();
        nowSwipeLeft(); // takes you to Sign In screen, which should be showing that user is signed in

        // Need to sign in
        HelperCode.signInUserTimhot(prefs);
        try {
            Thread.sleep(1000); // had to put in sleep, as could not work out how to consistently get groups to display before testing code tries to choose a group
        }catch (Exception ex){

        }
        nowSwipeLeft(); // takes you to Groups screen

        HelperCode.registerPhone(prefs);

        nowSwipeLeft(); // takes you to GPS
        prefs.setIsDisabled(false);
        nowSwipeLeft(); // takes you to Test RecordAndSaveOnPhone

        // Need to put phone into offline mode so it doesn't try to upload the recording
        prefs.setInternetConnectionMode("offline");

        int numberOfRecordingsBeforeTestRecord = Util.getNumberOfRecordings(targetContext);

        onView(withId(R.id.btnRecordNow)).perform(click());

        // Need to stop app trying to make more recordings as this stuffs up the upload files idling resource count
        prefs.setIsDisabled(true);

        onView(withId(R.id.btnFinished)).perform(click());

        onView(withId(R.id.btnAdvanced)).perform(click());

    }



    private static void tearDownForDeleteAllRecordings(ActivityTestRule<MainActivity> mActivityTestRule) {
        prefs.setInternetConnectionMode("normal");
        Util.signOutUser(targetContext);
        prefs.setIsDisabled(false);
    }


    private static void deleteAllRecordings(){

        int numberOfRecordingsBeforeDelete = Util.getNumberOfRecordings(targetContext);
        assertEquals(true, numberOfRecordingsBeforeDelete > 0);

        onView(withId(R.id.btnDeleteAllRecordings)).perform(click());
        HelperCode.dismissDialogWithYes();

        int numberOfRecordingsAfterDelete = Util.getNumberOfRecordings(targetContext);
        assertEquals(true, numberOfRecordingsAfterDelete == 0);

        onView(withId(R.id.tvMessagesManageRecordings)).check(matches(withText("All recordings on the phone have been deleted.")));
    }



    private static void nowSwipeLeft(){
        onView(withId(R.id.SetUpWizard)).perform(swipeLeft());
    }

}
