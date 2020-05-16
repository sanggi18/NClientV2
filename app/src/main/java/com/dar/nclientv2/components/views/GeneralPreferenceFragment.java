package com.dar.nclientv2.components.views;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SeekBarPreference;

import com.dar.nclientv2.PINActivity;
import com.dar.nclientv2.R;
import com.dar.nclientv2.SettingsActivity;
import com.dar.nclientv2.async.VersionChecker;
import com.dar.nclientv2.settings.Global;
import com.dar.nclientv2.settings.Login;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class GeneralPreferenceFragment extends PreferenceFragmentCompat {
    private AppCompatActivity act;

    public void setAct(AppCompatActivity act) {
        this.act = act;
    }

    public void setType(SettingsActivity.Type type) {
        switch (type){
            case MAIN:mainMenu();break;
            case COLUMN:columnMenu();break;
            case DATA:dataMenu();
        }
    }

    private void dataMenu() {
        addPreferencesFromResource(R.xml.settings_data);
        SeekBarPreference mobile=findPreference(getString(R.string.key_mobile_usage));
        SeekBarPreference wifi=findPreference(getString(R.string.key_wifi_usage));
        mobile.setOnPreferenceChangeListener((preference, newValue) -> {
            mobile.setTitle(getDataUsageString((Integer) newValue));
            return true;
        });
        wifi.setOnPreferenceChangeListener((preference, newValue) -> {
            wifi.setTitle(getDataUsageString((Integer) newValue));
            return true;
        });
        mobile.setTitle(getDataUsageString(mobile.getValue()));
        wifi.setTitle(getDataUsageString(wifi.getValue()));
        mobile.setUpdatesContinuously(true);
        wifi.setUpdatesContinuously(true);
    }
    private int getDataUsageString(int val){
        switch (val){
            case 0:return R.string.data_usage_no;
            case 1:return R.string.data_usage_thumb;
            case 2:return R.string.data_usage_full;
        }
        return R.string.data_usage_full;
    }
    private void mainMenu(){
        addPreferencesFromResource(R.xml.settings);

        findPreference("col_screen").setOnPreferenceClickListener(preference -> {
            Intent i=new Intent(act,SettingsActivity.class);
            i.putExtra(act.getPackageName()+".TYPE", SettingsActivity.Type.COLUMN.ordinal());
            act.runOnUiThread(() -> act.startActivity(i));
            return false;
        });
        findPreference("data_screen").setOnPreferenceClickListener(preference -> {
            Intent i=new Intent(act,SettingsActivity.class);
            i.putExtra(act.getPackageName()+".TYPE", SettingsActivity.Type.DATA.ordinal());
            act.runOnUiThread(() -> act.startActivity(i));
            return false;
        });
        findPreference(getString(R.string.key_use_account_tag)).setEnabled(Login.isLogged());

        findPreference(getString(R.string.key_theme_select)).setOnPreferenceChangeListener((preference, newValue) -> {
            act.recreate();
            return true;
        });
        findPreference(getString(R.string.key_language)).setOnPreferenceChangeListener((preference, newValue) -> {
            act.recreate();
            return true;
        });
        findPreference("has_pin").setOnPreferenceChangeListener((preference, newValue) -> {
            if(newValue.equals(Boolean.TRUE)) {
                Intent i = new Intent(act, PINActivity.class);
                i.putExtra(act.getPackageName() + ".SET", true);
                startActivity(i);
                act.finish();
                return false;
            }
            act.getSharedPreferences("Settings",0).edit().remove("pin").apply();
            return true;
        });

        findPreference("version").setTitle(getString(R.string.app_version_format, Global.getVersionName(getContext())));

        double cacheSize=Global.recursiveSize(act.getCacheDir())/((double)(1<<20));

        //clear cache if pressed
        findPreference(getString(R.string.key_cache)).setSummary(getString(R.string.cache_size_formatted,cacheSize));
        findPreference(getString(R.string.key_cache)).setOnPreferenceClickListener(preference -> {
            MaterialAlertDialogBuilder builder=new MaterialAlertDialogBuilder(act);
            builder.setTitle(R.string.clear_cache);
            builder.setPositiveButton(R.string.yes, (dialog, which) -> {
                Global.recursiveDelete(act.getCacheDir());
                act.runOnUiThread(() -> {
                    Toast.makeText(act, act.getString(R.string.cache_cleared), Toast.LENGTH_SHORT).show();
                    double cSize=Global.recursiveSize(act.getCacheDir())/((double)(2<<20));
                    findPreference(getString(R.string.key_cache)).setSummary(getString(R.string.cache_size_formatted,cSize));
                });

            }).setNegativeButton(R.string.no,null).setCancelable(true);
            builder.show();

            return true;
        });
        findPreference(getString(R.string.key_update)).setOnPreferenceClickListener(preference -> {
            new VersionChecker(act,false);
            return true;
        });
        findPreference("bug").setOnPreferenceClickListener(preference -> {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Dar9586/NClientV2/issues/new"));
            startActivity(i);
            return true;
        });
        findPreference("donate").setOnPreferenceClickListener(preference -> {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Dar9586/NClientV2#donation"));
            startActivity(i);
            return true;
        });
        ((SeekBarPreference)findPreference(getString(R.string.key_max_history_size))).setShowSeekBarValue(true);
        ((SeekBarPreference)findPreference(getString(R.string.key_favorite_limit))).setShowSeekBarValue(true);
    }



    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey){
        getPreferenceManager().setSharedPreferencesName("Settings");
    }

    private void columnMenu() {
        addPreferencesFromResource(R.xml.settings_column);
        /*findPreference("col_main").setOnPreferenceClickListener(preferenceClickListener(1));
        findPreference("col_download").setOnPreferenceClickListener(preferenceClickListener(2));
        findPreference("col_favorite").setOnPreferenceClickListener(preferenceClickListener(4));
        findPreference("col_history").setOnPreferenceClickListener(preferenceClickListener(3));*/
        ((SeekBarPreference)findPreference(getString(R.string.key_column_port_down))).setShowSeekBarValue(true);
        ((SeekBarPreference)findPreference(getString(R.string.key_column_port_favo))).setShowSeekBarValue(true);
        ((SeekBarPreference)findPreference(getString(R.string.key_column_port_main))).setShowSeekBarValue(true);
        ((SeekBarPreference)findPreference(getString(R.string.key_column_port_hist))).setShowSeekBarValue(true);
        ((SeekBarPreference)findPreference(getString(R.string.key_column_land_down))).setShowSeekBarValue(true);
        ((SeekBarPreference)findPreference(getString(R.string.key_column_land_favo))).setShowSeekBarValue(true);
        ((SeekBarPreference)findPreference(getString(R.string.key_column_land_main))).setShowSeekBarValue(true);
        ((SeekBarPreference)findPreference(getString(R.string.key_column_land_hist))).setShowSeekBarValue(true);
        findPreference(getString(R.string.key_column_port_down)).setOnPreferenceChangeListener(changeListener);
        findPreference(getString(R.string.key_column_port_favo)).setOnPreferenceChangeListener(changeListener);
        findPreference(getString(R.string.key_column_port_main)).setOnPreferenceChangeListener(changeListener);
        findPreference(getString(R.string.key_column_port_hist)).setOnPreferenceChangeListener(changeListener);
        findPreference(getString(R.string.key_column_land_down)).setOnPreferenceChangeListener(changeListener);
        findPreference(getString(R.string.key_column_land_favo)).setOnPreferenceChangeListener(changeListener);
        findPreference(getString(R.string.key_column_land_main)).setOnPreferenceChangeListener(changeListener);
        findPreference(getString(R.string.key_column_land_hist)).setOnPreferenceChangeListener(changeListener);
    }
    private SeekBarPreference.OnPreferenceChangeListener changeListener= (preference, newValue) -> {
        int p= Integer.parseInt(String.valueOf(newValue));
        if(p==0){
            ((SeekBarPreference)preference).setValue(1);
            return false;
        }
        return true;
    };
}
