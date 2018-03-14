package nz.org.cacophony.cacophonometerlite;



import org.junit.runner.RunWith;
import org.junit.runners.Suite;

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


