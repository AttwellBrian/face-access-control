package com.googlecode.javacv.facepreview;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.facepreview.FacePredictor;

import static com.googlecode.javacv.cpp.opencv_highgui.*;

import android.os.Debug;
import android.test.AndroidTestCase;
import android.util.Log;

// Tests whether faces are recognized, using images from a testing set, that is seperate from the
// training set.
public class Test_FacePredictor extends AndroidTestCase {

	static FacePredictor facePredictor = null; 
	
    public void setUp() throws Exception {
    	if (facePredictor == null) {
    		// perform one time setup for tests   
    		IplImage [] authorizedImages = {
    				cvLoadImage(Loader.extractResource(getClass(), "/com/googlecode/javacv/facepreview/data/authorized_1.jpg", getContext().getCacheDir(), "image", ".jpg").getAbsolutePath()),
    				cvLoadImage(Loader.extractResource(getClass(), "/com/googlecode/javacv/facepreview/data/authorized_2.jpg", getContext().getCacheDir(), "image", ".jpg").getAbsolutePath()),
    				cvLoadImage(Loader.extractResource(getClass(), "/com/googlecode/javacv/facepreview/data/authorized_3.jpg", getContext().getCacheDir(), "image", ".jpg").getAbsolutePath())
    		};
    		facePredictor = new FacePredictor(getContext(), authorizedImages);
    	}
    }
	
    public void testSerializedPredictor() throws IOException {
		// Save LBPH histograms to file, and check that they match expectations
    	// (For now, just print it to the screen)
		File file = new File(getContext().getFilesDir(), "face-predictor.txt");
		facePredictor.algorithm.save(file.getAbsolutePath());
		
		FileInputStream instream = new FileInputStream(getContext().getFilesDir().getAbsolutePath()+"/face-predictor.txt");
		InputStreamReader inputreader = new InputStreamReader(instream);
		BufferedReader buffreader = new BufferedReader(inputreader);
		
		assertNotNull(buffreader.readLine());
    }
    
	public void testRecognizeThirdPerson() throws IOException { 
		File imageFile = Loader.extractResource(getClass(),
				"/com/googlecode/javacv/facepreview/data/b_03_05.jpg",
				getContext().getCacheDir(), "image", ".jpg");
	    IplImage image = cvLoadImage(imageFile.getAbsolutePath());
	    String name = facePredictor.identify(image).first;
	    assertEquals("3", name);
	}	
	
	public void testRecognizeEightPerson() throws IOException { 
		File imageFile = Loader.extractResource(getClass(),
				"/com/googlecode/javacv/facepreview/data/b_08_05.jpg",
				getContext().getCacheDir(), "image", ".jpg");
	    IplImage image = cvLoadImage(imageFile.getAbsolutePath());
	    String name = facePredictor.identify(image).first;
	    assertEquals("8", name);
	}	
	
	public void testRecognizeForthPerson() throws IOException { 
		File imageFile = Loader.extractResource(getClass(),
				"/com/googlecode/javacv/facepreview/data/b_04_05.jpg",
				getContext().getCacheDir(), "image", ".jpg");
	    IplImage image = cvLoadImage(imageFile.getAbsolutePath());
	    String name = facePredictor.identify(image).first;
	    assertEquals("4", name);
	}	
	
	public void testRecognizeSixthPerson() throws IOException { 
		File imageFile = Loader.extractResource(getClass(),
				"/com/googlecode/javacv/facepreview/data/b_06_05.jpg",
				getContext().getCacheDir(), "image", ".jpg");
	    IplImage image = cvLoadImage(imageFile.getAbsolutePath());
	    String name = facePredictor.identify(image).first;
	    assertEquals("6", name);
	}	
	
	public void testTrivialRecognize2ndPerson() throws IOException { 
		// Use testing image from training set, as a sanity test
		File imageFile = Loader.extractResource(getClass(),
				"/com/googlecode/javacv/facepreview/data/a_02_05.jpg",
				getContext().getCacheDir(), "image", ".jpg");
	    IplImage image = cvLoadImage(imageFile.getAbsolutePath());
	    String name = facePredictor.identify(image).first;
	    assertEquals("2", name);
	}	
	
	public void testTrivialRecognize3rdPerson() throws IOException { 
		// Use testing image from training set, as a sanity test
		File imageFile = Loader.extractResource(getClass(),
				"/com/googlecode/javacv/facepreview/data/a_03_05.jpg",
				getContext().getCacheDir(), "image", ".jpg");
	    IplImage image = cvLoadImage(imageFile.getAbsolutePath());
	    String name = facePredictor.identify(image).first;
	    assertEquals("3", name);
	}	
	
	public void testTrivialRecognize4thPerson() throws IOException { 
		// Use testing image from training set, as a sanity test
		File imageFile = Loader.extractResource(getClass(),
				"/com/googlecode/javacv/facepreview/data/a_04_05.jpg",
				getContext().getCacheDir(), "image", ".jpg");
	    IplImage image = cvLoadImage(imageFile.getAbsolutePath());
	    String name = facePredictor.identify(image).first;
	    assertEquals("4", name);
	}	
	
	public void testTrivialRecognize5thPerson() throws IOException { 
		// Use testing image from training set, as a sanity test
		File imageFile = Loader.extractResource(getClass(),
				"/com/googlecode/javacv/facepreview/data/a_05_05.jpg",
				getContext().getCacheDir(), "image", ".jpg");
	    IplImage image = cvLoadImage(imageFile.getAbsolutePath());
	    String name = facePredictor.identify(image).first;
	    assertEquals("5", name);
	}	
	
	public void testAuthentication() throws IOException {
		// Use testing image from training set, as a sanity test
		File imageFile = Loader.extractResource(getClass(),
				"/com/googlecode/javacv/facepreview/data/authorized_1.jpg",
				getContext().getCacheDir(), "image", ".jpg");
	    IplImage image = cvLoadImage(imageFile.getAbsolutePath());
		assertTrue(facePredictor.authenticate(image));
		
		// Perform more than a sanity check.
		imageFile = Loader.extractResource(getClass(),
				"/com/googlecode/javacv/facepreview/data/authorized_test.jpg",
				getContext().getCacheDir(), "image", ".jpg");
	    image = cvLoadImage(imageFile.getAbsolutePath());
	    String name = facePredictor.identify(image).first;
	    assertEquals("11", name);
	    // checks the same thing as the above line, for now
		assertTrue(facePredictor.authenticate(image));
	}
	
	
}
