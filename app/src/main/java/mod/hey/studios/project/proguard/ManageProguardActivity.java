package mod.hey.studios.project.proguard;

import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import androidx.appcompat.widget.Toolbar;

import pro.sketchware.R;

import com.besome.sketch.lib.base.BaseAppCompatActivity;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.HashMap;

import mod.agus.jcoderz.editor.manage.library.locallibrary.ManageLocalLibrary;

public class ManageProguardActivity extends BaseAppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private ProguardHandler pg;
    private MaterialSwitch sw_pg_enabled;
    private MaterialSwitch sw_pg_debug;
    private MaterialSwitch r8_enabled;

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.ig_toolbar_back) {
            finish();
        } else if (id == R.id.ln_pg_rules) {
            new ProguardRulesDialog(this, pg).show();
        } else if (id == R.id.ln_pg_fm) {
            fmDialog();
        }
    }

    private void fmDialog() {
        ManageLocalLibrary mll = new ManageLocalLibrary(getIntent().getStringExtra("sc_id"));

        final String[] libraries = new String[mll.list.size()];
        final boolean[] enabledLibraries = new boolean[mll.list.size()];

        for (int i = 0; i < mll.list.size(); i++) {
            HashMap<String, Object> current = mll.list.get(i);

            Object name = current.get("name");
            if (name instanceof String) {
                libraries[i] = (String) name;
                enabledLibraries[i] = pg.libIsProguardFMEnabled(libraries[i]);
            } else {
                libraries[i] = "(broken library configuration)";
                enabledLibraries[i] = false;
            }
        }

        MaterialAlertDialogBuilder bld = new MaterialAlertDialogBuilder(this);
        bld.setTitle("Select Local libraries");
        bld.setMultiChoiceItems(libraries, enabledLibraries, (dialog, which, isChecked) -> enabledLibraries[which] = isChecked);
        bld.setPositiveButton(R.string.common_word_save, (dialog, which) -> {

            ArrayList<String> finalList = new ArrayList<>();

            for (int i = 0; i < libraries.length; i++) {
                if (enabledLibraries[i]) {
                    finalList.add(libraries[i]);
                }
            }
            
            pg.setProguardFMLibs(finalList);
        });
        bld.setNegativeButton(R.string.common_word_cancel, null);
        bld.create().show();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int id = buttonView.getId();
        if (id == R.id.sw_pg_enabled) {
            pg.setProguardEnabled(isChecked);
        } else if (id == R.id.r8_enabled) {
            pg.setR8Enabled(isChecked);
        } else if (id == R.id.sw_pg_debug) {
            pg.setDebugEnabled(isChecked);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage_proguard);

        initialize();
        initializeLogic();
    }

    private void initialize() {
        sw_pg_enabled = findViewById(R.id.sw_pg_enabled);

        LinearLayout ln_pg_rules = findViewById(R.id.ln_pg_rules);
        sw_pg_debug = findViewById(R.id.sw_pg_debug);

        LinearLayout ln_pg_fm = findViewById(R.id.ln_pg_fm);

        sw_pg_enabled.setOnCheckedChangeListener(this);
        ln_pg_rules.setOnClickListener(this);
        r8_enabled = findViewById(R.id.r8_enabled);
        r8_enabled.setOnCheckedChangeListener(this);

        sw_pg_debug.setOnCheckedChangeListener(this);
        ln_pg_fm.setOnClickListener(this);
    }

    private void initializeLogic() {
        _initToolbar();
        pg = new ProguardHandler(getIntent().getStringExtra("sc_id"));
        sw_pg_enabled.setChecked(pg.isShrinkingEnabled());
        sw_pg_debug.setChecked(pg.isDebugFilesEnabled());
        r8_enabled.setChecked(pg.isR8Enabled());
    }

    private void _initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Code Shrinking Manager");
        toolbar.setNavigationOnClickListener(view -> onBackPressed());
    }
}