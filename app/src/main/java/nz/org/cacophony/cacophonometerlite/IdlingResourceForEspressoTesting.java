package nz.org.cacophony.cacophonometerlite;

import android.support.test.espresso.idling.CountingIdlingResource;

/**
 * Created by Tim Hunt on 12-Mar-18.
 */

interface IdlingResourceForEspressoTesting {
    CountingIdlingResource registerIdlingResource = new CountingIdlingResource("REGISTER");
      CountingIdlingResource recordNowIdlingResource = new CountingIdlingResource("RECORD_NOW");
     CountingIdlingResource uploadingIdlingResource = new CountingIdlingResource("UPLOADING");
      CountingIdlingResource toggleAirplaneModeIdlingResource = new CountingIdlingResource("TOGGLE_AIRPLANE_MODE");


}
