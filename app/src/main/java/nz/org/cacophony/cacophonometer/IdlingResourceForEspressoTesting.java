package nz.org.cacophony.cacophonometer;

import android.support.test.espresso.idling.CountingIdlingResource;

/**
 * This interface (used by the main activities) enables the espresso testing framework to wait for long running background
 * operations (such as uploading a recording) to finish, before the espresso test moves to the next
 * line of test code.  Basically a counter can be incremented when a long running operation starts,
 * and decremented when it finishes.  An Espresso test will not progress is the counter is non zero.
 */

interface IdlingResourceForEspressoTesting {
//    CountingIdlingResource registerIdlingResource = new CountingIdlingResource("REGISTER");
//      CountingIdlingResource recordNowIdlingResource = new CountingIdlingResource("RECORD_NOW");
//     CountingIdlingResource uploadingIdlingResource = new CountingIdlingResource("UPLOADING");
//      CountingIdlingResource toggleAirplaneModeIdlingResource = new CountingIdlingResource("TOGGLE_AIRPLANE_MODE");


    CountingIdlingResource recordIdlingResource = new CountingIdlingResource("RECORD");

}
