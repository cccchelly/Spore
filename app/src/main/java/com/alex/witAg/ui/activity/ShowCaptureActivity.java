package com.alex.witAg.ui.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.ImageView;

import com.alex.witAg.AppContants;
import com.alex.witAg.R;
import com.alex.witAg.utils.CapturePostUtil;
import com.alex.witAg.utils.FileUtils;
import com.alex.witAg.utils.ToastUtils;
import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

@Route(path = AppContants.ARouterUrl.ShowCaptureActivity)
public class ShowCaptureActivity extends AppCompatActivity {

    @Autowired(name = AppContants.SHOW_PIC_URL_KEY)
    String name;
    @BindView(R.id.show_capture_image)
    ImageView mImage;
    @BindView(R.id.show_capture_btn_post)
    Button mBtnPost;
    File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_capture);
        ButterKnife.bind(this);
        init();
    }


    private void init() {
        ARouter.getInstance().inject(this);
        file = FileUtils.getFileFromSdcard(name);
        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        mImage.setImageBitmap(bitmap);
    }

    @OnClick(R.id.show_capture_btn_post)
    public void onViewClicked() {
        ToastUtils.showToast("开始上传");
        CapturePostUtil.postPic(file,name);
    }
}
