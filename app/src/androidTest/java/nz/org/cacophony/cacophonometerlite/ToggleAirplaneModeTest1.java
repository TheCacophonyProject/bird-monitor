package nz.org.cacophony.cacophonometerlite;


import android.os.Build;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class ToggleAirplaneModeTest1 {


    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);


    @Test
    public void test1() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN && isRootAvailable()) { // Jelly bean is 4.1
            AirplaneModeToggleTest.testAirplaneModeTogglingOnRootedAndGreaterThanJellyBean(mActivityTestRule);
        }
    }

    @Test
    public void test2() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN && !isRootAvailable()) { // Jelly bean is 4.1
            // This test needs to be run twice, once with airplane mode on and then again with it off
            AirplaneModeToggleTest.testAirplaneModeTogglingOnNonRootedAndGreaterThanJellyBean(mActivityTestRule);
        }
    }

//    @Test
//    public void test3() {
//        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN && isRootAvailable()) { // Jelly bean is 4.1
//
//        }
//    }
//
//    @Test
//    public void test4() {
//        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN && !isRootAvailable()) { // Jelly bean is 4.1
//
//        }
//    }

    public static boolean isRootAvailable(){
        // https://stackoverflow.com/questions/1101380/determine-if-running-on-a-rooted-device
        for(String pathDir : System.getenv("PATH").split(":")){
            if(new File(pathDir, "su").exists()) {
                return true;
            }
        }
        return false;
    }

}