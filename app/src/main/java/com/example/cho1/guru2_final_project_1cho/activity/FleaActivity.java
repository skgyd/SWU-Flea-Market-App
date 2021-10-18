package com.example.cho1.guru2_final_project_1cho.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.cho1.guru2_final_project_1cho.R;
import com.example.cho1.guru2_final_project_1cho.fragment.FragmentBuy;
import com.example.cho1.guru2_final_project_1cho.fragment.FragmentSell;
import com.google.android.material.tabs.TabLayout;

public class FleaActivity extends AppCompatActivity {

    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private ViewPagerAdapter mViewPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flea);
        mTabLayout = findViewById(R.id.tabLayout);
        mViewPager = findViewById(R.id.viewPager);

        //탭 생성
        mTabLayout.addTab(mTabLayout.newTab().setText("사주세요"));
        mTabLayout.addTab(mTabLayout.newTab().setText("팔아주세요"));
        mTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        //ViewPager 생성
        mViewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), mTabLayout.getTabCount());
        //Tab이랑 viewpager랑 연결
        mViewPager.setAdapter(mViewPagerAdapter);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        mTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }
            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });

        /*//글등록 버튼 눌러 페이지 이동
        Button mbtnOk = findViewById(R.id.btnOk);

        mbtnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), BuyWriteActivity.class);
                startActivity(i);
            }
        });*/
    }  //end onCreate

    static class ViewPagerAdapter extends FragmentPagerAdapter {

        private int tabCount;

        public ViewPagerAdapter(FragmentManager fm, int count){
            super(fm);
            this.tabCount = count;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    return new FragmentBuy();
                case 1:
                    return new FragmentSell();
            }
            return null;
        }

        @Override
        public int getCount() { return tabCount; } //실수하면 안됨! 만들어 놓은걸로 바꿔야 함


    }
}
