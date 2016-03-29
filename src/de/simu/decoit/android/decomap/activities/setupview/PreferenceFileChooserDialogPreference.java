package de.simu.decoit.android.decomap.activities.setupview;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;

/**
 * Preference to use PreferenceFileChooserDialog
 *
 * @author Leonid Schwenke, Decoit GmbH
 * @version 0.2
 */
public class PreferenceFileChooserDialogPreference extends Preference {

    private final String defaultValue;
    private final String fileEnding;
    private final boolean onlyDir;

    private final SharedPreferences preferenceManager;

    public PreferenceFileChooserDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        String ANDROID_DNS = "http://schemas.android.com/apk/res/android";
        String defValue = attrs.getAttributeValue(ANDROID_DNS, "defaultValue");
        if (defValue == null) {
            defaultValue = Environment.getExternalStorageDirectory().getAbsolutePath();
        } else {
            defaultValue = Environment.getExternalStorageDirectory() + defValue;
        }
        String MYNS = "http://schemas.android.com/apk/res-auto";
        fileEnding = attrs.getAttributeValue(MYNS, "fileEnding");
        onlyDir = attrs.getAttributeBooleanValue(MYNS, "onlyDirectory", false);
        preferenceManager = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    protected void onClick() {
        super.onClick();
        if (onlyDir) {
            new PreferenceFileChooserDialog(getContext(), true, getKey(), defaultValue).show();
        } else {
            new PreferenceFileChooserDialog(getContext(), fileEnding, getKey(), defaultValue).show();
        }
    }

    public String getPath() {
        return preferenceManager.getString(getKey(), defaultValue);
    }
}
