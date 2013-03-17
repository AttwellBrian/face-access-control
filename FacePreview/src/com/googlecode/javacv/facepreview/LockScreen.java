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
import com.googlecode.javacv.facepreview.views.FaceView.FaceViewImageCallback;
import com.googlecode.javacv.facepreview.views.Preview;

public class LockScreen extends Activity implements FaceView.FaceViewImageCallback {
    private FrameLayout layout;
    private FaceView faceView;
    private Preview mPreview;

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Hide the window title.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        bindService(new Intent(LockScreen.this, RecognizerService.class), mConnection, Context.BIND_ABOVE_CLIENT);
        
        // Create our Preview view and set it as the content of our activity.
        try {
            layout = new FrameLayout(this);
            faceView = new FaceView(this);
            mPreview = new Preview(this, faceView);
            faceView.setFaceViewImageCallback(this);
            layout.addView(mPreview);
            layout.addView(faceView);
            setContentView(layout);
        } catch (IOException e) {
            e.printStackTrace();
            new AlertDialog.Builder(this).setMessage(e.getMessage()).create().show();
        }
        
    }
    
    private static long lastUnixTime = System.currentTimeMillis();//don't use this for subsecond
    
	@Override
	public void image(IplImage image) {

		final IplImage ownedImage = image.clone();
		final RecognizerService service = mBoundService;
		if (service.facePredictor == null) {
			return;
		}
		
		// Rate limit the image analysis (should probably be done in the other function)
		if (System.currentTimeMillis() <= lastUnixTime + 4000) {
			return;
		}
		lastUnixTime = System.currentTimeMillis();
		
		new AsyncTask<RecognizerService, Void, Boolean>() {
			@Override
			protected Boolean doInBackground(RecognizerService... params) {
				return service.facePredictor.authenticate(ownedImage);
			}
			@Override
			protected void onPostExecute(Boolean result) {
				if (result) {
					//AlertDialog.Builder builder = new AlertDialog.Builder(LockScreen.this);
					//builder.setMessage("You have unlocked the app!").setTitle("Success");
					//AlertDialog dialog = builder.create();
					//dialog.show();
				}
				
				int duration = Toast.LENGTH_SHORT;
				Toast toast = Toast.makeText(getApplicationContext(), "Debug: result = " + result, duration);
				toast.show();
				
				// TODO: only let one of these async tasks execute at once (for performance reasons, not for correctness reasons)
			}
		}.execute();
	}
    
    private RecognizerService mBoundService;
    private ServiceConnection mConnection = new ServiceConnection() {
    	public void onServiceConnected(ComponentName className, IBinder service) {
    	        // This is called when the connection with the service has been
    	        // established, giving us the service object we can use to
    	        // interact with the service.  Because we have bound to a explicit
    	        // service that we know is running in our own process, we can
    	        // cast its IBinder to a concrete class and directly access it.
    	        mBoundService = ((RecognizerService.RecognizerServiceBinder) service).getService();
    	    }

    	    public void onServiceDisconnected(ComponentName className) {
    	        // Because it is running in our same process, we should never
    	        // see this happen.
    	        mBoundService = null;
    	    }
    	};

}