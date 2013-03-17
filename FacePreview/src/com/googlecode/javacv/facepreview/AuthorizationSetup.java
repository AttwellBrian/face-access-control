/*
 * FacePreview - A fusion of OpenCV's facedetect and Android's CameraPreview samples,
 *               with JavaCV + JavaCPP as the glue in between.
 *
 * This file was based on CameraPreview.java that came with the Samples for 
 * Android SDK API 8, revision 1 and contained the following copyright notice:
 *
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.googlecode.javacv.facepreview;

import static com.googlecode.javacv.cpp.opencv_core.cvGet2D;
import static com.googlecode.javacv.cpp.opencv_highgui.cvSaveImage;

import java.io.File;
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
import com.googlecode.javacv.facepreview.views.Preview;

// NOTE: currently the classifier doesn't work, since we are using too high an API.
public class AuthorizationSetup extends Activity {
    private FrameLayout layout;
    private FaceView faceView;
    private Preview mPreview;

    public LinkedList<IplImage> grayImages = new LinkedList<IplImage>();//TODO: delete this since I don't read the results from it
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Hide the window title.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        // Create our Preview view and set it as the content of our activity.
        try {
            layout = new FrameLayout(this);
            faceView = new FaceView(this);
            mPreview = new Preview(this, faceView);
            layout.addView(mPreview);
            layout.addView(faceView);
            setContentView(layout);
            faceView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (grayImages.size() >= 3) {
						return;
					}
					
					// TODO: instead of copying grayImage, copy the full image.
					// since this gets called on UI thread, shouldn't be any issue directly grabbing the entire grayImage
					try {
						IplImage copiedImage = new IplImage(faceView.grayImage);
						grayImages.add(copiedImage);
					} catch (Exception e) {
						// catch phony exception that gets thrown as warning
					}
					
					
					// TODO: initialize faceView.displayedText  somewhere
					
					// long running operation on UI thread...
					//debugPrintIplImage(copiedImage, FacePreview.this);
					
					CharSequence text = "First image is securely set";
					if (grayImages.size() == 1) {
						faceView.displayedText = "Tap the screen to set a SECOND picture of your face - This side up.";
						File file = new File(getApplicationContext().getExternalFilesDir(null), "face_image_1.jpg");
						cvSaveImage(file.getAbsolutePath(), faceView.grayImage);
					} else if (grayImages.size() == 2) {
						text = "Second image is securely set";
						faceView.displayedText = "Tap the screen to set the FINAL picture of your face - This side up.";
						File file = new File(getApplicationContext().getExternalFilesDir(null), "face_image_2.jpg");
						cvSaveImage(file.getAbsolutePath(), faceView.grayImage);
					} else if (grayImages.size() == 3) {
						text = "Final image is securely set";
						File file = new File(getApplicationContext().getExternalFilesDir(null), "face_image_3.jpg");
						cvSaveImage(file.getAbsolutePath(), faceView.grayImage);
						
						// Tell the user they are finished with their authorization. Don't let them
						// return to the current activity, since you aren't allowed to perform setup twice.
						Intent intent = new Intent(AuthorizationSetup.this, CompletedAuthorization.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
						AuthorizationSetup.this.startActivity(intent);	
					}
					
					int duration = Toast.LENGTH_SHORT;
					Toast toast = Toast.makeText(getApplicationContext(), text, duration);
					toast.show();
					
				}
			});
        } catch (IOException e) {
            e.printStackTrace();
            new AlertDialog.Builder(this).setMessage(e.getMessage()).create().show();
        }
        
    }
    
    
/*
    // todo; delete
    static int debugPictureCount = 0;
    private static void debugPrintIplImage(IplImage src, Context context) {
    	Bitmap tmpbitmap = IplImageToBitmap(src);
        MediaStore.Images.Media.insertImage(context.getContentResolver(), tmpbitmap, "image" + Calendar.getInstance().get(Calendar.SECOND) + debugPictureCount++ , "temp");
    }
    
    // todo; delete
    private static Bitmap IplImageToBitmap(IplImage src) {//don't need to do this anymore... can use the cvSave function
        int width = src.width();
        int height = src.height();
        int smallFactor = 1;
        Bitmap bitmap = Bitmap.createBitmap(width/smallFactor, height/smallFactor, Bitmap.Config.ARGB_8888);
        for(int r=0;r<height/smallFactor;r+=1) {
            for(int c=0;c<width/smallFactor;c+=1) {
                int gray = (int) Math.floor(cvGet2D(src,r*smallFactor,c*smallFactor).getVal(0));
                bitmap.setPixel(c, r, Color.argb(255, gray, gray, gray));
            }
        }
        return bitmap;
    }
*/
    
}
