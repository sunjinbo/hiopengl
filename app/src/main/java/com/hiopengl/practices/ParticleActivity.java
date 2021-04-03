package com.hiopengl.practices;

import android.os.Bundle;

import com.hiopengl.R;
import com.hiopengl.base.ActionBarActivity;

public class ParticleActivity extends ActionBarActivity {

    private ParticleView mParticleView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_particle);
        mParticleView = findViewById(R.id.particle_view);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mParticleView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mParticleView.onPause();
    }
}
