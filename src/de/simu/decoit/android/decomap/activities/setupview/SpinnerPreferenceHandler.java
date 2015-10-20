package de.simu.decoit.android.decomap.activities.setupview;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import de.simu.decoit.android.decomap.activities.R;
import de.simu.decoit.android.decomap.util.Toolbox;

/**
 * SelectedListener for monitoring mode spinner Preferences
 *
 * @author Leonid Schwenke, Decoit GmbH
 * @version 0.2
 */
public class SpinnerPreferenceHandler implements AdapterView.OnItemSelectedListener {
    protected final Context context;
    private final String key;
    private final ArrayAdapter<CharSequence> spinnerAdapter;
    private Spinner spinner;

    public SpinnerPreferenceHandler(Context pContex, Spinner pSpinner, String pKey) {
        context = pContex;
        key = pKey;
        setSpinner(pSpinner);

        spinnerAdapter = ArrayAdapter.createFromResource(pContex,
                R.array.preferences_value_serverForm, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Toolbox.logTxt("dsasdasda","dddddddddddddd");
        SharedPreferences prefs;
        SharedPreferences.Editor editor;

        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        editor = prefs.edit();

        editor.putString(key, ((TextView) spinner.getSelectedView()).getText().toString());
        editor.commit();

        Toolbox.logTxt("dsasdasda", "SDASDAsda");
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        spinner.setSelection(getSelectedItemPosition());
    }

    public void setSpinner(Spinner pSpinner) {
        if (spinner == pSpinner) {
            return;
        }

        if (spinner != null) {
            spinner.setOnItemSelectedListener(null);
        }
        spinner = pSpinner;
        spinner.setAdapter(spinnerAdapter);
        spinner.setSelection(getSelectedItemPosition());
        spinner.setOnItemSelectedListener(this);
    }

    public String getSelectedItem() {
        SharedPreferences prefs;
        prefs = PreferenceManager.getDefaultSharedPreferences(context);

        return prefs.getString(key, "IF-MAP");
    }

    public int getSelectedItemPosition() {
        String[] modeArray = context.getResources().getStringArray(R.array.preferences_value_serverForm);
        int pos = 0;
        String value = getSelectedItem();
        for (pos = 0; pos < modeArray.length; pos++) {
            if (value.equals(modeArray[pos])) {
                return pos;
            }
        }
        return pos;
    }

    public void resume() {
        spinner.setOnItemSelectedListener(this);
        spinner.setSelection(getSelectedItemPosition());
    }

    public void pause() {
        spinner.setOnItemSelectedListener(null);
    }
}