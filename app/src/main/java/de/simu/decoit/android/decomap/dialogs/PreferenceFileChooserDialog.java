package de.simu.decoit.android.decomap.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;

import de.simu.decoit.android.decomap.activities.R;

/**
 * Dialog to choose files or directories!
 *
 * @author Leonid Schwenke, Decoit GmbH
 * @version 0.2
 */
public class PreferenceFileChooserDialog extends Dialog {

    private static final String PARENT_DIR = "..";
    private ArrayList<String> fileList;
    private File currentPath;
    private boolean onlyDir = false;
    private String fileEnding;
    private final String key;

    private final LayoutInflater inflater;

    private View selectedRow;
    private View selectedDir;

    /**
     * Constructor
     *
     * @param context in which context should the dialog run
     * @param onlyDir only show directories
     * @param key preference key for filepath
     * @param defaultValue default filepath
     */
    public PreferenceFileChooserDialog(Context context, boolean onlyDir, String key, String defaultValue) {
        this(context, "", key, defaultValue);

        this.onlyDir = onlyDir;
    }

    /**
     * Constructor
     *
     * @param context in which context should the dialog run
     * @param fileEndsWith fileending filter
     * @param key preference key for filepath
     * @param defaultValue default filepath
     */
    public PreferenceFileChooserDialog(Context context, String fileEndsWith, String key, String defaultValue) {
        super(context);
        this.key = key;
        if (fileEndsWith != null) {
            this.fileEnding = fileEndsWith;
        } else {
            this.fileEnding = "";
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        File path;
        path = new File(prefs.getString(key, defaultValue));

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (!path.isDirectory()) {
            path = path.getParentFile();
        }
        if (!path.exists()) {
            if (!path.mkdirs()) {
                path = Environment.getExternalStorageDirectory();
            }
        }
        currentPath = path;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        loadFileList(currentPath);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.file_chooser_dialog);

        findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        WindowManager.LayoutParams layout = new WindowManager.LayoutParams();
        layout.copyFrom(getWindow().getAttributes());
        layout.width = WindowManager.LayoutParams.MATCH_PARENT;
        layout.height = WindowManager.LayoutParams.MATCH_PARENT;
        getWindow().setAttributes(layout);
        buildFileList();
    }

    private void buildFileList() {
        TextView title;
        title = ((TextView) findViewById(R.id.dialogTitle));
        title.setText(currentPath.getPath());

        Button selectDir = ((Button) findViewById(R.id.chooseButton));
        if (onlyDir) {
            selectDir.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveValue(currentPath);
                }
            });
        } else {
            selectDir.setVisibility(View.GONE);
        }

        TableLayout list = ((TableLayout) findViewById(R.id.fileList));
        list.removeAllViews();

        for (int i = 0; i < fileList.size(); i++) {
            inflater.inflate(R.layout.file_chooser_file_row, list);
        }

        for (int i = 0; i < fileList.size(); i++) {
            String file = fileList.get(i);
            View row = list.getChildAt(i);
            File choosenFile = getChosenFile(file);
            TextView fileView = ((TextView) row.findViewById(R.id.fileText));
            if (choosenFile.isDirectory()) {
                fileView.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(getContext(), R.drawable.ic_folder), null, null, null);
            } else {
                fileView.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(getContext(), R.drawable.ic_file), null, null, null);
            }
            fileView.setText(file);

            row.setOnTouchListener(new OnTouchRow());
            row.setOnClickListener(new OnClickRow(choosenFile));
        }
    }

    private File getChosenFile(String fileChosen) {
        if (fileChosen.equals(PARENT_DIR)) {
            return currentPath.getParentFile();
        } else {
            return new File(currentPath, fileChosen);
        }
    }


    private void loadFileList(File path) {
        this.currentPath = path;
        fileList = new ArrayList<>();

        if (path.exists() && path.canRead() && path.isDirectory()) {
            if (path.getParentFile() != null) {
                fileList.add(PARENT_DIR);
            }

            FilenameFilter filter = new FilenameFilter() {
                public boolean accept(File dir, String filename) {
                    File selectedFile = new File(dir, filename);
                    if (!selectedFile.canRead()) {
                        return false;
                    }
                    if (onlyDir) {
                        return selectedFile.isDirectory();
                    } else {
                        return (fileEnding != null && filename.toLowerCase().endsWith(fileEnding)) || selectedFile.isDirectory();
                    }
                }
            };

            Collections.addAll(fileList, path.list(filter));
        } else {
            loadFileList(Environment.getExternalStorageDirectory());
        }
    }

    private void saveValue(File selectedFile) {
        SharedPreferences prefs;
        SharedPreferences.Editor editor;

        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        editor = prefs.edit();
        if (selectedDir == null) {
            editor.putString(key, selectedFile.getAbsolutePath());
        } else {
            editor.putString(key, getChosenFile((String) ((TextView) selectedDir.findViewById(R.id.fileText)).getText()).getAbsolutePath());
        }

        editor.apply();
        dismiss();
    }

    private class OnClickRow implements View.OnClickListener {

        private final File choosenFile;

        public OnClickRow(File choosenFile) {
            this.choosenFile = choosenFile;
        }

        @Override
        public void onClick(View v) {
            if (!onlyDir || v.equals(selectedDir)) {
                if (choosenFile.isDirectory()) {
                    selectedRow = null;
                    selectedDir = null;
                    loadFileList(choosenFile);
                    buildFileList();
                } else {
                    saveValue(choosenFile);
                }
            } else {
                selectedDir = v;
            }
        }
    }

    private class OnTouchRow implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (!v.equals(selectedRow)) {
                if (selectedRow != null) {
                    selectedRow.setBackgroundColor(Color.TRANSPARENT);
                }
                v.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.defaultSelected));
                selectedRow = v;
            }

            return false;
        }
    }
}
