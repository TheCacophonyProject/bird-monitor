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
public class RunRecordNowTest1 {


    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);


    @Test
    public void pressRecordNow() {
        RecordNow.test1(mActivityTestRule);
    }


}