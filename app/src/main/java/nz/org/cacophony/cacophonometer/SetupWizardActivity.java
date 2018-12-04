package nz.org.cacophony.cacophonometer;

import android.support.v4.view.ViewPager;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v7.widget.Toolbar;


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


//https://developer.android.com/training/appbar/setting-up#java
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);


        //https://www.youtube.com/watch?v=UqtsyhASW74
        mSectionsStatePagerAdapter = new SectionsStatePagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.container);

        setupViewPager(mViewPager);
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_help, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.button_help:
                int currentItem = mViewPager.getCurrentItem();
                String fragmentTitle = mSectionsStatePagerAdapter.getPageTitle(currentItem).toString();
                Util.displayHelp(this, fragmentTitle);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
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
            // Set the toolbar title

        }else{
            finish();
        }
    }

    private void setupViewPager(ViewPager viewPager){

        mSectionsStatePagerAdapter.addFragment(new WelcomeFragment(), getResources().getString(R.string.activity_or_fragment_title_welcome));
        mSectionsStatePagerAdapter.addFragment(new CreateAccountFragment(), getResources().getString(R.string.activity_or_fragment_title_create_account));
        mSectionsStatePagerAdapter.addFragment(new SignInFragment(), getResources().getString(R.string.activity_or_fragment_title_sign_in));
        mSectionsStatePagerAdapter.addFragment(new GroupsFragment(), getResources().getString(R.string.activity_or_fragment_title_create_or_choose_group));
        mSectionsStatePagerAdapter.addFragment(new RegisterFragment(), getResources().getString(R.string.activity_or_fragment_title_register_phone));
        mSectionsStatePagerAdapter.addFragment(new GPSFragment(), getResources().getString(R.string.activity_or_fragment_title_gps_location));
        mSectionsStatePagerAdapter.addFragment(new TestRecordFragment(),  getResources().getString(R.string.activity_or_fragment_title_test_record));
        viewPager.setAdapter(mSectionsStatePagerAdapter);

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
