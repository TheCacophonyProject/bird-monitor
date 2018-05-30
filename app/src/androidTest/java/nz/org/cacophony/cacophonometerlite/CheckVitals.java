package nz.org.cacophony.cacophonometerlite;

import android.view.View;
import android.widget.TextView;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;


class CheckVitals {


    public static void checkVitals(boolean testServer) {



        HelperCode.openVitalsActivity();

        onView(withId(R.id.appPermissionText)).check(matches(withText(R.string.required_permissions_true)));

        onView(withId(R.id.mainRegisteredStatus)).perform(scrollTo()).check(matches(withText(R.string.registered_true)));

        onView(withId(R.id.loggedInText)).check(matches(withText(R.string.logged_in_to_server_true)));

        // Check that Device ID message is appropriate ie has a device ID and says Test Server (or no if production server)
        onView(withId(R.id.deviceIDText)).check(matches(isDeviceIdOK(testServer)));

//        try {
//            Thread.sleep(5000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

    }






    private static Matcher<View> isDeviceIdOK(final boolean testServer) {
        // https://www.programcreek.com/java-api-examples/?code=kevalpatel2106/smart-lens/smart-lens-master/app/src/androidTest/java/com/kevalpatel2106/smartlens/testUtils/CustomMatchers.java
        // http://blog.sqisland.com/2016/06/advanced-espresso-at-io16.html

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("is Device Id OK ");

            }

            @Override
            public boolean matchesSafely(View view) {
                if (!(view instanceof TextView)){
                    return false;
                }
                String deviceIdText = (String)((TextView)view).getText();
                if (testServer){
                    boolean hasTextServerInText = deviceIdText.contains("Test Server");
                    //noinspection SimplifiableIfStatement
                    if (!hasTextServerInText){
                        return false;
                    }
                    return  ((TextView)view).getText().length() > 23;  // if > 11 characters it means there is a device id displayed

                }else{
                    return  ((TextView)view).getText().length() > 11;  // if > 11 characters it means there is a device id displayed
                }




            }
        };
    }


}
