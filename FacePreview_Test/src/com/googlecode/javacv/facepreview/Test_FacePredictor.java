package com.googlecode.javacv.facepreview;

import java.io.File;
import java.io.IOException;

import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.facepreview.FacePredictor;

import static com.googlecode.javacv.cpp.opencv_highgui.*;

import android.test.AndroidTestCase;

public class Test_FacePredictor extends AndroidTestCase {

	public void testRecognizeThirdPerson() throws IOException {
		FacePredictor facePredictor = new FacePredictor(getContext()); 
		File imageFile = Loader.extractResource(getClass(),
				"/com/googlecode/javacv/facepreview/b_03_05.jpg",
				getContext().getCacheDir(), "image", ".jpg");
	    IplImage image = cvLoadImage(imageFile.getAbsolutePath());
	    String name = facePredictor.identify(image).first;
	    assertEquals(name, "3");
	}	
}
