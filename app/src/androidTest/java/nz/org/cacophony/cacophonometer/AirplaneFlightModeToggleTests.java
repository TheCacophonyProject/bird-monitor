package nz.org.cacophony.cacophonometer;


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
public class AirplaneFlightModeToggleTests {


    @Rule
    public  final ActivityTestRule<MainActivity>  mActivityTestRule = new ActivityTestRule<>(MainActivity.class);


    @Test
    public void canToggleAirplaneModeOnRootedDevice() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN && isRootAvailable()) { // Jelly bean is 4.1
            AirplaneModeToggleTest.testAirplaneModeTogglingOnRootedAndGreaterThanJellyBean(mActivityTestRule);
        }
    }

    @Test
    public void canToggleAirplaneModeOnNonRootedDevice() {
        boolean isRootAvailable = isRootAvailable();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN && !isRootAvailable) { // Jelly bean is 4.1
            // This test needs to be run twice, once with airplane mode on and then again with it off
            AirplaneModeToggleTest.testAirplaneModeTogglingOnNonRootedAndGreaterThanJellyBean(mActivityTestRule);
        }
    }



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