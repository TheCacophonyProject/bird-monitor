package nz.org.cacophony.cacophonometer;

import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;


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

//        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
//
//            // This method will be invoked when a new page becomes selected.
//            @Override
//            public void onPageSelected(int position) {
//                if (mViewPager.getCurrentItem() == 5){
//                    Log.e(TAG, "onPageSelected " + mViewPager.getCurrentItem());
//                }
//
//            }
//
//            // This method will be invoked when the current page is scrolled
//            @Override
//            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//                if (mViewPager.getCurrentItem() == 5){
//                    Log.e(TAG, "onPageScrolled " + mViewPager.getCurrentItem());
//                }
//            }
//
//            // Called when the scroll state changes:
//            // SCROLL_STATE_IDLE, SCROLL_STATE_DRAGGING, SCROLL_STATE_SETTLING
//            @Override
//            public void onPageScrollStateChanged(int state) {
//                if (mViewPager.getCurrentItem() == 5){
//                    Log.e(TAG, "onPageScrolled " + mViewPager.getCurrentItem());
//                }
//            }
//        });
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

    public void setNumberOfPagesForNotSigned(){
        mSectionsStatePagerAdapter.setNumberOfPages(3);
        mSectionsStatePagerAdapter.notifyDataSetChanged();
    }

    public void setNumberOfPagesForSignedInNotRegistered(){
        mSectionsStatePagerAdapter.setNumberOfPages(5);
        mSectionsStatePagerAdapter.notifyDataSetChanged();
    }



    public void setNumberOfPagesForRegisterd(){
        mSectionsStatePagerAdapter.setNumberOfPages(7);
        mSectionsStatePagerAdapter.notifyDataSetChanged();
    }



//    public void addLastPages(){
//        mSectionsStatePagerAdapter.addLastPages();
//        mSectionsStatePagerAdapter.notifyDataSetChanged();
//
////        mSectionsStatePagerAdapter.addFragment(new GPSFragment(), getResources().getString(R.string.activity_or_fragment_title_gps_location));
////        mSectionsStatePagerAdapter.addFragment(new TestRecordFragment(),  getResources().getString(R.string.activity_or_fragment_title_test_record));
////        mSectionsStatePagerAdapter.notifyDataSetChanged();
//    }

//    public void removePages(){
//        mSectionsStatePagerAdapter.removeLastPages();
//        mSectionsStatePagerAdapter.notifyDataSetChanged();
//
////        // Check that the pages are there before trying to remove them - this situation shouldn't occur
////        int numberOfPages = mSectionsStatePagerAdapter.getCount();
////        if (numberOfPages == 7 ){
////            mSectionsStatePagerAdapter.removeLastPages();
////            mSectionsStatePagerAdapter.notifyDataSetChanged();
////        }
//    }



    private void setupViewPager(ViewPager viewPager){

        mSectionsStatePagerAdapter.addFragment(new WelcomeFragment(), getResources().getString(R.string.activity_or_fragment_title_welcome));
        Prefs prefs = new Prefs(this);

        mSectionsStatePagerAdapter.addFragment(new CreateAccountFragment(), getResources().getString(R.string.activity_or_fragment_title_create_account));
        mSectionsStatePagerAdapter.addFragment(new SignInFragment(), getResources().getString(R.string.activity_or_fragment_title_sign_in));
        mSectionsStatePagerAdapter.addFragment(new GroupsFragment(), getResources().getString(R.string.activity_or_fragment_title_create_or_choose_group));
        mSectionsStatePagerAdapter.addFragment(new RegisterFragment(), getResources().getString(R.string.activity_or_fragment_title_register_phone));

       // if (prefs.getGroupName() != null){
            mSectionsStatePagerAdapter.addFragment(new GPSFragment(), getResources().getString(R.string.activity_or_fragment_title_gps_location));
            mSectionsStatePagerAdapter.addFragment(new TestRecordFragment(),  getResources().getString(R.string.activity_or_fragment_title_test_record));
      //  }

        viewPager.setAdapter(mSectionsStatePagerAdapter);

        boolean signedIn = prefs.getUserSignedIn();
        boolean registered = false;
        if (prefs.getGroupName() != null) {
            registered = true;
        }

        if (!signedIn){
            setNumberOfPagesForNotSigned();
            mSectionsStatePagerAdapter.notifyDataSetChanged();
        }else if (!registered){
            setNumberOfPagesForSignedInNotRegistered();
        }else{
            setNumberOfPagesForRegisterd();
        }

//        if (prefs.getGroupName() == null) {
//            removePages();
//        }

    }

//    public void setPageView(int page){
//        mViewPager.setCurrentItem(page);
//    }


    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }


}
