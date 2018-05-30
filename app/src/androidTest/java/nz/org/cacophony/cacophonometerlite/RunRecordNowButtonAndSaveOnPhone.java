package nz.org.cacophony.cacophonometerlite;



import android.test.suitebuilder.annotation.LargeTest;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by Tim Hunt on 14-Mar-18.
 */


@LargeTest
@RunWith(AndroidJUnit4.class)
public class RunRecordNowButtonAndSaveOnPhone {


    @Rule
    public final ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);


    @Test
    public void recordNowButtonAndSaveOnPhone() {
        // Assumptions for intitial conditions
        // 1) Device is registerd with test server and Use test server is checked

        // 3) Root access is checked

        // 6) Online mode is probably checked to allow remote control software (AirDroid) to be used
        // 7) Choose Mode is OFF


        RecordNow.recordNowButtonAndSaveOnPhone(mActivityTestRule);
    }


}