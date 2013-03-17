package com.googlecode.javacv.facepreview;

import java.util.ArrayList;

public class BackgroundConsistencyAnalysis {

	// last 100 frames: number of pixels in face that were considered inside the face
	private ArrayList<Integer> motionTrendFace = new ArrayList<Integer>(100); 
	private ArrayList<Integer> motionTrendNotFace = new ArrayList<Integer>(100); // last 100 frames
	
}
