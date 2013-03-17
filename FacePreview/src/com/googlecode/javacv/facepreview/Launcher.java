package com.googlecode.javacv.facepreview;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class Launcher extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        File recognizerFile = new File(this.getExternalFilesDir(null).getAbsolutePath() + "/recognizer.xml");
	    if (recognizerFile.exists()) {
	    	Intent intent = new Intent(this,LockScreen.class);
	    	startActivity(intent);
	    	finish();
	    } else {
	    	Intent intent = new Intent(this, Introduction.class);
	    	startActivity(intent);
	    	finish();
	    }

    }
}