package nz.org.cacophony.birdmonitor;



import android.support.test.espresso.IdlingRegistry;
import android.support.test.filters.LargeTest;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by Tim Hunt on 14-Mar-18.
 */


@LargeTest
@RunWith(AndroidJUnit4.class)
public class RunRooted {


    @Rule
    public final ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Before
    public void registerIdlingResource() {
        // let espresso know to synchronize with background tasks
        IdlingRegistry.getInstance().register(IdlingResourceForEspressoTesting.rootedIdlingResource);
    }

    @After
    public void unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(IdlingResourceForEspressoTesting.rootedIdlingResource);
    }


    @Test
    public void rooted() {

        Rooted.rooted(mActivityTestRule);


    }


}