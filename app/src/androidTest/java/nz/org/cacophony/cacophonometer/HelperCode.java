package nz.org.cacophony.cacophonometer;

import android.content.Context;
import android.util.Log;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertEquals;

public class HelperCode {

    public static void signInViaBackDoor(Prefs prefs, Context targetContext){
        // This does not test the normal sigin process, it just sets up app for other tests

        prefs.setUserSignedIn(true);
        prefs.setUsername("timhot");
        prefs.setUsernamePassword("Pppother1");


    }

    public static void useTestServerAndShortRecordings(Prefs prefs, Context targetContext){

        targetContext = getInstrumentation().getTargetContext();
        prefs = new Prefs(targetContext);

        if (!prefs.getUseTestServer()){
            Util.setUseTestServer(targetContext, true);
            prefs.setUseShortRecordings(true);
        }


    }

    public static void signOutUser(Prefs prefs, Context targetContext){

        prefs.setUsername(null);
        prefs.setUsernamePassword(null);
        prefs.setUserSignedIn(false);
    }

    public static void dismissWelcomeDialog(){
        try{
            onView(allOf(withId(android.R.id.button1), withText("OK"))).perform(scrollTo(),click());
        }catch (Exception ex){
            Log.e("HelperCode", ex.getLocalizedMessage());
        }
    }

    public static void signIn(Prefs prefs){



//        try {
//            onView(withId(R.id.btnSignOutUser)).perform(click());
//        } catch (Exception e) {
//            // View not displayed
//        }


        try {

            Thread.sleep(1000); // had to put in sleep, as the GUI was replacing the username after I set it below
            onView(withId(R.id.etUserNameOrEmailInput)).perform(replaceText("timhot"), closeSoftKeyboard());

            onView(withId(R.id.etPasswordInput)).perform(replaceText("Pppother1"), closeSoftKeyboard());
            onView(withId(R.id.btnSignIn)).perform(click());

        }catch (Exception ex){
            Log.e("SignInUser", ex.getLocalizedMessage());
        }



    }


}
