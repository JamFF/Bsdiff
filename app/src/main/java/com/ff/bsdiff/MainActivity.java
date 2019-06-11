package com.ff.bsdiff;

import android.Manifest;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    private static final String TAG = "Bsdiff";
    private static final String TAG_PERMISSION = "Permission";
    private static final int PERMISSION_STORAGE_CODE = 10001;
    private static final String PERMISSION_STORAGE_MSG = "需要SD卡读写权限，否则无法正常使用";
    private static final String[] PERMS = {Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private ApkUpdateTask mApkUpdateTask;

    private ProgressDialog dialog;// 加载框

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button bt_version = findViewById(R.id.bt_update);
        TextView tv_version = findViewById(R.id.tv_version);

        String versionName = APKUtils.getVersionName(this);
        // String versionName = BuildConfig.VERSION_NAME;
        tv_version.setText(versionName);
        if (versionName.equals("1.0")) {
            bt_version.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateApk();
                }
            });
        } else {
            bt_version.setEnabled(false);
        }
    }

    /**
     * Glide方式加载GIF图
     */
    @AfterPermissionGranted(PERMISSION_STORAGE_CODE)
    private void updateApk() {
        if (!EasyPermissions.hasPermissions(this, PERMS)) {
            // 申请权限
            EasyPermissions.requestPermissions(this, PERMISSION_STORAGE_MSG,
                    PERMISSION_STORAGE_CODE, PERMS);
            return;
        }
        showProgressDialog();
        mApkUpdateTask = new ApkUpdateTask(MainActivity.this);
        mApkUpdateTask.execute();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        if (requestCode == PERMISSION_STORAGE_CODE) {
            Log.d(TAG_PERMISSION, "onPermissionsGranted: ");
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            // 拒绝权限，并不再询问
            new AppSettingsDialog
                    .Builder(this)
                    .setTitle("授权提醒")
                    .setRationale(PERMISSION_STORAGE_MSG)
                    .setPositiveButton("打开设置")
                    .setNegativeButton("取消")
                    .build()
                    .show();
        } else {
            // 拒绝权限
            if (requestCode == PERMISSION_STORAGE_CODE) {
                Log.d(TAG_PERMISSION, "onPermissionsDenied: ");
            }
        }
    }

    @Override
    protected void onDestroy() {
        dismissProgressDialog();
        if (mApkUpdateTask != null && mApkUpdateTask.getStatus() == AsyncTask.Status.RUNNING) {
            mApkUpdateTask.cancel(true);
        }
        super.onDestroy();
    }

    private void showProgressDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (dialog == null) {
                    dialog = new ProgressDialog(MainActivity.this);
                    dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                }
                dialog.setMessage("更新中");
                dialog.setCancelable(false);// 点击屏幕和按返回键都不能取消加载框
                dialog.show();
            }
        });
    }

    private void dismissProgressDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
    }

    private static class ApkUpdateTask extends AsyncTask<Void, Void, Boolean> {

        private WeakReference<MainActivity> mReference;

        private ApkUpdateTask(MainActivity activity) {
            mReference = new WeakReference<>(activity);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                // 1.下载差分包
                Log.d(TAG, "开始下载");
                String patchFile = Environment.getExternalStorageDirectory() + File.separator + "patch";
                Log.d(TAG, "patchFile:" + patchFile);

                // 2.获取当前应用的apk文件/data/app/
                String oldFile = APKUtils.getSourceApkPath(mReference.get(), mReference.get().getPackageName());
                Log.d(TAG, "oldFile:" + oldFile);

                // 3.合并得到最新版本的APK文件
                String newFile = Environment.getExternalStorageDirectory() + File.separator + "new.apk";
                BsPatch.patch(oldFile, newFile, patchFile);
                Log.d(TAG, "newFile:" + patchFile);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            // 4.安装
            if (aBoolean && mReference != null && mReference.get() != null) {
                mReference.get().dismissProgressDialog();
                Log.d(TAG, "开始安装");
                // FIXME: 2019-06-11 没有真正适配Android 8.0，需要未知来源权限判断
                APKUtils.installApk(mReference.get(), Environment.
                        getExternalStorageDirectory() + File.separator + "new.apk");
            }
        }

        @Override
        protected void onCancelled() {
            Log.d(TAG, "取消更新");
        }
    }
}
