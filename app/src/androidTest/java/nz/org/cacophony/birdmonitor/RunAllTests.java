package nz.org.cacophony.birdmonitor;

/*
  Created by Tim Hunt on 14-Mar-18.

  Often one or more of the tests will fail.  I haven't got to the bottom of the reason(s) but
  suspect it is because the app is also running the standard background recordings at the same
  time.

  Just run the individual test to check.  e.g if the deleteRecordings fails, then run 'RunDeleteRecordings' on its own.

 */



import org.junit.runner.RunWith;
import org.junit.runners.Suite;

// Runs all unit tests.
@RunWith(Suite.class)
@Suite.SuiteClasses({
        RunRooted.class,
        RunCreateAccount.class,
        RunSignInUser.class,
        RunUnRegisterPhone.class,
        RunRegisterPhone.class,
        RunRecordSaveOnPhone.class,
        RunRecordSaveOnServer.class,
        RunDeleteRecordings.class,
        RunUploadRecordings.class,
        RunGuiControls.class,

})
public class RunAllTests {


}