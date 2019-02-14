package nz.org.cacophony.cacophonometer;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tim Hunt on 28-Nov-18.
 */
public class SectionsStatePagerAdapter extends FragmentStatePagerAdapter {
    private static final String TAG = "SectionsStatePagerAdapt";

    private int numberOfPages = 1;

private final List<Fragment> mfragmentList = new ArrayList<>();
    private final List<String> mfragmentTitleList = new ArrayList<>();

    public SectionsStatePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public void addFragment(Fragment fragment, String title){
        mfragmentList.add(fragment);
        mfragmentTitleList.add(title);


    }



    public void setNumberOfPages(int numberOfPages){
        this.numberOfPages = numberOfPages;
    }




    @Override
    public Fragment getItem(int position) {
       return mfragmentList.get(position);

    }

    @Override
    public int getCount() {

        //return mfragmentList.size();
        return numberOfPages;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // http://codetheory.in/android-pagertabstrip-pagertitlestrip-viewpager/
        return mfragmentTitleList.get(position);

    }
}
