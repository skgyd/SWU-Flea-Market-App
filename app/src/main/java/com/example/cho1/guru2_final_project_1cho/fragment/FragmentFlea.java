package com.example.cho1.guru2_final_project_1cho.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.cho1.guru2_final_project_1cho.R;
import com.example.cho1.guru2_final_project_1cho.activity.FleaActivity;

public class FragmentFlea extends Fragment {


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_flea, container, false);

        view.findViewById(R.id.btnClothes).setOnClickListener(mCLicks);
        view.findViewById(R.id.btnBook).setOnClickListener(mCLicks);
        view.findViewById(R.id.btnLife).setOnClickListener(mCLicks);
        view.findViewById(R.id.btnGifticon).setOnClickListener(mCLicks);
        view.findViewById(R.id.btnData).setOnClickListener(mCLicks);
        view.findViewById(R.id.btnTicket).setOnClickListener(mCLicks);
        view.findViewById(R.id.btnElecDevice).setOnClickListener(mCLicks);
        view.findViewById(R.id.btnCosmetic).setOnClickListener(mCLicks);
        view.findViewById(R.id.btnEtc).setOnClickListener(mCLicks);

        return view;
    }

    private View.OnClickListener mCLicks = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent i = new Intent(getActivity(), FleaActivity.class);
            switch(view.getId()) {
                case R.id.btnClothes:
                    i.putExtra("CATEGORY", "옷");
                    break;
                case R.id.btnBook:
                    i.putExtra("CATEGORY", "책");
                    break;
                case R.id.btnLife:
                    i.putExtra("CATEGORY", "생활용품");
                    break;
                case R.id.btnGifticon:
                    i.putExtra("CATEGORY", "기프티콘");
                    break;
                case R.id.btnData:
                    i.putExtra("CATEGORY", "데이터");
                    break;
                case R.id.btnTicket:
                    i.putExtra("CATEGORY", "대리예매");
                    break;
                case R.id.btnElecDevice:
                    i.putExtra("CATEGORY", "전자기기");
                    break;
                case R.id.btnCosmetic:
                    i.putExtra("CATEGORY", "화장품");
                    break;
                case R.id.btnEtc:
                    i.putExtra("CATEGORY", "기타");
                    break;
            }
            startActivity(i);
        }
    };
}
