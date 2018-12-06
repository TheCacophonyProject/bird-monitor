package nz.org.cacophony.cacophonometer;

import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

public class AdvancedWizardActivity extends AppCompatActivity {
    private static final String TAG = "AdvancedWizardActivity";

    private SectionsStatePagerAdapter mSectionsStatePagerAdapter;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advance_wizard);

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

        mSectionsStatePagerAdapter.addFragment(new ManageRecordingsFragment(), getResources().getString(R.string.activity_or_fragment_title_manage_recordings));
        mSectionsStatePagerAdapter.addFragment(new InternetConnectionFragment(), getResources().getString(R.string.activity_or_fragment_title_internet_connection));
        mSectionsStatePagerAdapter.addFragment(new SoundFragment(), getResources().getString(R.string.activity_or_fragment_title_warning_sound));
        mSectionsStatePagerAdapter.addFragment(new BatteryFragment(), getResources().getString(R.string.activity_or_fragment_title_activity_ignore_low_battery));
        mSectionsStatePagerAdapter.addFragment(new FrequencyFragment(), getResources().getString(R.string.activity_or_fragment_title_activity_frequency));
        mSectionsStatePagerAdapter.addFragment(new RootedFragment(), getResources().getString(R.string.activity_or_fragment_title_rooted));

        Prefs prefs = new Prefs(this);
        if (prefs.getSettingsForTestServerEnabled()){
            mSectionsStatePagerAdapter.addFragment(new TestingFragment(),  getResources().getString(R.string.activity_or_fragment_title_settings_for_testing));
        }

        viewPager.setAdapter(mSectionsStatePagerAdapter);

    }



}
