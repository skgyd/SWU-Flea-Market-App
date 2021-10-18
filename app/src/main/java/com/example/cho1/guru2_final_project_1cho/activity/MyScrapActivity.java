package com.example.cho1.guru2_final_project_1cho.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;

import com.example.cho1.guru2_final_project_1cho.R;
import com.example.cho1.guru2_final_project_1cho.fragment.FragmentMyBuy;
import com.example.cho1.guru2_final_project_1cho.fragment.FragmentMyBuyScrap;
import com.example.cho1.guru2_final_project_1cho.fragment.FragmentMyEx;
import com.example.cho1.guru2_final_project_1cho.fragment.FragmentMyExScrap;
import com.example.cho1.guru2_final_project_1cho.fragment.FragmentMyFree;
import com.example.cho1.guru2_final_project_1cho.fragment.FragmentMyFreeScrap;
import com.example.cho1.guru2_final_project_1cho.fragment.FragmentMySell;
import com.example.cho1.guru2_final_project_1cho.fragment.FragmentMySellScrap;
import com.google.android.material.tabs.TabLayout;

public class MyScrapActivity extends AppCompatActivity {

    private TabLayout mTabMyBoard;
    private ViewPager mViewMyBoard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_scrap);

        mTabMyBoard = findViewById(R.id.tabMyScrap);
        mViewMyBoard = findViewById(R.id.viewrMyScrap);

        // 탭 생성
        mTabMyBoard.addTab(mTabMyBoard.newTab().setText("사주세요"));
        mTabMyBoard.addTab(mTabMyBoard.newTab().setText("팔아주세요"));
        mTabMyBoard.addTab(mTabMyBoard.newTab().setText("물물교환"));
        mTabMyBoard.addTab(mTabMyBoard.newTab().setText("무료나눔"));
        mTabMyBoard.setTabGravity(TabLayout.GRAVITY_FILL);

// ViewPager 생성
        ViewPagerAdapter adapter =
                new ViewPagerAdapter(getSupportFragmentManager(), mTabMyBoard.getTabCount());

        // 탭과 뷰페이저를 서로 연결
        mViewMyBoard.setAdapter(adapter);
        mViewMyBoard.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabMyBoard));
        mTabMyBoard.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewMyBoard.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private int tabCount;

        public ViewPagerAdapter(FragmentManager fm, int count) {
            super(fm);
            this.tabCount = count;
        }

        @Override
        public Fragment getItem(int position) {
            switch(position) {
                case 0:
                    return new FragmentMyBuyScrap();
                case 1:
                    return new FragmentMySellScrap();
                case 2:
                    return new FragmentMyExScrap();
                case 3:
                    return new FragmentMyFreeScrap();
            }
            return null;
        }

        @Override
        public int getCount() {
            return tabCount;
        }
    } //end class ViewPagerAdapter
}