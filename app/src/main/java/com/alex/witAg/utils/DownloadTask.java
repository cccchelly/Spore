package com.alex.witAg.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.PowerManager;
import android.support.v7.app.AlertDialog;

import com.alex.witAg.ui.activity.MainActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 下载应用
 *
 * @author Administrator
 */
class DownloadTask extends AsyncTask<String, Integer, String> {
    private AlertDialog progressDialog;
    private static final String DOWNLOAD_NAME = "myapp.apk";
    private Context context;
    private PowerManager.WakeLock mWakeLock;

    public DownloadTask(Context context) {
        this.context = context;
    }

    @Override
    protected String doInBackground(String... sUrl) {
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        File file = null;
        try {
            URL url = new URL(sUrl[0]);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            // expect HTTP 200 OK, so we don't mistakenly save error
            // report
            // instead of the file
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return "Server returned HTTP "
                        + connection.getResponseCode() + " "
                        + connection.getResponseMessage();
            }
            // this will be useful to display download percentage
            // might be -1: server did not report the length
            int fileLength = connection.getContentLength();
            if (Environment.getExternalStorageState().equals(
                    Environment.MEDIA_MOUNTED)) {
                file = new File(Environment.getExternalStorageDirectory(),
                        DOWNLOAD_NAME);

                if (!file.exists()) {
                    // 判断父文件夹是否存在
                    if (!file.getParentFile().exists()) {
                        file.getParentFile().mkdirs();
                    }
                }

            } else {
                ToastUtils.showToast( "sd卡未挂载");
            }
            input = connection.getInputStream();
            output = new FileOutputStream(file);
            byte data[] = new byte[4096];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                // allow canceling with back button
                if (isCancelled()) {
                    input.close();
                    return null;
                }
                total += count;
                // publishing the progress....
                if (fileLength > 0) // only if total length is known
                    publishProgress((int) (total * 100 / fileLength));
                output.write(data, 0, count);

            }
        } catch (Exception e) {
            System.out.println(e.toString());
            return e.toString();

        } finally {
            try {
                if (output != null)
                    output.close();
                if (input != null)
                    input.close();
            } catch (IOException ignored) {
            }
            if (connection != null)
                connection.disconnect();
        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // take CPU lock to prevent CPU from going off if the user
        // presses the power button during download
        PowerManager pm = (PowerManager) context
                .getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                getClass().getName());
        mWakeLock.acquire();
        progressDialog = new AlertDialog.Builder(context)
                .setCancelable(true)
                .setTitle("下载中")
                .setMessage("0%")
                .create();
        progressDialog.show();
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);
        // if we get here, length is known, now set indeterminate to false
        progressDialog.setMessage(progress[0]+"%");
    }

    @Override
    protected void onPostExecute(String result) {
        mWakeLock.release();
        progressDialog.dismiss();
        update();
    }

    private void update() {
        //安装应用
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(Environment
                        .getExternalStorageDirectory(), DOWNLOAD_NAME)),
                "application/vnd.android.package-archive");
        context.startActivity(intent);
    }
}