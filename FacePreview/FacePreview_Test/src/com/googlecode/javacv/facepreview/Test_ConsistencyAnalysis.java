package com.googlecode.javacv.facepreview;

import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_core.cvClearMemStorage;
import static com.googlecode.javacv.cpp.opencv_core.cvGetSeqElem;
import static com.googlecode.javacv.cpp.opencv_core.cvLoad;
import static com.googlecode.javacv.cpp.opencv_objdetect.CV_HAAR_FIND_BIGGEST_OBJECT;
import static com.googlecode.javacv.cpp.opencv_objdetect.cvHaarDetectObjects;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Calendar;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.provider.MediaStore;
import android.test.AndroidTestCase;

import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.cpp.opencv_core.CvMemStorage;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_objdetect;
import com.googlecode.javacv.cpp.opencv_objdetect.CvHaarClassifierCascade;
import com.googlecode.javacv.cpp.opencv_video.BackgroundSubtractorMOG2;
import com.googlecode.javacv.facepreview.compute.BackgroundConsistencyAnalysis;



public class Test_ConsistencyAnalysis extends AndroidTestCase {
	
	private BackgroundConsistencyAnalysis analysis = new BackgroundConsistencyAnalysis();
	private BackgroundSubtractorMOG2 backgroundSubtractor = new BackgroundSubtractorMOG2();
    private CvHaarClassifierCascade classifier;
    private CvMemStorage storage;
    private CvSeq faces;
	
	// TODO: refactor FaceViewWithAnalysis so we don't need to copy and paste into test
    public void setUp() throws Exception {
        // Load the classifier file from Java resources.
        File classifierFile = Loader.extractResource(getClass(),
            "/com/googlecode/javacv/facepreview/data/haarcascade_frontalface_alt.xml",
            getContext().getCacheDir(), "classifier", ".xml");
        if (classifierFile == null || classifierFile.length() <= 0) {
            throw new IOException("Could not extract the classifier file from Java resource.");
        }

        // Preload the opencv_objdetect module to work around a known bug.
        Loader.load(opencv_objdetect.class);
        classifier = new CvHaarClassifierCascade(cvLoad(classifierFile.getAbsolutePath()));
        classifierFile.delete();
        if (classifier.isNull()) {
            throw new IOException("Could not load the classifier file.");
        }
        storage = CvMemStorage.create();
    }
    
    // this test currently FAILS. Some constant factors need to be adjusted in order for it to succeed.
	// TODO: refactor this, so we don't duplicate code in the test
    public void testRealVideo() throws IOException, com.googlecode.javacv.FrameGrabber.Exception {

    	
    	File videoFile = Loader.extractResource(getClass(),
				"/com/googlecode/javacv/facepreview/dedicated_testdata/face_video_real.mp4",
		getContext().getCacheDir(), "video", ".mp4");
		
		 // Using android media class, since couldn't get opencv's functions to load videos properly
		 MediaMetadataRetriever media = new MediaMetadataRetriever();
		 media.setDataSource(videoFile.getAbsolutePath());
		 
		 int debugPictureCount = 0;
		 for (long time = 0; time < 15*1000000; time += 500000) { // every .2 second
			 // The original file resolution was 1280 x 720 pixels
			 Bitmap bitmap = media.getFrameAtTime(time);
			 					//MediaStore.Images.Media.insertImage(getContext().getContentResolver(), bitmap, "image" + Calendar.getInstance().get(Calendar.SECOND) + debugPictureCount++ , "temp");
			 // use a smaller bitmap, similar to the size of the one inside FaceViewWithAnalysis 
			 bitmap = Bitmap.createScaledBitmap(bitmap, 1280/8, 720/8, true);
			 
			 IplImage ipl = iplFromBitmap(bitmap);
			 IplImage foreground = IplImage.create(ipl.width(), ipl.height(), IPL_DEPTH_8U, 1);
			 
			 					//FacePredictor.debugPrintIplImage(ipl, getContext());
			 final double learningRate = 0.05;
			 backgroundSubtractor.apply(ipl, foreground, learningRate);
			 					//FacePredictor.debugPrintIplImage(ipl, getContext());
			 
			 cvClearMemStorage(storage);
			 faces = cvHaarDetectObjects(ipl, classifier, storage, 1.1, 3, CV_HAAR_FIND_BIGGEST_OBJECT);
			 
			 analysis.processNewFrame(foreground.getByteBuffer(), bitmap.getHeight(), bitmap.getWidth(), new CvRect(cvGetSeqElem(faces, 0)));
		 }
		 
		 assertTrue(analysis.pass());	    
    }    
    
    private IplImage iplFromBitmap(Bitmap map) {
        // probably not the best way to do conversion
    	
    	// gray scalify
    	for(int x = 0; x < map.getWidth(); ++x) {
            for(int y = 0; y < map.getHeight(); ++y) {
                // get one pixel color
                int pixel = map.getPixel(x, y);
                // retrieve color of all channels
                int A = Color.alpha(pixel);
                int R = Color.red(pixel);
                int G = Color.green(pixel);
                int B = Color.blue(pixel);
                // take conversion up to one single value
                R = G = B = (int)(0.299 * R + 0.587 * G + 0.114 * B);
                // set new pixel color to output bitmap
                map.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }
    	
    	int f = 1;
        int imageWidth  = map.getWidth();
        int imageHeight = map.getHeight();

        IplImage ipl = IplImage.create(imageWidth, imageHeight, IPL_DEPTH_8U, 1);
        ByteBuffer imageBuffer = ipl.getByteBuffer();
        int imageStride = ipl.widthStep();
        for (int y = 0; y < imageHeight; y++) {
            int imageLine = y*imageStride;
            for (int x = 0; x < imageWidth; x++) {
                imageBuffer.put(imageLine + x, (byte)(map.getPixel(x, y)&0xFF));
            }
        }
        return ipl;
    }

}