package nz.org.cacophony.cacophonometer;

import android.content.Intent;
import android.support.v4.view.ViewPager;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;


public class SetupWizardActivity extends AppCompatActivity {
    // https://www.youtube.com/watch?v=UqtsyhASW74

    private static final String TAG = "SetupWizardActivity";

    private SectionsStatePagerAdapter mSectionsStatePagerAdapter;
    private ViewPager mViewPager;

    private String group = ""; // to be used to pass the name of the selected group between fragments

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_wizard);

        //https://www.youtube.com/watch?v=UqtsyhASW74
        mSectionsStatePagerAdapter = new SectionsStatePagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.container);

        setupViewPager(mViewPager);
    }



    public void previousPageView(){
        int currentItem = mViewPager.getCurrentItem();
        currentItem--;
        if (currentItem >= 0 ){
            mViewPager.setCurrentItem(currentItem);
        }else{
            finish();
        }
    }

    public void nextPageView(){
        int currentItem = mViewPager.getCurrentItem();
        currentItem++;
        if (currentItem < mViewPager.getAdapter().getCount()){
            mViewPager.setCurrentItem(currentItem);
        }else{
            finish();
        }
    }

    private void setupViewPager(ViewPager viewPager){
        SectionsStatePagerAdapter adapter = new SectionsStatePagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new WelcomeFragment(), "Welcome");
        adapter.addFragment(new CreateAccountFragment(), "Create a Cacophony Account");
        adapter.addFragment(new SignInFragment(), "Sign in to your Cacophony Account");
        adapter.addFragment(new GroupsFragment(), "Create or choose a Group");
        adapter.addFragment(new RegisterFragment(), "Register Phone");
        adapter.addFragment(new GPSFragment(), "GPS Location");
        adapter.addFragment(new TestRecordFragment(), "Test Record");
        viewPager.setAdapter(adapter);
    }

    public void setPageView(int page){
        mViewPager.setCurrentItem(page);
    }


    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }
}
