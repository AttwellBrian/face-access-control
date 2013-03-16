package com.googlecode.javacv.facepreview;

import static com.googlecode.javacv.cpp.opencv_highgui.cvLoadImage;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.List;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

// Assumption: unbinding doesn't happen
// maybe this isn't the correct way to do this.
// FORGET THIS SERVICE, JUST USE A SEPERATE THREAD
// NO, USE A BOUND SERVICE (http://developer.android.com/guide/components/bound-services.html#Binder)

public class RecognizerService extends Service {
	
	
	FacePredictor facePredictor = null;
	
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
		
		//initPredictor();//should check if we are starting or not
		
        return START_STICKY;//should consider changing this later
    }
	
	@Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    // This is the object that receives interactions from clients.
    private final IBinder mBinder = new RecognizerServiceBinder();
    
    public class RecognizerServiceBinder extends Binder {
    	RecognizerService getService() {
        	// Return this instance of LocalService so clients can call public methods
        	// Very simple, since we aren't using multiple processes
            return RecognizerService.this;
        }
    };
	
    /*
	  } else if ( intent.getAction().equals( "com.googlecode.javacv.facepreview.authenticate" ) ) {
		  String fileName = intent.getExtras().getString("filename");
		  IplImage image = cvLoadImage(fileName);
    */
    
   
    public void initPredictor() {
		try {
			IplImage [] authorizedImages = {
    				cvLoadImage(getApplicationContext().getFilesDir() + "/face_image_1.jpg"),
    				cvLoadImage(getApplicationContext().getFilesDir() + "/face_image_2.jpg"),
    				cvLoadImage(getApplicationContext().getFilesDir() + "/face_image_3.jpg")
    		};
			facePredictor = new FacePredictor(this, authorizedImages);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    
}