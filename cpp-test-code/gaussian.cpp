/* Demo of the background/foreground detection algorithme */
/* Author: Francois Cauwe */
#include <iostream>
#include <cv.h>
#include <highgui.h>

#include "opencv2/core/core.hpp"
#include "opencv2/contrib/contrib.hpp"
#include "opencv2/highgui/highgui.hpp"

#include <ctype.h>
#include <stdio.h>

#include "cvaux.h"

int main(int argc, char** argv)
{

    /* Start capturing */
    CvCapture* capture = 0;

    if( argc == 1 || (argc == 2 && strlen(argv[1]) == 1 && isdigit(argv[1][0]))) {
        capture = cvCaptureFromCAM( argc == 2 ? argv[1][0] - '0' : 0 );
    } else if( argc == 2 )
        capture = cvCaptureFromAVI( argv[1] );

    if( !capture )
    {
        fprintf(stderr,"Could not initialize...\n");
        return -1;
    }

    /* print a welcome message, and the OpenCV version */
    printf ("Demo of the background classification using CvGaussBGModel %s (%d.%d.%d)\n",
        CV_VERSION,
        CV_MAJOR_VERSION, CV_MINOR_VERSION, CV_SUBMINOR_VERSION);

    /* Capture 1 video frame for initialization */
    IplImage* videoFrame = NULL;
    videoFrame = cvQueryFrame(capture);

    if(!videoFrame)
    {
        printf("Bad frame \n");
        exit(0);
    }

    // Create windows
    cvNamedWindow("BG", 1);
    cvNamedWindow("FG", 1);

    // Select parameters for Gaussian model.
    CvGaussBGStatModelParams* params = new CvGaussBGStatModelParams;                        
    params->win_size=2; 
    params->n_gauss=5;
    params->bg_threshold=0.7;
    params->std_threshold=3.5;
    params->minArea=15;
    params->weight_init=0.05;
    params->variance_init=30; 

    // Creat CvBGStatModel
    // cvCreateGaussianBGModel( IplImage* first_frame, CvGaussBGStatModelParams* parameters )
    // or
    // cvCreateGaussianBGModel( IplImage* first_frame )
    CvBGStatModel* bgModel = cvCreateGaussianBGModel(videoFrame ,params);

    int key=-1;
    while(key != 'q')
    {
        // Grab a fram
        videoFrame = cvQueryFrame(capture);
        if( !videoFrame )
            break;
        
        // Update model
        cvUpdateBGStatModel(videoFrame,bgModel);
        
        // Display results
        cvShowImage("BG", bgModel->background);
        cvShowImage("FG", bgModel->foreground); 
        key = cvWaitKey(10);
    }

    cvDestroyWindow("BG");
    cvDestroyWindow("FG");
    cvReleaseBGStatModel( &bgModel );
    cvReleaseCapture(&capture);
    return 0;
}
