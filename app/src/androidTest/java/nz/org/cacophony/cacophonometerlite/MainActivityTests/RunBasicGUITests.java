package nz.org.cacophony.cacophonometerlite.MainActivityTests;



import android.support.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import nz.org.cacophony.cacophonometerlite.MainActivity;

// Runs all unit tests.
@RunWith(Suite.class)
@Suite.SuiteClasses({

        MainActivityBasicGUI.class,
        MainActivityCheckButtons.class,
        OffModeRadioButton.class,
        MainActivityActionBar.class,
        OffModeRadioButton.class

})
public class RunBasicGUITests {


}


