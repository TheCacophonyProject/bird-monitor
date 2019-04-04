package nz.org.cacophony.birdmonitor;

/**
 * Created by Tim Hunt on 14-Mar-18.
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