package com.googlecode.javacv.facepreview;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.provider.MediaStore;
import android.text.format.Time;
import android.util.Pair;

import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.cpp.opencv_contrib.FaceRecognizer;
import com.googlecode.javacv.cpp.opencv_core.CvMat;
import com.googlecode.javacv.cpp.opencv_core.CvMemStorage;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_core.MatVector;
import com.googlecode.javacv.cpp.opencv_objdetect.CvHaarClassifierCascade;

import static com.googlecode.javacv.cpp.opencv_core.CV_32SC1;
import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_core.cvClearMemStorage;
import static com.googlecode.javacv.cpp.opencv_core.cvCreateMat;
import static com.googlecode.javacv.cpp.opencv_core.cvGetSeqElem;
import static com.googlecode.javacv.cpp.opencv_core.cvLoad;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_BGR2GRAY;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_INTER_AREA;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCvtColor;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvResize;
import static com.googlecode.javacv.cpp.opencv_objdetect.CV_HAAR_DO_CANNY_PRUNING;
import static com.googlecode.javacv.cpp.opencv_objdetect.cvHaarDetectObjects;

import com.googlecode.javacv.cpp.opencv_core.CvMat;
import com.googlecode.javacv.cpp.opencv_core.CvMemStorage;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_core.MatVector;
import com.googlecode.javacv.cpp.opencv_objdetect.CvHaarClassifierCascade;
import com.googlecode.javacv.cpp.opencv_objdetect;


import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;
import static com.googlecode.javacv.cpp.opencv_objdetect.*;
import static com.googlecode.javacv.cpp.opencv_highgui.*;

// minimally based off of https://github.com/pathikrit/JFaceRecog/blob/master/src/lib/FacialRecognition.java
public class FacePredictor {
      
    // We can try out different algorithms here: http://docs.opencv.org/trunk/modules/contrib/doc/facerec/facerec_api.html
    private static final Double THRESHHOLD = 80d;
    private static final FaceRecognizer ALGO_FACTORY =
        com.googlecode.javacv.cpp.opencv_contrib.createLBPHFaceRecognizer(1, 8, 8, 8, THRESHHOLD);
        //com.googlecode.javacv.cpp.opencv_contrib.createFisherFaceRecognizer(0, THRESHHOLD);
        //com.googlecode.javacv.cpp.opencv_contrib.createEigenFaceRecognizer(0, THRESHHOLD);
    private static final Pair<Integer, Integer> scale = new Pair<Integer, Integer>(100, 100);

    private static final Map<Integer, String> names = new HashMap<Integer, String>();
    private final FaceRecognizer algorithm;

    private Context context;
    
    private static void debugPrintIplImage(IplImage src, Context context) {
    	Bitmap tmpbitmap = IplImageToBitmap(src);
        MediaStore.Images.Media.insertImage(context.getContentResolver(), tmpbitmap, "image" + Calendar.getInstance().get(Calendar.SECOND) + debugPictureCount++ , "temp");
    }
    
    private static Bitmap IplImageToBitmap(IplImage src) {
        int width = src.width();
        int height = src.height();
        int smallFactor = 8;
        Bitmap bitmap = Bitmap.createBitmap(width/smallFactor, height/smallFactor, Bitmap.Config.ARGB_8888);
        for(int r=0;r<height;r+=smallFactor) {
            for(int c=0;c<width;c+=smallFactor) {
                int gray = (int) Math.floor(cvGet2D(src,r,c).getVal(0));
                bitmap.setPixel(c/smallFactor, r/smallFactor, Color.argb(255, gray, gray, gray));
            }
        }
        return bitmap;
    }
    
  private void addNameAndFace(String fileName, int imgCount, int personCount, MatVector images, CvMat labels) throws IOException {
      File imageFile = Loader.extractResource(getClass(), fileName,
              context.getCacheDir(), "image", ".jpg");
      IplImage image = cvLoadImage(imageFile.getAbsolutePath());
      IplImage grayImage = IplImage.create(image.width(), image.height(), IPL_DEPTH_8U, 1);
      cvCvtColor(image, grayImage, CV_BGR2GRAY);
 
      CvRect faceRectangle = detectFace(grayImage);
      images.put(imgCount, toTinyGray(image, faceRectangle));
      labels.put(imgCount, personCount);
      String name = new Integer(personCount).toString();
      names.put(personCount, name);	  
  }
    
