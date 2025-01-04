package mod.khaled.logcat;

import static pro.sketchware.utility.FileUtil.createNewFileIfNotPresent;

import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import pro.sketchware.R;
import pro.sketchware.databinding.ActivityLogcatreaderBinding;
import pro.sketchware.databinding.EasyDeleteEdittextBinding;
import pro.sketchware.databinding.ViewLogcatItemBinding;

import com.besome.sketch.lib.base.BaseAppCompatActivity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pro.sketchware.utility.SketchwareUtil;
import pro.sketchware.lib.base.BaseTextWatcher;
import mod.hey.studios.util.Helper;

public class LogReaderActivity extends BaseAppCompatActivity {

    private final BroadcastReceiver logger = new Logger();
    private final Pattern logPattern = Pattern.compile("^(.*\\d) ([VADEIW]) (.*): (.*)");
    private String pkgFilter = "";
    private String packageName = "pro.sketchware";
    private boolean autoScroll = true;

    private String date;
    private String tag;
    private String body;
    private String type;

    private final ArrayList<HashMap<String, Object>> mainList = new ArrayList<>();
    private ArrayList<String> pkgFilterList = new ArrayList<>();

    private ActivityLogcatreaderBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);

        binding = ActivityLogcatreaderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initialize();
    }

    private void initialize() {
        binding.logsRecyclerView.setAdapter(new Adapter(new ArrayList<>()));

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("pro.sketchware.ACTION_NEW_DEBUG_LOG");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(logger, intentFilter, Context.RECEIVER_EXPORTED);
        } else {
            registerReceiver(logger, intentFilter);
        }

        binding.topAppBar.setNavigationOnClickListener(Helper.getBackPressedClickListener(this));
        binding.topAppBar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_clear) {
                mainList.clear();
                ((Adapter) binding.logsRecyclerView.getAdapter()).deleteAll();
            } else if (id == R.id.action_auto_scroll) {
                autoScroll = !item.isChecked();
                item.setChecked(autoScroll);
                if (autoScroll) {
                    binding.logsRecyclerView.getLayoutManager().scrollToPosition(binding.logsRecyclerView.getAdapter().getItemCount() - 1);
                }
            } else if (id == R.id.action_filter) {
                showFilterDialog();
            } else if (id == R.id.action_export) {
                exportLogcat(mainList);
            }
            return true;
        });

        binding.searchInput.addTextChangedListener(new BaseTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                final String _charSeq = s.toString();
                if (_charSeq.isEmpty() && (pkgFilterList.isEmpty())) {
                    binding.logsRecyclerView.setAdapter(new Adapter(mainList));
                } else {
                    ArrayList<HashMap<String, Object>> filteredList = new ArrayList<>();
                    for (HashMap<String, Object> m : mainList) {
                        if (!pkgFilterList.isEmpty()) {
                            if (m.containsKey("pkgName") && pkgFilterList.contains(m.get("pkgName").toString())) {
                                if (m.get("logRaw").toString().toLowerCase().contains(_charSeq.toLowerCase())) {
                                    filteredList.add(m);
                                }
                            }
                        } else if (m.get("logRaw").toString().toLowerCase().contains(_charSeq.toLowerCase())) {
                            filteredList.add(m);
                        }
                    }
                    binding.logsRecyclerView.setAdapter(new Adapter(filteredList));
                }
            }
        });
    }

    void showFilterDialog() {
        var dialogBinding = EasyDeleteEdittextBinding.inflate(getLayoutInflater());
        View view = dialogBinding.getRoot();

        dialogBinding.imgDelete.setVisibility(View.GONE);

        var builder = new MaterialAlertDialogBuilder(this)
                .setTitle("Filter by package name")
                .setMessage("For multiple package names, separate them with a comma (,).")
                .setIcon(R.drawable.ic_mtrl_filter)
                .setView(view)
                .setPositiveButton("Apply", (dialog, which) -> {
                    pkgFilter = dialogBinding.easyEdInput.getText().toString();
                    pkgFilterList = new ArrayList<>(Arrays.asList(pkgFilter.split(",")));
                    binding.searchInput.setText(binding.searchInput.getText().toString());
                })
                .setNeutralButton("Reset", (dialog, which) -> {
                    pkgFilter = "";
                    pkgFilterList.clear();
                    dialogBinding.easyEdInput.setText("");
                })
                .setNegativeButton("Cancel", null)
                .create();

        builder.show();
    }

    private void exportLogcat(ArrayList<HashMap<String, Object>> logs){
        try {
            File file = new File(Environment.getExternalStorageDirectory(),".sketchware/logcat/" + packageName +"_"+ Calendar.getInstance(Locale.ENGLISH).getTimeInMillis() + ".txt");
            createNewFileIfNotPresent(file.getAbsolutePath());
            FileWriter writer = new FileWriter(file);
            for (int i = 0; i < logs.size(); i++) {
                if (logs.get(i).containsKey("date")) date = Objects.requireNonNull(logs.get(i).get("date")).toString();
                if (logs.get(i).containsKey("type")) type = Objects.requireNonNull(logs.get(i).get("type")).toString();
                if (logs.get(i).containsKey("header")) tag = Objects.requireNonNull(logs.get(i).get("header")).toString();
                if (logs.get(i).containsKey("body")) body = Objects.requireNonNull(logs.get(i).get("body")).toString();

                writer.write( date + " " +  type + " " + tag + " " + body + "\n");
            }
            writer.close();
            SketchwareUtil.toast("Logcat exported successfully");

        } catch (IOException ex) {
            SketchwareUtil.toastError("Something went wrong!");
        }
    }
    

    private class Logger extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            HashMap<String, Object> map = new HashMap<>();
            if (intent.hasExtra("log") && (intent.getStringExtra("log") != null)) {
                if (intent.hasExtra("packageName")) {
                    map.put("pkgName", intent.getStringExtra("packageName"));
                    packageName = intent.getStringExtra("packageName");
                }
                map.put("logRaw", intent.getStringExtra("log"));
                if (intent.getStringExtra("log") == null) return;

                Matcher matcher = logPattern.matcher(intent.getStringExtra("log"));
                if (matcher.matches()) {
                    //group 1 = pid, time stuff & idk
                    //group 2 = log type (a,d,e,i,w)
                    //group 3 = log tag,headers
                    //group 4 = everything else
                    map.put("date", matcher.group(1).trim());
                    map.put("type", matcher.group(2).trim());
                    map.put("header", matcher.group(3));
                    map.put("body", matcher.group(4));
                    map.put("culturedLog", "true");
                }

                mainList.add(map);
                if (pkgFilterList.isEmpty()) {
                    if (!binding.searchInput.getText().toString().isEmpty()) {
                        if (map.get("logRaw").toString().toLowerCase().contains(binding.searchInput.getText().toString().toLowerCase())) {
                            ((Adapter) binding.logsRecyclerView.getAdapter()).updateList(map);
                        }
                    } else {
                        ((Adapter) binding.logsRecyclerView.getAdapter()).updateList(map);
                    }
                } else if (map.containsKey("pkgName") && pkgFilterList.contains(map.get("pkgName").toString())) {
                    if (!binding.searchInput.getText().toString().isEmpty()) {
                        if (map.get("logRaw").toString().toLowerCase().contains(binding.searchInput.getText().toString().toLowerCase())) {
                            ((Adapter) binding.logsRecyclerView.getAdapter()).updateList(map);
                        }
                    } else {
                        ((Adapter) binding.logsRecyclerView.getAdapter()).updateList(map);
                    }
                }
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        binding.searchInput.clearFocus();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(logger);
    }

    private class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {
        private final ArrayList<HashMap<String, Object>> data;

        public void updateList(final HashMap<String, Object> _map) {
            data.add(_map);
            binding.logsRecyclerView.getAdapter().notifyItemInserted(data.size() + 1);

            if (autoScroll) {
                ((LinearLayoutManager) binding.logsRecyclerView.getLayoutManager()).scrollToPosition(data.size() - 1);
                binding.appBarLayout.setExpanded(false);
            }

            binding.noContentLayout.setVisibility(data.isEmpty() ? View.VISIBLE : View.GONE);
        }

        public void deleteAll() {
            data.clear();
            binding.logsRecyclerView.getAdapter().notifyDataSetChanged();
            binding.noContentLayout.setVisibility(View.VISIBLE);
        }

        public Adapter(ArrayList<HashMap<String, Object>> data) {
            this.data = data;
        }

        @Override
        @NonNull
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            var listBinding = ViewLogcatItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            var layoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            listBinding.getRoot().setLayoutParams(layoutParams);
            return new ViewHolder(listBinding);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
            var binding = holder.listBinding;

            if (data.get(position).containsKey("pkgName")) {
                binding.pkgName.setText(data.get(position).get("pkgName").toString());
                binding.pkgName.setVisibility(View.VISIBLE);
            } else {
                binding.pkgName.setVisibility(View.GONE);
            }
            if (data.get(position).containsKey("culturedLog")) {
                binding.dateHeader.setVisibility(View.VISIBLE);
                binding.type.setText(data.get(position).get("type").toString());
                binding.dateHeader.setText(data.get(position).get("date").toString() + " | " + (data.get(position).get("header").toString()));
                switch (Objects.requireNonNull(data.get(position).get("type")).toString()) {
                    case "A" -> binding.type.setBackgroundColor(0xFF9C27B0);
                    case "D" -> binding.type.setBackgroundColor(0xFF2196F3);
                    case "E" -> binding.type.setBackgroundColor(0xFFF44336);
                    case "I" -> binding.type.setBackgroundColor(0xFF4CAF50);
                    case "V" -> binding.type.setBackgroundColor(0xFF000000);
                    case "W" -> binding.type.setBackgroundColor(0xFFFFC107);
                    default -> {
                        binding.type.setBackgroundColor(0xFF000000);
                        binding.type.setText("U");
                    }
                }
                binding.log.setText(data.get(position).get("body").toString());
                try {
                    if (data.get(position).get("date").toString().equals(data.get(position + 1).get("date").toString())) {
//                        binding.divider.setVisibility(View.GONE);
                        try {
                            if (data.get(position).get("pkgName").toString().equals(data.get(position + 1).get("pkgName").toString())) {
                                binding.pkgName.setVisibility(View.GONE);
                            } else {
                                binding.pkgName.setVisibility(View.VISIBLE);
                            }
                        } catch (Exception ignored) {
                            binding.pkgName.setVisibility(View.VISIBLE);
                        }
                        try {
                            if (data.get(position).get("header").toString().equals(data.get(position + 1).get("header").toString())) {
                                binding.dateHeader.setVisibility(View.GONE);
                            } else {
                                binding.dateHeader.setVisibility(View.VISIBLE);
                            }
                        } catch (Exception ignored) {
                            binding.dateHeader.setVisibility(View.VISIBLE);
                        }
                    }
                } catch (Exception ignored) {
                }
            } else {
                binding.log.setText(data.get(position).get("logRaw").toString());
                binding.type.setBackgroundColor(0xFF000000);
                binding.type.setText("U");
                binding.dateHeader.setVisibility(View.GONE);
            }
            binding.getRoot().setOnLongClickListener(v -> {
                SketchwareUtil.toast("Copied to clipboard");
                ((ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("clipboard", data.get(position).get("logRaw").toString()));
                return true;
            });
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        private class ViewHolder extends RecyclerView.ViewHolder {
            private final ViewLogcatItemBinding listBinding;

            public ViewHolder(@NonNull ViewLogcatItemBinding listBinding) {
                super(listBinding.getRoot());
                this.listBinding = listBinding;
            }
        }
    }
}
