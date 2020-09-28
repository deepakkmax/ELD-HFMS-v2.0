package com.hutchsystems.hutchconnect;

import androidx.appcompat.app.AppCompatActivity;


public abstract class ELogMainActivity extends AppCompatActivity {

    public abstract void maintenaceDueMonitor();

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onStop() {

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
