package com.example.cho1.guru2_final_project_1cho.firebase;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cho1.guru2_final_project_1cho.R;
import com.example.cho1.guru2_final_project_1cho.activity.JoinActivity;
import com.example.cho1.guru2_final_project_1cho.bean.CommentBean;
import com.example.cho1.guru2_final_project_1cho.bean.ExBean;
import com.example.cho1.guru2_final_project_1cho.bean.FleaBean;
import com.example.cho1.guru2_final_project_1cho.bean.FreeBean;
import com.example.cho1.guru2_final_project_1cho.bean.MemberBean;
import com.example.cho1.guru2_final_project_1cho.db.FileDB;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;
import java.util.StringTokenizer;

public class CommentAdapter extends BaseAdapter {
    private Context mContext;
    private List<CommentBean> mCommentList;
    private CommentBean mCommentBean;
    private static FleaBean mFleaBean;
    private static ExBean mExBean;
    private static FreeBean mFreeBean;
    private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();

    public CommentAdapter(Context context, List<CommentBean> commentList) {
        mContext = context;
        mCommentList = commentList;
    }

    public static void setFleaBean(FleaBean fleaBean) {
        mFleaBean = fleaBean;
    }

    public static void setExBean(ExBean exBean) {
        mExBean = exBean;
    }

    public static void setFreeBean(FreeBean freeBean) {
        mFreeBean = freeBean;
    }

    public void setList(List<CommentBean> commentList) {
        mCommentList = commentList;
    }

    @Override
    public int getCount() { return mCommentList.size(); }

    @Override
    public Object getItem(int i) { return mCommentList.get(i); }

    @Override
    public long getItemId(int i) { return i; }

    @Override
    public View getView(final int position, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.view_comment_item, null);

        TextView txtComment = view.findViewById(R.id.txtComment);
        TextView txtCommentDate = view.findViewById(R.id.txtCommentDate);
        TextView txtCommentId = view.findViewById(R.id.txtCommentId);
        final ImageView imgCommentDelete = view.findViewById(R.id.imgCommentDelete);

        mCommentBean = mCommentList.get(position);

        txtComment.setText(mCommentBean.comment);
        txtCommentDate.setText(mCommentBean.date);
        StringTokenizer tokens = new StringTokenizer(mCommentBean.userId);
        String userId = tokens.nextToken("@") ;
        txtCommentId.setText(userId);
        Linkify.addLinks(txtComment, Linkify.WEB_URLS);
        if(mFleaBean != null) {
            if (TextUtils.equals(mFleaBean.userId, mCommentBean.userId)) {
                String strColor = "#ff4848";
                txtCommentId.setTextColor(Color.parseColor(strColor));
                txtCommentId.setPaintFlags(txtCommentId.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
            }
        } else if(mExBean != null) {
            if (TextUtils.equals(mExBean.userId, mCommentBean.userId)) {
                String strColor = "#ff4848";
                txtCommentId.setTextColor(Color.parseColor(strColor));
                txtCommentId.setPaintFlags(txtCommentId.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
            }
        }

        imgCommentDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builer = new AlertDialog.Builder(mContext);
                builer.setTitle("삭제");
                builer.setMessage("삭제하시겠습니까?");
                builer.setNegativeButton("아니오", null);
                builer.setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        MemberBean loginMember = FileDB.getLoginMember(mContext);
                        mCommentBean = mCommentList.get(position);
                        if(TextUtils.equals(mFirebaseAuth.getCurrentUser().getEmail(), mCommentBean.userId)) {
                            if (mCommentBean.flag == 1) {
                                String guid = JoinActivity.getUserIdFromUUID(mFleaBean.userId);
                                FirebaseDatabase.getInstance().getReference().child("buy").child(mFleaBean.id).child("comments").child(mCommentBean.id).removeValue();
                                notifyDataSetChanged();
                            } else if (mCommentBean.flag == 2) {
                                String guid = JoinActivity.getUserIdFromUUID(mFleaBean.userId);
                                FirebaseDatabase.getInstance().getReference().child("sell").child(mFleaBean.id).child("comments").child(mCommentBean.id).removeValue();
                                notifyDataSetChanged();
                            } else if (mCommentBean.flag == 3) {
                                String guid = JoinActivity.getUserIdFromUUID(mExBean.userId);
                                FirebaseDatabase.getInstance().getReference().child("ex").child(mExBean.id).child("comments").child(mCommentBean.id).removeValue();
                                notifyDataSetChanged();
                            } else if (mCommentBean.flag == 4) {
                                String guid = JoinActivity.getUserIdFromUUID(mFreeBean.userId);
                                FirebaseDatabase.getInstance().getReference().child("free").child(mFreeBean.id).child("comments").child(mCommentBean.id).removeValue();
                                notifyDataSetChanged();
                            }
                            Toast.makeText(mContext, "삭제 되었습니다", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(mContext, "본인의 댓글만 삭제할 수 있습니다", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                builer.create().show(); // 다이어로그 나타남
            }
        });

        return view;
    }
}
