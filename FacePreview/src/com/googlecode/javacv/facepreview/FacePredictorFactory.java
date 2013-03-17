package com.googlecode.javacv.facepreview;

import static com.googlecode.javacv.cpp.opencv_highgui.cvLoadImage;

import java.io.File;
import java.io.IOException;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import com.googlecode.javacv.cpp.opencv_core.IplImage;

public class FacePredictorFactory {
	
	// Singletons in android can only be used if you don't care about your app working in the background
	private static FacePredictor facePredictor;
	
	public static FacePredictor createFacePredictor(Context context) {
		if (facePredictor != null) {
			return facePredictor;
		}
		
		try {
			// try loading the the predictor from file
			facePredictor = new FacePredictor(context, "recognizer.xml");
			return facePredictor;
		} catch (Exception e) {}
		
		IplImage [] authorizedImages = {
				cvLoadImage(context.getApplicationContext().getExternalFilesDir(null).getAbsolutePath() + "/face_image_1.jpg"),
				cvLoadImage(context.getApplicationContext().getExternalFilesDir(null).getAbsolutePath() + "/face_image_2.jpg"),
				cvLoadImage(context.getApplicationContext().getExternalFilesDir(null).getAbsolutePath() + "/face_image_3.jpg")
		};
		try {
			facePredictor = new FacePredictor(context, authorizedImages);
			facePredictor.save(context, "recognizer.xml");
			// TODO: delete the images used to construct the recognizer
			return facePredictor;
		} catch (IOException e) {
			return null;
		}		
	}
	    
}