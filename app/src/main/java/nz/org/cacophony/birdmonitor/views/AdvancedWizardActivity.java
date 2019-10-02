package nz.org.cacophony.birdmonitor.views;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import nz.org.cacophony.birdmonitor.Prefs;
import nz.org.cacophony.birdmonitor.R;
import nz.org.cacophony.birdmonitor.SectionsStatePagerAdapter;
import nz.org.cacophony.birdmonitor.Util;

public class AdvancedWizardActivity extends AppCompatActivity {

    private static final String TAG = "AdvancedWizardActivity";

    private SectionsStatePagerAdapter mSectionsStatePagerAdapter;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advance_wizard);

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        //https://www.youtube.com/watch?v=UqtsyhASW74
        mSectionsStatePagerAdapter = new SectionsStatePagerAdapter(getSupportFragmentManager());
        mViewPager = findViewById(R.id.container);

        setupViewPager(mViewPager);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
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

    public void nextPageView() {
        int currentItem = mViewPager.getCurrentItem();
        currentItem++;
        if (currentItem < mViewPager.getAdapter().getCount()) {
            mViewPager.setCurrentItem(currentItem);


        } else {
            finish();
        }
    }

    private void setNumberOfPagesForAdvanced() {
        mSectionsStatePagerAdapter.setNumberOfPages(7);
        mSectionsStatePagerAdapter.notifyDataSetChanged();
    }


    private void setNumberOfPagesForVeryAdvanced() {
        mSectionsStatePagerAdapter.setNumberOfPages(mSectionsStatePagerAdapter.getCount());
        mSectionsStatePagerAdapter.notifyDataSetChanged();
    }

    private void setupViewPager(ViewPager viewPager) {

        mSectionsStatePagerAdapter.addFragment(new ManageRecordingsFragment(), getResources().getString(R.string.activity_or_fragment_title_manage_recordings));
        mSectionsStatePagerAdapter.addFragment(new InternetConnectionFragment(), getResources().getString(R.string.activity_or_fragment_title_internet_connection));
        mSectionsStatePagerAdapter.addFragment(new SoundFragment(), getResources().getString(R.string.activity_or_fragment_title_warning_sound));
        mSectionsStatePagerAdapter.addFragment(new BatteryFragment(), getResources().getString(R.string.activity_or_fragment_title_activity_ignore_low_battery));
        mSectionsStatePagerAdapter.addFragment(new FrequencyFragment(), getResources().getString(R.string.activity_or_fragment_title_activity_frequency));
        mSectionsStatePagerAdapter.addFragment(new SunriseAlarmFragment(), getResources().getString(R.string.activity_or_fragment_title_activity_sun_alarms));
        mSectionsStatePagerAdapter.addFragment(new RootedFragment(), getResources().getString(R.string.activity_or_fragment_title_rooted));

        // And for Very Advanced
        mSectionsStatePagerAdapter.addFragment(new AudioSourceSettingsFragment(), getResources().getString(R.string.activity_or_fragment_title_settings_for_audio_source));
        mSectionsStatePagerAdapter.addFragment(new TestingFragment(), getResources().getString(R.string.activity_or_fragment_title_settings_for_testing));


        Prefs prefs = new Prefs(this);
        if (prefs.getVeryAdvancedSettingsEnabled()) {
            setNumberOfPagesForVeryAdvanced();
        } else {
            setNumberOfPagesForAdvanced();
        }

        viewPager.setAdapter(mSectionsStatePagerAdapter);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        // https://stackoverflow.com/questions/35989288/onrequestpermissionsresult-not-being-called-in-fragment-if-defined-in-both-fragm
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Need to update the number of recordings displayed in the Managed Recordings Fragment.  Did it hear because for some reason I could not get it to refresh using this method in the ManageRecordingsFragment
        try {
            ((ManageRecordingsFragment) mSectionsStatePagerAdapter.getItem(0)).displayOrHideGUIObjects();
        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage(), ex);
        }
    }


}
