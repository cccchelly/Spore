package com.alex.witAg.utils;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.alex.witAg.R;

/**
 * Created by Administrator on 2018-07-30.
 */

public class DialogDelete {
    AlertDialog dialog;
    TextView tvCancle;
    TextView tvSure;
    OnSureListener onSure;

    public DialogDelete(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.delete_dialog_layout,null);
        dialog = new AlertDialog.Builder(context,R.style.quick_option_dialog).create();
        dialog.show();
        Window window = dialog.getWindow();
        window.setContentView(view);
        dialog.setCanceledOnTouchOutside(true);
        window.getDecorView().setPadding(0, 0, 0, 0);
        //window.setWindowAnimations(R.style.PopupWindowAnimation);  //添加动画
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);
        window.setGravity(Gravity.BOTTOM);  //此处可以设置dialog显示的位置
        window.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        dialog.dismiss();

         tvCancle = (TextView) view.findViewById(R.id.delete_dialog_tv_sure);
         tvSure = (TextView) view.findViewById(R.id.delete_dialog_tv_cancle);

         tvCancle.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 dialog.dismiss();
             }
         });
         tvSure.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 if (onSure != null){
                     onSure.getInputContent();
                 }
             }
         });


    }

    public void show(){
        dialog.show();
    }

    public void disMiss(){
        dialog.dismiss();
    }

    public void setOnSureListener(OnSureListener onInputSure){
        this.onSure = onInputSure;
    }

    public interface OnSureListener {
        void getInputContent();
    }


}
