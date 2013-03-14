package com.googlecode.javacv.facepreview;

import java.io.File;
import java.io.IOException;

import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.facepreview.FacePredictor;

import static com.googlecode.javacv.cpp.opencv_highgui.*;

import android.test.AndroidTestCase;

// Tests whether faces are recognized, using images from a testing set, that is seperate from the
// training set.
public class Test_FacePredictor extends AndroidTestCase {

	static FacePredictor facePredictor = null; 
	
    public void setUp() throws IOException {
    	if (facePredictor == null)
    		// perform one time setup
    		facePredictor = new FacePredictor(getContext());     
    }
	
	public void testRecognizeThirdPerson() throws IOException { 
		File imageFile = Loader.extractResource(getClass(),
				"/com/googlecode/javacv/facepreview/b_03_05.jpg",
				getContext().getCacheDir(), "image", ".jpg");
	    IplImage image = cvLoadImage(imageFile.getAbsolutePath());
	    String name = facePredictor.identify(image).first;
	    assertEquals("3", name);
	}	
	
	public void testRecognizeEightPerson() throws IOException { 
		File imageFile = Loader.extractResource(getClass(),
				"/com/googlecode/javacv/facepreview/b_08_05.jpg",
				getContext().getCacheDir(), "image", ".jpg");
	    IplImage image = cvLoadImage(imageFile.getAbsolutePath());
	    String name = facePredictor.identify(image).first;
	    assertEquals("8", name);
	}	
	
	public void testRecognizeForthPerson() throws IOException { 
		File imageFile = Loader.extractResource(getClass(),
				"/com/googlecode/javacv/facepreview/b_04_05.jpg",
				getContext().getCacheDir(), "image", ".jpg");
	    IplImage image = cvLoadImage(imageFile.getAbsolutePath());
	    String name = facePredictor.identify(image).first;
	    assertEquals("4", name);
	}	
	
	public void testRecognizeSixthPerson() throws IOException { 
		File imageFile = Loader.extractResource(getClass(),
				"/com/googlecode/javacv/facepreview/b_06_05.jpg",
				getContext().getCacheDir(), "image", ".jpg");
	    IplImage image = cvLoadImage(imageFile.getAbsolutePath());
	    String name = facePredictor.identify(image).first;
	    assertEquals("6", name);
	}	
	
	public void testTrivialRecognize2ndPerson() throws IOException { 
		// Use testing image from training set, as a sanity test
		File imageFile = Loader.extractResource(getClass(),
				"/com/googlecode/javacv/facepreview/a_02_05.jpg",
				getContext().getCacheDir(), "image", ".jpg");
	    IplImage image = cvLoadImage(imageFile.getAbsolutePath());
	    String name = facePredictor.identify(image).first;
	    assertEquals("2", name);
	}	
	
	public void testTrivialRecognize3rdPerson() throws IOException { 
		// Use testing image from training set, as a sanity test
		File imageFile = Loader.extractResource(getClass(),
				"/com/googlecode/javacv/facepreview/a_03_05.jpg",
				getContext().getCacheDir(), "image", ".jpg");
	    IplImage image = cvLoadImage(imageFile.getAbsolutePath());
	    String name = facePredictor.identify(image).first;
	    assertEquals("3", name);
	}	
	
	public void testTrivialRecognize4thPerson() throws IOException { 
		// Use testing image from training set, as a sanity test
		File imageFile = Loader.extractResource(getClass(),
				"/com/googlecode/javacv/facepreview/a_04_05.jpg",
				getContext().getCacheDir(), "image", ".jpg");
	    IplImage image = cvLoadImage(imageFile.getAbsolutePath());
	    String name = facePredictor.identify(image).first;
	    assertEquals("4", name);
	}	
	
	public void testTrivialRecognize5thPerson() throws IOException { 
		// Use testing image from training set, as a sanity test
		File imageFile = Loader.extractResource(getClass(),
				"/com/googlecode/javacv/facepreview/a_05_05.jpg",
				getContext().getCacheDir(), "image", ".jpg");
	    IplImage image = cvLoadImage(imageFile.getAbsolutePath());
	    String name = facePredictor.identify(image).first;
	    assertEquals("5", name);
	}	
	
	
}
