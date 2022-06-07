package com.example.navigation;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

public class StartDraw extends Activity {
    DrawView drawView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.e("[Activity]", "StartDraw");
        super.onCreate(savedInstanceState);

        drawView = new DrawView(this);
        drawView.setBackgroundColor(Color.WHITE);
        setContentView(drawView);

    }
}

