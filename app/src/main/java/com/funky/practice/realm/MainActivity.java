package com.funky.practice.realm;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

	@BindView(R.id.toolbar)
	Toolbar mToolbar;
	@BindView(R.id.fab)
	FloatingActionButton mFab;
	@BindView(R.id.pager)
	ViewPager mViewPager;

	PagerAdapter mPagerAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
		ButterKnife.bind(this);
		setSupportActionBar(mToolbar);

		mFab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Fragment fragment = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" + mViewPager.getCurrentItem());
				if (fragment instanceof OnClickFabListener) {
					((OnClickFabListener) fragment).onClickFab();
				} else {
					Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
							.setAction("Action", null).show();
				}
			}
		});
		mPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
			@Override
			public int getCount() {
				return 2;
			}

			@Override
			public Fragment getItem(int position) {
				if (position == 0) {
					return new RealmFragment();
				} else if (position == 1) {
					return new RecyclerViewFragment();
				}
				return null;
			}

			@Override
			public CharSequence getPageTitle(int position) {
				if (position == 1) {
					return "RecyclerView";
				}
				return getString(R.string.app_name);
			}
		};
		mViewPager.setAdapter(mPagerAdapter);
		mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
				mFab.setImageResource(position == 0 ? android.R.drawable.ic_dialog_alert : android.R.drawable.ic_dialog_info);
			}

			@Override
			public void onPageSelected(int position) {

			}

			@Override
			public void onPageScrollStateChanged(int state) {
				mFab.setVisibility(state == ViewPager.SCROLL_STATE_IDLE ? View.VISIBLE : View.GONE);
			}
		});
	}

	public interface OnClickFabListener {
		void onClickFab();
	}
}
