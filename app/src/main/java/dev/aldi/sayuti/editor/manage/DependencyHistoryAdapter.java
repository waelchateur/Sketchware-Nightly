package dev.aldi.sayuti.editor.manage;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import pro.sketchware.R;

public class DependencyHistoryAdapter extends ArrayAdapter<String> {
    private final DependencyHistoryManager historyManager;
    private final Runnable onHistoryChanged;

    public DependencyHistoryAdapter(Context context, List<String> dependencies,
                                    DependencyHistoryManager historyManager,
                                    Runnable onHistoryChanged) {
        super(context, 0, dependencies);
        this.historyManager = historyManager;
        this.onHistoryChanged = onHistoryChanged;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_dependency_history, parent, false);
        }

        String dependency = getItem(position);
        TextView textView = convertView.findViewById(R.id.dependency_text);
        ImageView deleteButton = convertView.findViewById(R.id.delete_button);

        textView.setText(dependency);
        deleteButton.setOnClickListener(v -> {
            historyManager.removeDependency(dependency);
            remove(dependency);
            notifyDataSetChanged();
            if (onHistoryChanged != null) onHistoryChanged.run();
        });

        return convertView;
    }
}