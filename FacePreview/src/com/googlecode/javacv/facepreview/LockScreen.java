package com.googlecode.javacv.facepreview;

import static com.googlecode.javacv.cpp.opencv_core.cvGet2D;
import static com.googlecode.javacv.cpp.opencv_highgui.cvSaveImage;

import java.io.IOException;
import java.util.Calendar;
import java.util.LinkedList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.facepreview.views.FaceView;
import com.googlecode.javacv.facepreview.views.FaceViewWithAnalysis;
import com.googlecode.javacv.facepreview.views.Preview;

public class LockScreen extends Activity implements FaceViewWithAnalysis.SuccessCallback {
    private FrameLayout layout;
    private FaceViewWithAnalysis faceView;
    private Preview mPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);        // Hide the window title.
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        try {
            layout = new FrameLayout(this);
            faceView = new FaceViewWithAnalysis(this);
            mPreview = new Preview(this, faceView);
            faceView.setSuccessCallback(this);
            layout.addView(mPreview);
            layout.addView(faceView);
            setContentView(layout);
        } catch (IOException e) {
            e.printStackTrace();
            new AlertDialog.Builder(this).setMessage(e.getMessage()).create().show();
        }
        
    }
    
	@Override
	public void success(boolean bool) {

		int duration = Toast.LENGTH_SHORT;
		Toast toast = Toast.makeText(getApplicationContext(), "Debug: result = " + bool, duration);
		toast.show();

	}
    
}