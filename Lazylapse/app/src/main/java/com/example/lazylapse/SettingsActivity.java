package com.example.lazylapse;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.TextUtils;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.PreferenceChangeListener;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CameraManager cameras = (CameraManager) getSystemService(CAMERA_SERVICE);
        String[] listCameras;
        String[] listIdCameras;
        try {
            listIdCameras = cameras.getCameraIdList();
            listCameras = new String[listIdCameras.length];
            for (String cameraId : listIdCameras) {
                listCameras[Integer.valueOf(cameraId)] = "sensor " + cameraId + ":" + cameras.getCameraCharacteristics(cameraId).get(CameraCharacteristics.LENS_FACING);
            }
        } catch (CameraAccessException e) {
            listIdCameras = new String[]{"1"};
            listCameras = new String[]{"error"};
        }

        addPreferencesFromResource(R.xml.preferences);
        ListPreference listPref = (ListPreference) findPreference("cameraId");
        listPref.setEntryValues(listIdCameras);
        listPref.setEntries(listCameras);
    }
}