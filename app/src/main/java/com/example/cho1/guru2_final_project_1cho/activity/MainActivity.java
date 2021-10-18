package com.example.cho1.guru2_final_project_1cho.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.cho1.guru2_final_project_1cho.R;
import com.example.cho1.guru2_final_project_1cho.bean.MemberBean;
import com.example.cho1.guru2_final_project_1cho.db.FileDB;
import com.example.cho1.guru2_final_project_1cho.fragment.FragmentEx;
import com.example.cho1.guru2_final_project_1cho.fragment.FragmentFlea;
import com.example.cho1.guru2_final_project_1cho.fragment.FragmentFree;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase mFirebaseDB = FirebaseDatabase.getInstance();
    private TabLayout mTabLayout;
    private ViewPager mViewPager;

    private Context mContext;

    private MemberBean loginMember;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = getApplicationContext();

        loginMember = FileDB.getLoginMember(this);
        //googleLoginFlag = getIntent().getBooleanExtra("googleLogin", false);

        TextView txtUserID = findViewById(R.id.txtUserID);

        mTabLayout = findViewById(R.id.tabLayout);
        mViewPager = findViewById(R.id.viewPager);

        // 탭 생성
        //mTabLayout.addTab(mTabLayout.newTab().setText("플리마켓").setIcon(R.drawable.cart2));
        mTabLayout.addTab(mTabLayout.newTab().setCustomView(createTabView("플리마켓")));
        mTabLayout.addTab(mTabLayout.newTab().setCustomView(createTabView2("물물교환")));
        mTabLayout.addTab(mTabLayout.newTab().setCustomView(createTabView3("무료나눔")));
        mTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);


        // ViewPager 생성
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager(), mTabLayout.getTabCount());
        // 탭과 뷰페이저를 서로 연결
        mViewPager.setAdapter(adapter);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });

        txtUserID.setText(mFirebaseAuth.getCurrentUser().getEmail());

        LinearLayout linearLayout = findViewById(R.id.linearLayout);
        linearLayout.setOnClickListener(mClicks);
    } // end onCreate()

    //커스텀 탭바 적용
    private View createTabView(String tabName) {
        View tabView = LayoutInflater.from(mContext).inflate(R.layout.custom_tab, null);
        TextView txtName = (TextView) tabView.findViewById(R.id.txtName);
        txtName.setText(tabName);
        return tabView;
    }
    //커스텀 탭바2 적용
    private View createTabView2(String tabName) {
        View tabView = LayoutInflater.from(mContext).inflate(R.layout.custom_tab2, null);
        TextView txtName = (TextView) tabView.findViewById(R.id.txtName);
        txtName.setText(tabName);
        return tabView;
    }
    //커스텀 탭바3 적용
    private View createTabView3(String tabName) {
        View tabView = LayoutInflater.from(mContext).inflate(R.layout.custom_tab3, null);
        TextView txtName = (TextView) tabView.findViewById(R.id.txtName);
        txtName.setText(tabName);
        return tabView;
    }

//
//    public void onTabSelected(TabLayout.Tab tab)
//    {
//        int tabIconColor = ContextCompat.getColor(mContext, R.color.colorAccent);
//        ImageView imageView = (ImageView)tab.getCustomView().findViewById(R.id.imgTab);
//        imageView.getBackground().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
//    }
//
//
//    public void onTabUnselected(TabLayout.Tab tab)
//    {
//        int tabIconColor = ContextCompat.getColor(mContext, R.color.white_color);
//        ImageView imageView = (ImageView)tab.getCustomView().findViewById(R.id.imgTab);
//        imageView.getBackground().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
//    }


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
                    return new FragmentFlea();
                case 1:
                    return new FragmentEx();
                case 2:
                    return new FragmentFree();
            }
            return null;
        }

        @Override
        public int getCount() {
            return tabCount;
        }
    } //end class ViewPagerAdapter

    private long backPressedAt;

    @Override
    public void onBackPressed() {
        if (backPressedAt + TimeUnit.SECONDS.toMillis(2) > System.currentTimeMillis()) {
            super.onBackPressed();
            finish();
        }
        else {
            if(this instanceof MainActivity) {
                Toast.makeText(this, "한번더 뒤로가기 클릭시 앱을 종료 합니다.", Toast.LENGTH_SHORT).show();
                backPressedAt = System.currentTimeMillis();
            } else {
                super.onBackPressed();
            }
        }
    }

    private View.OnClickListener mClicks = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.linearLayout:
                    Intent i = new Intent(MainActivity.this, ModifyMemberActivity.class);
                    startActivity(i);
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        loginMember = FileDB.getLoginMember(this);
    }
}
