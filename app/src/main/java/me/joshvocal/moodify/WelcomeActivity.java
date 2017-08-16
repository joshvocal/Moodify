package me.joshvocal.moodify;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import static android.view.View.GONE;

/**
 * Welcome Activity shows an introduction to Moodify when the user launches the app for the
 * first time.
 */

public class WelcomeActivity extends AppCompatActivity implements
        View.OnClickListener, ViewPager.OnPageChangeListener {

    private ViewPager mViewPager;
    private MyViewPagerAdapter mMyViewPagerAdapter;
    private LinearLayout mDotsLinearLayout;
    private TextView[] mDotsTextView;
    private int[] mLayouts;
    private Button mSkipButton;
    private Button mNextButton;
    private PrefManager mPrefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isFirstTimeLaunch();
        setNotificationBarTransparent();
        setContentView(R.layout.activity_welcome);
        setLayouts();
        setBottomDots(0);
        setStatusBarColor();

        mMyViewPagerAdapter = new MyViewPagerAdapter();
        mViewPager.setAdapter(mMyViewPagerAdapter);
        mViewPager.addOnPageChangeListener(this);
    }


    private void isFirstTimeLaunch() {
        // Checking for first time launch before calling setContentView()
        mPrefManager = new PrefManager(this);

        // If the user has already launched the application, skip the welcome screen.
        if (!mPrefManager.isFirstTimeLaunch()) {
            launchHomeScreen();
            finish();
        }
    }


    private void setNotificationBarTransparent() {
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
    }


    private void setLayouts() {
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mDotsLinearLayout = (LinearLayout) findViewById(R.id.layoutDots);

        mSkipButton = (Button) findViewById(R.id.btn_skip);
        mSkipButton.setOnClickListener(this);

        mNextButton = (Button) findViewById(R.id.btn_next);
        mNextButton.setOnClickListener(this);

        // Layouts of all welcome sliders
        mLayouts = new int[]{
                R.layout.welcome_slide1,
                R.layout.welcome_slide2,
                R.layout.welcome_slide3};
    }


    private void launchHomeScreen() {
        mPrefManager.setIsFirstTimeLaunch(false);
        startActivity(new Intent(this, SignInActivity.class));
        finish();
    }


    private void setBottomDots(int currentPage) {
        mDotsTextView = new TextView[mLayouts.length];

        int[] colorsActive = getResources().getIntArray(R.array.array_dot_active);
        int[] colorsInactive = getResources().getIntArray(R.array.array_dot_inactive);

        mDotsLinearLayout.removeAllViews();

        for (int i = 0; i < mDotsTextView.length; i++) {
            mDotsTextView[i] = new TextView(this);
            mDotsTextView[i].setText(Html.fromHtml("&#8226;"));
            mDotsTextView[i].setTextSize(35);
            mDotsTextView[i].setTextColor(colorsInactive[currentPage]);
            mDotsLinearLayout.addView(mDotsTextView[i]);
        }

        if (mDotsTextView.length > 0) {
            mDotsTextView[currentPage].setTextColor(colorsActive[currentPage]);
        }
    }


    private void setStatusBarColor() {
        // Make notification bar transparent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }


    private int getItem(int position) {
        return mViewPager.getCurrentItem() + position;
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_next:
                // Check for the last page
                // If the last page, home screen will be launched
                int current = getItem(1);
                if (current < mLayouts.length) {
                    // Move to the next screen
                    mViewPager.setCurrentItem(current);
                } else {
                    launchHomeScreen();
                }
                break;
            case R.id.btn_skip:
                launchHomeScreen();
                break;
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        // Empty
    }

    @Override
    public void onPageSelected(int position) {
        setBottomDots(position);

        // Changing the next button text
        if (position == mLayouts.length - 1) {
            // Last page, make button text different
            mNextButton.setText(getString(R.string.start));
            mSkipButton.setVisibility(GONE);
        } else {
            // There are still pages left
            mNextButton.setText(getString(R.string.next));
            mSkipButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        // Empty
    }


    /**
     * ViewPager adapter
     */

    public class MyViewPagerAdapter extends PagerAdapter {

        private LayoutInflater mLayoutInflater;

        public MyViewPagerAdapter() {
            // Empty constructor.
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            mLayoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = mLayoutInflater.inflate(mLayouts[position], container, false);
            container.addView(view);
            return view;
        }

        @Override
        public int getCount() {
            return mLayouts.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = (View) object;
            container.removeView(view);
        }
    }
}
