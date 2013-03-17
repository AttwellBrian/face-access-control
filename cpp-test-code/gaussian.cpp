// Usage of bgm
// I also thought this was good: http://kom.aau.dk/~zt/cources/Readings_in_VGIS_2009/cvGaussianBGModelDemo.cpp

#include <iostream>
#include <cv.h>
#include <highgui.h>

#include "opencv2/core/core.hpp"
#include "opencv2/contrib/contrib.hpp"
#include "opencv2/highgui/highgui.hpp"

#include <ctype.h>
#include <stdio.h>
#include <iostream>
#include <vector>

#include "cvaux.h"
 
int main(int argc, char *argv[])
{
    cv::Mat frame;
    cv::Mat back;
    cv::Mat fore; // binary mask for foreground
    cv::VideoCapture cap(0);
    cv::BackgroundSubtractorMOG2 bg;
    //bg.nmixtures = 3;
    //bg.bShadowDetection = false;
 
    std::vector<std::vector<cv::Point> > contours;
 
    cv::namedWindow("Forground");
    cv::namedWindow("Frame");
    cv::namedWindow("Background");
 
    for(;;)
    {
        cap >> frame;
        bg.operator ()(frame,fore);
        bg.getBackgroundImage(back);
        cv::erode(fore,fore,cv::Mat());
        cv::dilate(fore,fore,cv::Mat());
        cv::findContours(fore,contours,CV_RETR_EXTERNAL,CV_CHAIN_APPROX_NONE);
        cv::drawContours(frame,contours,-1,cv::Scalar(0,0,255),2);
        cv::imshow("Forground",fore);
        cv::imshow("Frame",frame);
        cv::imshow("Background",back);
        if(cv::waitKey(30) >= 0) break;
    }
    return 0;
}