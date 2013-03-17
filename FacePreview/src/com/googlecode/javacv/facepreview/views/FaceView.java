package com.googlecode.javacv.facepreview.views;

import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_core.cvClearMemStorage;
import static com.googlecode.javacv.cpp.opencv_core.cvGetSeqElem;
import static com.googlecode.javacv.cpp.opencv_core.cvLoad;
import static com.googlecode.javacv.cpp.opencv_highgui.cvSaveImage;
import static com.googlecode.javacv.cpp.opencv_objdetect.CV_HAAR_DO_CANNY_PRUNING;
import static com.googlecode.javacv.cpp.opencv_objdetect.cvHaarDetectObjects;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Toast;

import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.cpp.opencv_core.CvMemStorage;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.CvScalar;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_objdetect;
import com.googlecode.javacv.cpp.opencv_objdetect.CvHaarClassifierCascade;
import com.googlecode.javacv.facepreview.RecognizerService;


// can we use startFaceDetection on camera? probably not
public class FaceView extends View implements Camera.PreviewCallback {
    public static final int SUBSAMPLING_FACTOR = 4;

    public IplImage grayImage;
    public String displayedText = "Tap the screen to set your face - This side up.";    
    
    private CvHaarClassifierCascade classifier;
    private CvMemStorage storage;
    private CvSeq faces;
    
    public FaceView(Context context) throws IOException {
        super(context);
 
        // Load the classifier file from Java resources.
        File classifierFile = Loader.extractResource(getClass(),
            "/com/googlecode/javacv/facepreview/data/haarcascade_frontalface_alt.xml",
            context.getCacheDir(), "classifier", ".xml");
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
    
    public void onPreviewFrame(final byte[] data, final Camera camera) {
        try {
            Camera.Size size = camera.getParameters().getPreviewSize();
            processImage(data, size.width, size.height);
            camera.addCallbackBuffer(data);
        } catch (RuntimeException e) {
            // The camera has probably just been released, ignore.
        	System.err.println(e.toString());
        }
    }

    public interface FaceViewImageCallback {
    	void image(IplImage image /*BGR*/);
    }
    public void setFaceViewImageCallback(FaceViewImageCallback callback) {
    	mCallback = callback;
    }
    FaceViewImageCallback mCallback = null;
    
    // TODO: this more efficienty using built in API, or parallel for http://stackoverflow.com/questions/4010185/parallel-for-for-java
    protected void processImage(byte[] data, int width, int height) {
        // First, downsample our image and convert it into a grayscale IplImage
        int f = SUBSAMPLING_FACTOR;
        if (grayImage == null || grayImage.width() != width/f || grayImage.height() != height/f) {
        	try {
        		grayImage = IplImage.create(width/f, height/f, IPL_DEPTH_8U, 1);
        	} catch (Exception e) {
        		// ignore exception. It is only a warning in this case
        		System.err.println(e.toString());
        	}
        }
   
    	// TODO: spead this up
        int imageWidth  = grayImage.width();
        int imageHeight = grayImage.height();
        int dataStride = f*width;
        int imageStride = grayImage.widthStep();
        ByteBuffer imageBuffer = grayImage.getByteBuffer();
        for (int y = 0; y < imageHeight; y++) {
            int dataLine = y*dataStride;
            int imageLine = y*imageStride;
            for (int x = 0; x < imageWidth; x++) {
                imageBuffer.put(imageLine + x, data[dataLine + f*x]);
            }
        }

        if (mCallback != null) {
            if (debugPictureCount == 0) {
            	//debugPrintIplImage(grayImage, this.getContext());
            }
            mCallback.image(grayImage);
        }
        
        
        // The following 12 lines perform the following asynchronously:
        //		cvClearMemStorage(storage);
        // 		faces = cvHaarDetectObjects(grayImage, classifier, storage, 1.1, 3, CV_HAAR_DO_CANNY_PRUNING);
        // 		postInvalidate();
        if (!currentlyProcessing) {
        	currentlyProcessing = true;
            final IplImage imageCopy = grayImage.clone();
			new AsyncTask<Void, Void, Void>() {
				@Override
				protected Void doInBackground(Void... params) {
					cvClearMemStorage(storage);
					faces = cvHaarDetectObjects(imageCopy, classifier, storage, 1.1, 3, CV_HAAR_DO_CANNY_PRUNING); //note that I perform redundant calls to this. I later call this again inside the predictor
					return null;
				}
				@Override
				protected void onPostExecute(Void result) {
					postInvalidate();	
					currentlyProcessing = false;
				}
			}.execute();
        }
    }

    private boolean currentlyProcessing = false; // note: only access this in main thread
    
    // todo: delete
    static int debugPictureCount = 0;
    private static void debugPrintIplImage(IplImage src, Context context) {
    	File file = new File(context.getExternalFilesDir(null), "testimage_same.jpg");
    	cvSaveImage(file.getAbsolutePath(), src);
    	debugPictureCount++;
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setTextSize(20);

        float textWidth = paint.measureText(displayedText);
        canvas.drawText(displayedText, (getWidth()-textWidth)/2, 20, paint);

        if (faces != null) {
            paint.setStrokeWidth(2);
            paint.setStyle(Paint.Style.STROKE);
            float scaleX = (float)getWidth()/grayImage.width();
            float scaleY = (float)getHeight()/grayImage.height();
            int total = faces.total();
            for (int i = 0; i < total; i++) {
                CvRect r = new CvRect(cvGetSeqElem(faces, i));
                int x = r.x(), y = r.y(), w = r.width(), h = r.height();
                //Commented out code works if using back facing camera
                //canvas.drawRect(x*scaleX, y*scaleY, (x+w)*scaleX, (y+h)*scaleY, paint);
                canvas.drawRect(getWidth()-x*scaleX, y*scaleY, getWidth()-(x+w)*scaleX, (y+h)*scaleY, paint);
            }
        }
    }
}
