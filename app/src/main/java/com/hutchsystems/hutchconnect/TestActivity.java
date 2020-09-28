package com.hutchsystems.hutchconnect;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {



            setTheme(R.style.AppTheme);
            super.onCreate(savedInstanceState);

            setContentView(R.layout.fragment_login);
        }
}
