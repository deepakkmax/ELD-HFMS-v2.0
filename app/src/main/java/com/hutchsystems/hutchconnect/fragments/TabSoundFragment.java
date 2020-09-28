package com.hutchsystems.hutchconnect.fragments;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.common.LogFile;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.db.LogDB;
import com.hutchsystems.hutchconnect.db.SettingsDB;

public class TabSoundFragment extends Fragment implements CompoundButton.OnCheckedChangeListener {
    final String TAG = TabSoundFragment.class.getName();

    int driverId = 0;
    Switch switchReadViolation;
    Switch switchReadDutyStatusChanged;
    TextView tvVolumeLevel;

    AudioManager audio;

    SeekBar seekVolume;

    public TabSoundFragment() {
    }

    private void initialize(View view) {
        try {
            switchReadViolation = (Switch) view.findViewById(R.id.switchReadViolation);
            switchReadViolation.setChecked(Utility._appSetting.getViolationReading() == 1 ? true : false);
            switchReadViolation.setOnCheckedChangeListener(this);

            switchReadDutyStatusChanged = (Switch) view.findViewById(R.id.switchReadDutyStatusChanged);
            switchReadDutyStatusChanged.setChecked(Utility._appSetting.getDutyStatusReading() == 1 ? true : false);
            switchReadDutyStatusChanged.setOnCheckedChangeListener(this);

            tvVolumeLevel = (TextView) view.findViewById(R.id.tvVolumeLevel);

            audio = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
            seekVolume = (SeekBar) view.findViewById(R.id.seekVolume);
            //set maximum for progress bar
            seekVolume.setMax(audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
            //set current value for progress
            seekVolume.setProgress(audio.getStreamVolume(AudioManager.STREAM_MUSIC));

            seekVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                int progreessed = audio.getStreamVolume(AudioManager.STREAM_MUSIC);

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                    if (progress > progreessed) {
//                        //UP
//                        audio.adjustStreamVolume(AudioManager.STREAM_MUSIC,
//                                AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
//                    } else {
//                        //down
//                        audio.adjustStreamVolume(AudioManager.STREAM_MUSIC,
//                                AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
//                    }

                    progreessed = progress;
                    audio.setStreamVolume(AudioManager.STREAM_MUSIC, progreessed, AudioManager.FLAG_PLAY_SOUND);
                    Log.i(TAG, "progress=" + progress);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    //int maxVolume = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                    if (seekBar.getProgress() != progreessed) {
                        Log.i(TAG, "Not update");
                        progreessed = seekBar.getProgress();
                    }
                    audio.setStreamVolume(AudioManager.STREAM_MUSIC, progreessed, 0);
                }
            });
        } catch (Exception e) {
            LogFile.write(TabSoundFragment.class.getName() + "::initialize error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(TabSoundFragment.class.getName(),"initialize",e.getMessage(), Utility.printStackTrace(e));

        }

    }

    public static TabSoundFragment newInstance() {
        TabSoundFragment fragment = new TabSoundFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.i("Settings", "onCreateView Sound");
        View view = inflater.inflate(R.layout.tab_fragment_sound, container, false);
        initialize(view);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        try {
            switch (buttonView.getId()) {
                case R.id.switchReadViolation:
                    Utility._appSetting.setViolationReading(isChecked ? 1 : 0);
                    SettingsDB.CreateSettings();
                    break;

                case R.id.switchReadDutyStatusChanged:
                    Utility._appSetting.setDutyStatusReading(isChecked ? 1 : 0);
                    SettingsDB.CreateSettings();
                    break;
            }

        } catch (Exception e) {
            LogFile.write(TabSoundFragment.class.getName() + "::onCheckedChanged Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(TabSoundFragment.class.getName(),"onCheckedChanged",e.getMessage(), Utility.printStackTrace(e));

        }
    }
}