  public FacePredictor(Context context) throws IOException {
    
    this.context = context;
    
    // Load the classifier file from Java resources.
    File classifierFile = Loader.extractResource(getClass(),
        "/com/googlecode/javacv/facepreview/haarcascade_frontalface_alt.xml",
        context.getCacheDir(), "classifier", ".xml");
    if (classifierFile == null || classifierFile.length() <= 0) {
        throw new IOException("Could not extract the classifier file from Java resource.");
    }
    // Preload the opencv_objdetect module to work around a known bug.
    Loader.load(opencv_objdetect.class);
    classifier = new CvHaarClassifierCascade(cvLoad(classifierFile.getAbsolutePath()));
    
    final int numberOfImages = (8+1)*3; // TODO: calculate this more smartly.. maybe don't need to calculate
    final MatVector images = new MatVector(numberOfImages);
    final CvMat labels = cvCreateMat(1, numberOfImages, CV_32SC1);
   
    // TODO: process these images ahead of time (otherwise startup will take several minutes)
    int imgCount = 0;
    for (int personCount = 2; personCount < 10; personCount++) { // training people 2-10
    	// TODO: use a couple images per person. We have the four images per person available. I'm just not using them.
        String fileName = String.format("/com/googlecode/javacv/facepreview/a_%02d_05.jpg", personCount);
        addNameAndFace(fileName, imgCount, personCount, images, labels);
        imgCount++;
        
        fileName = String.format("/com/googlecode/javacv/facepreview/a_%02d_15.jpg", personCount);
        addNameAndFace(fileName, imgCount, personCount, images, labels);
        imgCount++;
        
        fileName = String.format("/com/googlecode/javacv/facepreview/b_%02d_15.jpg", personCount);
        addNameAndFace(fileName, imgCount, personCount, images, labels);
        imgCount++;
        
    }
    addNameAndFace("/com/googlecode/javacv/facepreview/authorized_1.jpg", imgCount, 11, images, labels);
    addNameAndFace("/com/googlecode/javacv/facepreview/authorized_2.jpg", imgCount+1, 11, images, labels);
    addNameAndFace("/com/googlecode/javacv/facepreview/authorized_2.jpg", imgCount+2, 11, images, labels);
    
    // TODO: add some asserts
    assert (numberOfImages == labels.size());
    
    this.algorithm = ALGO_FACTORY;
    algorithm.train(images, labels);
  }
  
  /**
   * Identify the face in bounding box r in image
   */
  Pair<String, Double> identify(IplImage image, CvRect face) {
    final IplImage iplImage = toTinyGray(image, face);
    final int[] prediction = new int[1];
    final double[] confidence = new double[1];
    algorithm.predict(iplImage, prediction, confidence);
    String name = names.get(prediction[0]);
    Double confidence_ = 100*(THRESHHOLD - confidence[0])/THRESHHOLD;
    return new Pair<String, Double>(name, confidence_); 
  }
    
  public Pair<String, Double> identify(IplImage image) {
    IplImage grayImage = IplImage.create(image.width(), image.height(), IPL_DEPTH_8U, 1);
    cvCvtColor(image, grayImage, CV_BGR2GRAY);
    CvRect faceRectangle = detectFace(grayImage);
	  
    final IplImage iplImage = toTinyGray(image, faceRectangle);
    final int[] prediction = new int[1];
    final double[] confidence = new double[1];
    algorithm.predict(iplImage, prediction, confidence);
    String name = names.get(prediction[0]);
    Double confidence_ = 100*(THRESHHOLD - confidence[0])/THRESHHOLD;

    // we return the identity with the highest confidence rating 
    for (int i = 0; i < confidence.length; i++) {
    	assert(confidence_ >= confidence[i]);
    }
    return new Pair<String, Double>(name, confidence_); 
  }
    

  private static final CvMemStorage storage = CvMemStorage.create();
  private static final int F = 4; // scaling factor
  private static CvHaarClassifierCascade classifier ;

  static int debugPictureCount = 0;
  /**
   * This does facial detection and NOT facial recognition
   */
  private synchronized CvRect detectFace(IplImage image) {
	cvClearMemStorage(storage);
    
    final CvSeq cvSeq = cvHaarDetectObjects(image, classifier, storage, 1.1, 3, CV_HAAR_DO_CANNY_PRUNING);
    assert ( cvSeq.total() > 0);
    return  new CvRect(cvGetSeqElem(cvSeq, 0));
  }

  /**
   * Images should be grayscaled and scaled-down for faster calculations
   */
  private IplImage toTinyGray(IplImage image, CvRect r) {
      IplImage gray = cvCreateImage(cvGetSize(image), IPL_DEPTH_8U, 1);
      IplImage roi = cvCreateImage(cvGetSize(image), IPL_DEPTH_8U, 1);
      CvRect r1 = new CvRect(r.x()-10, r.y()-10, r.width()+10, r.height()+10);
      cvCvtColor(image, gray, CV_BGR2GRAY);
      cvSetImageROI(gray, r1);
      cvResize(gray, roi, CV_INTER_LINEAR);
      cvEqualizeHist(roi, roi);
      	debugPrintIplImage(roi, context);
      return roi;
  }
}