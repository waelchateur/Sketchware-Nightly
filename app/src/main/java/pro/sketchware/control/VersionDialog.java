package pro.sketchware.control;

import android.text.TextUtils;
import android.view.LayoutInflater;

import com.besome.sketch.projects.MyProjectSettingActivity;
import pro.sketchware.R;
import pro.sketchware.databinding.DialogAdvancedVersionControlBinding;

import a.a.a.aB;
import a.a.a.mB;
import pro.sketchware.lib.validator.VersionNamePostfixValidator;
import mod.hey.studios.util.Helper;

public class VersionDialog {
    private final MyProjectSettingActivity activity;
    private final DialogAdvancedVersionControlBinding binding;

    public VersionDialog(MyProjectSettingActivity activity) {
        this.activity = activity;
        LayoutInflater inflater = LayoutInflater.from(activity);
        binding = DialogAdvancedVersionControlBinding.inflate(inflater);
    }

    public void show() {
        final aB dialog = new aB(activity);
        dialog.a(R.drawable.numbers_48);
        dialog.b("Advanced Version Control");

        binding.versionCode.setText(String.valueOf(Integer.parseInt(Helper.getText(activity.binding.verCode))));
        binding.versionName1.setText(Helper.getText(activity.binding.verName).split(" ")[0]);
        if (Helper.getText(activity.binding.verName).split(" ").length > 1)
            binding.versionName2.setText(Helper.getText(activity.binding.verName).split(" ")[1]);

        dialog.a(binding.getRoot());
        dialog.b(Helper.getResString(R.string.common_word_save), v -> {
            final String verCode = Helper.getText(binding.versionCode);
            final String verName = Helper.getText(binding.versionName1);
            final String verNamePostfix = Helper.getText(binding.versionName2);

            boolean validVerCode = !TextUtils.isEmpty(verCode);
            boolean validVerName = !TextUtils.isEmpty(verName);

            if (validVerCode) {
                binding.versionCode.setError(null);
            } else {
                binding.versionCode.setError("Invalid Version Code");
            }

            if (validVerName) {
                binding.versionName1.setError(null);
            } else {
                binding.versionName1.setError("Invalid Version Name");
            }

            if (!mB.a() && validVerCode && validVerName) {
                activity.binding.verCode.setText(verCode);
                activity.binding.verName.setText(!verNamePostfix.isEmpty() ? (verName + " " + verNamePostfix) : verName);
                dialog.dismiss();
            }
        });

        binding.versionName2.addTextChangedListener(new VersionNamePostfixValidator(activity, binding.tilVersionNameExtra));
        dialog.a(Helper.getResString(R.string.common_word_cancel), Helper.getDialogDismissListener(dialog));
        dialog.show();
    }
}
