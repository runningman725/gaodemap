package com.example.utils;

import android.app.Activity;
import android.app.Dialog;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.example.gaodemap.R;

public class CustomDialog {

    private static OnNavMapChooseListener onNavMapChooseListener;

    public static void showBottomDialog(Activity activity){
        final Dialog dialog = new Dialog(activity,R.style.DialogTheme);
        View view = View.inflate(activity, R.layout.dialog_bottom_navmap,null);
        dialog.setContentView(view);
        dialog.setCanceledOnTouchOutside(true);

        Window window = dialog.getWindow();
        window.setGravity(Gravity.BOTTOM);
        window.setWindowAnimations(R.style.main_menu_animStyle);
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.show();

        dialog.findViewById(R.id.tv_gaode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onNavMapChooseListener.chooseMap(Const.GAODEMAP);
                dialog.dismiss();
            }
        });

        dialog.findViewById(R.id.tv_baidu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onNavMapChooseListener.chooseMap(Const.BAIDUMAP);
                dialog.dismiss();
            }
        });

        dialog.findViewById(R.id.tv_tencent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onNavMapChooseListener.chooseMap(Const.TENCENTMAP);
                dialog.dismiss();
            }
        });

        dialog.findViewById(R.id.tv_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

    }

    public static void setNavMapChooseListener(OnNavMapChooseListener onNavMapChooseListener){
        CustomDialog.onNavMapChooseListener = onNavMapChooseListener;
    }

    public interface OnNavMapChooseListener {
        void chooseMap(String map);
    }
}
