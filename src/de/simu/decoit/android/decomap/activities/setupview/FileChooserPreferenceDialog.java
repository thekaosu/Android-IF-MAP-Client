package de.simu.decoit.android.decomap.activities.setupview;

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
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import de.simu.decoit.android.decomap.activities.R;

/**
 * Dialog to choose files or directories!
 *
 * @author Leonid Schwenke, Decoit GmbH
 * @version 0.2
 */
public class FileChooserPreferenceDialog extends Dialog {

    private static final String PARENT_DIR = "..";
    private ArrayList<String> fileList;
    private File currentPath;
    private boolean onlyDir = false;
    private String fileEnding;
    private final String key;

    private final LayoutInflater inflater;
    private TextView title;

    private View selectedRow;
    private View selectedDir;

    /**
     * @param context
     */
    public FileChooserPreferenceDialog(Context context, boolean onlyDir, String key) {
        this(context, "", key);

        this.onlyDir = onlyDir;
    }

    /**
     * @param context
     */
    public FileChooserPreferenceDialog(Context context, String fileEndsWith, String key) {
        super(context);
        this.key = key;
        this.fileEnding = fileEndsWith;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        File path;
        if (key.equals("logPath")) {
            path = new File(prefs.getString(key, Environment.getExternalStorageDirectory() + "/ifmap-client/logs/"));
        } else if (key.equals("keystorePath")) {
            path = new File(prefs.getString(key, Environment.getExternalStorageDirectory() + "/ifmap-client/keystore/"));
        } else {
            path = new File(prefs.getString(key, ""));

        }
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (!path.isDirectory() && path != null) {
            path = path.getParentFile();
        }
        if (!path.exists()) {
            if (!path.mkdirs()) {
                path = Environment.getExternalStorageDirectory();
            }
        }
        currentPath = path;
    }

    /**
     * @return file dialog
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        loadFileList(currentPath);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.file_chooser_dialog);

        ((Button) findViewById(R.id.cancelButton)).setOnClickListener(new View.OnClickListener() {
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
        title = ((TextView) findViewById(R.id.dialogTitle));
        title.setText(currentPath.getPath());

        if (!onlyDir) {
            ((Button) findViewById(R.id.chooseButton)).setVisibility(View.GONE);
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
            if (choosenFile.isDirectory()) {
                ((ImageView) row.findViewById(R.id.fileIcon)).setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_tab_log_unselected));
            } else {
                ((ImageView) row.findViewById(R.id.fileIcon)).setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_tab_status_unselected));
            }
            ((TextView) row.findViewById(R.id.fileText)).setText(file);

            row.setOnTouchListener(new OnTouchRow());
            row.setOnClickListener(new OnClickRow(choosenFile));
        }
    }

    public boolean isOnlyDir() {
        return isOnlyDir();
    }

    public void setOnlyDir(boolean onlyDir) {
        this.onlyDir = onlyDir;
    }

    public void setFileEnding(String fileEnding) {
        this.fileEnding = fileEnding != null ? fileEnding.toLowerCase() : fileEnding;
    }

    public String getFileEnding() {
        return fileEnding;
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
        fileList = new ArrayList<String>();

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

            for (String file : path.list(filter)) {
                fileList.add(file);
            }
        } else {
            loadFileList(Environment.getExternalStorageDirectory());
        }
    }

    private class OnClickRow implements View.OnClickListener {

        private File choosenFile;

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
                    SharedPreferences prefs;
                    SharedPreferences.Editor editor;

                    prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                    editor = prefs.edit();
                    editor.putString(key, currentPath + "/" + ((TextView) v.findViewById(R.id.fileText)).getText());
                    editor.commit();
                    dismiss();
                }
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
            } else {
                selectedDir = v;
            }

            return false;
        }
    }
}
