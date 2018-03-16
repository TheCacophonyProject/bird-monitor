package nz.org.cacophony.cacophonometerlite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Created by Tim Hunt on 16-Mar-18.
 */

// Runs all unit tests.
@RunWith(Suite.class)
@Suite.SuiteClasses({

        RunModeTest1.class,
        RunModeTest2.class,
        RunModeTest3.class,
        RunModeTest4.class,
        RunModeTest5.class
        })

public class RunModeTestsInSpecifiedOrder {

}

