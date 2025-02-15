package pro.sketchware.utility;

import static pro.sketchware.utility.ThemeUtils.isDarkThemeEnabled;

import android.os.Build;
import android.content.Context;
import android.graphics.Typeface;

import androidx.annotation.NonNull;

import com.google.android.material.color.MaterialColors;

import io.github.rosemoe.sora.widget.CodeEditor;
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme;
import io.github.rosemoe.sora.langs.java.JavaLanguage;
import io.github.rosemoe.sora.widget.schemes.SchemeDarcula;

import mod.jbk.code.CodeEditorColorSchemes;
import mod.jbk.code.CodeEditorLanguages;

import a.a.a.Lx;

public class EditorUtils {
	EditorUtils() {
	}
	
	@NonNull
	public static EditorColorScheme getMaterialStyledScheme(final CodeEditor editor) {
		var scheme = editor.getColorScheme();
		var primary = MaterialColors.getColor(editor, com.google.android.material.R.attr.colorPrimary);
		var surface = MaterialColors.getColor(editor, com.google.android.material.R.attr.colorSurface);
		var onSurface = MaterialColors.getColor(editor, com.google.android.material.R.attr.colorOnSurface);
		var onSurfaceVariant = MaterialColors.getColor(editor, com.google.android.material.R.attr.colorOnSurfaceVariant);
		scheme.setColor(EditorColorScheme.KEYWORD, primary);
		scheme.setColor(EditorColorScheme.FUNCTION_NAME, primary);
		scheme.setColor(EditorColorScheme.WHOLE_BACKGROUND, surface);
		scheme.setColor(EditorColorScheme.CURRENT_LINE, surface);
		scheme.setColor(EditorColorScheme.LINE_NUMBER_PANEL, surface);
		scheme.setColor(EditorColorScheme.LINE_NUMBER_BACKGROUND, surface);
		scheme.setColor(EditorColorScheme.TEXT_NORMAL, onSurface);
		scheme.setColor(EditorColorScheme.SELECTION_INSERT, onSurfaceVariant);
		return scheme;
	}
	
	@NonNull
	public static Typeface getTypeface(final Context context) {
		return Typeface.createFromAsset(context.getAssets(), "fonts/jetbrainsmono-regular.ttf");
	}
	
	public static void loadJavaConfig(final CodeEditor editor) {
		editor.setEditorLanguage(new JavaLanguage());
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			if (isDarkThemeEnabled(editor.getContext())) {
				editor.setColorScheme(new SchemeDarcula());
			} else {
				editor.setColorScheme(new EditorColorScheme());
			}
		} else {
			editor.setColorScheme(new EditorColorScheme());
		}
		editor.setColorScheme(getMaterialStyledScheme(editor));
		formatJavaCode(editor);
	}
	
	public static void loadXmlConfig(final CodeEditor editor) {
		editor.setEditorLanguage(CodeEditorLanguages.loadTextMateLanguage(CodeEditorLanguages.SCOPE_NAME_XML));
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			if (isDarkThemeEnabled(editor.getContext())) {
				editor.setColorScheme(CodeEditorColorSchemes.loadTextMateColorScheme(CodeEditorColorSchemes.THEME_DRACULA));
			} else {
				editor.setColorScheme(CodeEditorColorSchemes.loadTextMateColorScheme(CodeEditorColorSchemes.THEME_GITHUB));
			}
		} else {
			editor.setColorScheme(CodeEditorColorSchemes.loadTextMateColorScheme(CodeEditorColorSchemes.THEME_GITHUB));
		}
		editor.setColorScheme(getMaterialStyledScheme(editor));
	}
	
	public static void formatJavaCode(final CodeEditor editor) {
		// Get text from the editor
		String[] lines = editor.getText().toString().split("\n");
		
		StringBuilder formattedString = new StringBuilder();
		
		// Trim each line while preserving structure
		for (String line : lines) {
			String tmp = line;
			tmp = (tmp + "X").trim();
			formattedString.append(tmp.substring(0, tmp.length() - 1));
			formattedString.append("\n");
		}
		
		String prettifiedString = formattedString.toString();
		
		try {
			prettifiedString = Lx.j(prettifiedString, true);
		} catch (Exception e) {
			return;
		}
		
		// Set formatted text back into the editor
		editor.setText(prettifiedString);
	}
}
