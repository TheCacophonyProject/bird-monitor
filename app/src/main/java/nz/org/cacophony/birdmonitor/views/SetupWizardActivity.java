package nz.org.cacophony.birdmonitor.views;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import nz.org.cacophony.birdmonitor.Prefs;
import nz.org.cacophony.birdmonitor.R;
import nz.org.cacophony.birdmonitor.SectionsStatePagerAdapter;
import nz.org.cacophony.birdmonitor.Util;

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
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);


        //https://www.youtube.com/watch?v=UqtsyhASW74
        mSectionsStatePagerAdapter = new SectionsStatePagerAdapter(getSupportFragmentManager());
        mViewPager = findViewById(R.id.container);

        setupViewPager(mViewPager);

        final Prefs prefs = new Prefs(this.getApplicationContext());
        if (prefs.getDeviceName() == null) {
            Util.displayHelp(this, getResources().getString(R.string.app_icon_name));
        }
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
            // Set the toolbar title

        } else {
            finish();
        }
    }

    public void setNumberOfPagesForNotSigned() {
        mSectionsStatePagerAdapter.setNumberOfPages(3);
        mSectionsStatePagerAdapter.notifyDataSetChanged();
    }

    public void setNumberOfPagesForSignedInNotRegistered() {
        mSectionsStatePagerAdapter.setNumberOfPages(5);
        mSectionsStatePagerAdapter.notifyDataSetChanged();
    }

    public void setNumberOfPagesForRegisterd() {
        mSectionsStatePagerAdapter.setNumberOfPages(7);
        mSectionsStatePagerAdapter.notifyDataSetChanged();
    }

    private void setupViewPager(ViewPager viewPager) {

        mSectionsStatePagerAdapter.addFragment(new WelcomeFragment(), getResources().getString(R.string.activity_or_fragment_title_welcome));
        Prefs prefs = new Prefs(this);

        mSectionsStatePagerAdapter.addFragment(new CreateAccountFragment(), getResources().getString(R.string.activity_or_fragment_title_create_account));
        mSectionsStatePagerAdapter.addFragment(new SignInFragment(), getResources().getString(R.string.activity_or_fragment_title_sign_in));
        mSectionsStatePagerAdapter.addFragment(new GroupsFragment(), getResources().getString(R.string.activity_or_fragment_title_create_or_choose_group));
        mSectionsStatePagerAdapter.addFragment(new RegisterFragment(), getResources().getString(R.string.activity_or_fragment_title_register_phone));

        mSectionsStatePagerAdapter.addFragment(new GPSFragment(), getResources().getString(R.string.activity_or_fragment_title_gps_location));
        mSectionsStatePagerAdapter.addFragment(new TestRecordFragment(), getResources().getString(R.string.activity_or_fragment_title_test_record));

        viewPager.setAdapter(mSectionsStatePagerAdapter);

        boolean signedIn = prefs.getUserSignedIn();
        boolean registered = false;
        if (prefs.getGroupName() != null) {
            registered = true;
        }

        if (!signedIn) {
            setNumberOfPagesForNotSigned();
        } else if (!registered) {
            setNumberOfPagesForSignedInNotRegistered();
        } else {
            setNumberOfPagesForRegisterd();
        }
    }

    public String getGroup() {
        if (group != null) {
            if (group.equals("")) {
                group = null;
            }
        }
        return group;
    }

    public void setGroup(String group) {
        if (group != null) {
            if (group.equals("")) {
                group = null;
            }
        }
        this.group = group;
    }

    public void displayOKDialogMessage(String title, String messageToDisplay) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Add the buttons
        builder.setPositiveButton("OK", (dialog, id) -> {
        });

        builder.setMessage(messageToDisplay)
                .setTitle(title);

        final AlertDialog dialog = builder.create();

        dialog.setOnShowListener(dialogInterface -> {

            Button btnPositive = dialog.getButton(Dialog.BUTTON_POSITIVE);
            btnPositive.setTextSize(24);
            int btnPositiveColor = ResourcesCompat.getColor(getResources(), R.color.dialogButtonText, null);
            btnPositive.setTextColor(btnPositiveColor);

            //https://stackoverflow.com/questions/6562924/changing-font-size-into-an-alertdialog
            TextView textView = dialog.findViewById(android.R.id.message);
            textView.setTextSize(22);
        });

        dialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        // https://stackoverflow.com/questions/35989288/onrequestpermissionsresult-not-being-called-in-fragment-if-defined-in-both-fragm
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


}
