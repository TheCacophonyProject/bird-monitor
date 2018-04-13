package nz.org.cacophony.cacophonometerlite;

import android.support.test.espresso.idling.CountingIdlingResource;

/**
 * Created by Tim Hunt on 12-Mar-18.
 */

public interface IdlingResourceForEspressoTesting {
  public   CountingIdlingResource registerIdlingResource = new CountingIdlingResource("REGISTER");
  public   CountingIdlingResource recordNowIdlingResource = new CountingIdlingResource("RECORD_NOW");
  public   CountingIdlingResource uploadingIdlingResource = new CountingIdlingResource("UPLOADING");
}
