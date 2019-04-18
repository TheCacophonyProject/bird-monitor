package nz.org.cacophony.birdmonitor;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tim Hunt on 28-Nov-18.
 */
class SectionsStatePagerAdapter extends FragmentStatePagerAdapter {

    private int numberOfPages = 1;

    private final List<Fragment> mfragmentList = new ArrayList<>();
    private final List<String> mfragmentTitleList = new ArrayList<>();

    public SectionsStatePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public void addFragment(Fragment fragment, String title) {
        mfragmentList.add(fragment);
        mfragmentTitleList.add(title);
    }

    public void setNumberOfPages(int numberOfPages) {
        this.numberOfPages = numberOfPages;
    }

    @Override
    public Fragment getItem(int position) {
        return mfragmentList.get(position);
    }

    @Override
    public int getCount() {
        return numberOfPages;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // http://codetheory.in/android-pagertabstrip-pagertitlestrip-viewpager/
        return mfragmentTitleList.get(position);

    }
}
