package nz.org.cacophony.birdmonitor;

import android.support.test.espresso.idling.CountingIdlingResource;

/**
 * This interface (used by the main activities) enables the espresso testing framework to wait for long running background
 * operations (such as uploading a recording) to finish, before the espresso test moves to the next
 * line of test code.  Basically a counter can be incremented when a long running operation starts,
 * and decremented when it finishes.  An Espresso test will not progress is the counter is non zero.
 */

interface IdlingResourceForEspressoTesting {

    CountingIdlingResource signInIdlingResource = new CountingIdlingResource("SIGNIN");
    CountingIdlingResource getGroupsIdlingResource = new CountingIdlingResource("GET_GROUPS");
    CountingIdlingResource registerPhoneIdlingResource = new CountingIdlingResource("REGISTER_PHONE");
    CountingIdlingResource createAccountIdlingResource = new CountingIdlingResource("CREATE_ACCOUNT");
    CountingIdlingResource recordIdlingResource = new CountingIdlingResource("RECORD");
    CountingIdlingResource uploadFilesIdlingResource = new CountingIdlingResource("UPLOAD_FILES");
    CountingIdlingResource rootedIdlingResource = new CountingIdlingResource("ROOT");

    CountingIdlingResource testUploadRecordingsIdlingResource = new CountingIdlingResource("TEST_UPLOAD_RECORDINGS");

}
