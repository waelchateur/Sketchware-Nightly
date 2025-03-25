package com.besome.sketch.lib.base;

import static pro.sketchware.utility.theme.ThemeManager.getCurrentTheme;
import static pro.sketchware.utility.theme.ThemeManager.getSystemAppliedTheme;
import static pro.sketchware.utility.theme.ThemeManager.isAmoledEnabled;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.annotations.NonNull;
import com.google.firebase.analytics.FirebaseAnalytics;

import dev.chrisbanes.insetter.Insetter;

import java.util.ArrayList;

import pro.sketchware.dialogs.ProgressDialog;

import a.a.a.MA;
import a.a.a.ZA;
import a.a.a.lC;
import a.a.a.xB;
import pro.sketchware.utility.theme.ThemeManager;

public abstract class BaseAppCompatActivity extends AppCompatActivity {

    public FirebaseAnalytics mAnalytics;

    @Deprecated
    public Context e;
    public Activity parent;
    protected ProgressDialog progressDialog;
    private ZA lottieDialog;
    private ArrayList<MA> taskList;
    private boolean isAmoled;


    public void a(MA var1) {
        taskList.add(var1);
    }

    public void addTask(MA task) {
        taskList.add(task);
    }

    public void a(OnCancelListener cancelListener) {
        if (progressDialog != null && !progressDialog.isShowing()) {
            progressDialog.setOnCancelListener(cancelListener);
            progressDialog.show();
        }
    }

    public void a(String var1) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.setMessage(var1);
        }
    }

    public void g() {
        for (MA task : taskList) {
            if (task.getStatus() != Status.FINISHED && !task.isCancelled()) {
                task.cancel(true);
            }
        }
        taskList.clear();
    }

    public void h() {
        try {
            if (lottieDialog != null && lottieDialog.isShowing()) {
                lottieDialog.dismiss();
            }
        } catch (Exception var2) {
            lottieDialog = null;
            lottieDialog = new ZA(this);
        }
    }

    public void i() {
        try {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        } catch (Exception var2) {
            progressDialog = null;
            progressDialog = new ProgressDialog(this);
        }

    }

    public boolean isStoragePermissionGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == 0 && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == 0;
    }

    public boolean j() {
        return isStoragePermissionGranted();
    }

    public void k() {
        if (lottieDialog != null && !lottieDialog.isShowing() && !isFinishing()) {
            lottieDialog.show();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyMode(this, ThemeManager.getCurrentMode(this));
        isAmoled = isAmoledEnabled(this);
        super.onCreate(savedInstanceState);
        e = getApplicationContext();
        taskList = new ArrayList<>();
        lottieDialog = new ZA(this);
        lC.a(getApplicationContext(), false);
        progressDialog = new ProgressDialog(this);
        mAnalytics = FirebaseAnalytics.getInstance(this);
    }

    public final String getTranslatedString(@StringRes int resId) {
        return xB.b().a(getApplicationContext(), resId);
    }

    @Override
    public void onDestroy() {
        g();
        if (lottieDialog != null && lottieDialog.isShowing()) {
            lottieDialog.cancelAnimation();
        }
        super.onDestroy();
    }

    @Override
    public void onPause() {
        if (lottieDialog != null && lottieDialog.isShowing()) {
            lottieDialog.pauseAnimation();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        if ((getSystemAppliedTheme(this) != getCurrentTheme(this)) || (isAmoled != isAmoledEnabled(this))) {
            recreate();
        }

        if (lottieDialog != null && lottieDialog.isShowing()) {
            lottieDialog.resumeAnimation();
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        if (parent != null) {
            return parent.onCreateOptionsMenu(menu);
        }
        return true;
    }

    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (parent != null) {
            return parent.onOptionsItemSelected(item);
        }
        return false;
    }

    public void handleInsetts(View root) {
        Insetter.builder()
            .padding(WindowInsetsCompat.Type.navigationBars())
            .applyToView(root);
    }
}