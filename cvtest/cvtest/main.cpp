/**
 * @file objectDetection2.cpp
 * @author A. Huaman ( based in the classic facedetect.cpp in samples/c )
 * @brief A simplified version of facedetect.cpp, show how to load a cascade classifier and how to find objects (Face + eyes) in a video stream - Using LBP here
 */
#include "opencv2/objdetect/objdetect.hpp"
#include "opencv2/highgui/highgui.hpp"
#include "opencv2/imgproc/imgproc.hpp"
#include "precomp.hpp"

#include "opencv2/core/core.hpp"
#include "opencv2/contrib/contrib.hpp"

#include <iostream>
#include <sstream>
#include <stdio.h>

using namespace std;
using namespace cv;

/** Function Headers */
void detectAndDisplay( Mat frame );

/** Global variables */
string face_cascade_name = "lbpcascade_frontalface.xml";
string eyes_cascade_name = "haarcascade_eye_tree_eyeglasses.xml";
CascadeClassifier face_cascade;
CascadeClassifier eyes_cascade;
string window_name = "Capture - Face detection";

static Mat
histc_(const Mat& src, int minVal=0, int maxVal=255, bool normed=false)
{
  Mat result;
  // Establish the number of bins.
  int histSize = maxVal-minVal+1;
  // Set the ranges.
  float range[] = { static_cast<float>(minVal), static_cast<float>(maxVal+1) };
  const float* histRange = { range };
  // calc histogram
  calcHist(&src, 1, 0, Mat(), result, 1, &histSize, &histRange, true, false);
  // normalize
  if(normed) {
    result /= (int)src.total();
  }
  return result.reshape(1,1);
}

static Mat histc(InputArray _src, int minVal, int maxVal, bool normed)
{
  Mat src = _src.getMat();
  switch (src.type()) {
    case CV_8SC1:
      return histc_(Mat_<float>(src), minVal, maxVal, normed);
      break;
    case CV_8UC1:
      return histc_(src, minVal, maxVal, normed);
      break;
    case CV_16SC1:
      return histc_(Mat_<float>(src), minVal, maxVal, normed);
      break;
    case CV_16UC1:
      return histc_(src, minVal, maxVal, normed);
      break;
    case CV_32SC1:
      return histc_(Mat_<float>(src), minVal, maxVal, normed);
      break;
    case CV_32FC1:
      return histc_(src, minVal, maxVal, normed);
      break;
    default:
      CV_Error(CV_StsUnmatchedFormats, "This type is not implemented yet."); break;
  }
  return Mat();
}


static Mat spatial_histogram(InputArray _src, int numPatterns,
                             int grid_x, int grid_y, bool /*normed*/)
{
  Mat src = _src.getMat();
  // calculate LBP patch size
  int width = src.cols/grid_x;
  int height = src.rows/grid_y;
  // allocate memory for the spatial histogram
  Mat result = Mat::zeros(grid_x * grid_y, numPatterns, CV_32FC1);
  // return matrix with zeros if no data was given
  if(src.empty())
    return result.reshape(1,1);
  // initial result_row
  int resultRowIdx = 0;
  // iterate through grid
  for(int i = 0; i < grid_y; i++) {
    for(int j = 0; j < grid_x; j++) {
      Mat src_cell = Mat(src, Range(i*height,(i+1)*height), Range(j*width,(j+1)*width));
      Mat cell_hist = histc(src_cell, 0, (numPatterns-1), true);
      // copy to the result matrix
      Mat result_row = result.row(resultRowIdx);
      cell_hist.reshape(1,1).convertTo(result_row, CV_32FC1);
      // increase row count in result matrix
      resultRowIdx++;
    }
  }
  
  return result;
}

static Mat gen_matrix(int width, int height) {
  // d x (1) random matrix
  Mat proj_matrix (Size(width, height), CV_32FC1);
  
  // fill matrix with uniformly distributed random numbers.
  randu(proj_matrix, Scalar::all(1.0), Scalar::all(0.0));
  return proj_matrix;
}


static Mat projected_histogram(InputArray src) {
  // (1) X M feature matrix

  Mat src_mat = src.getMat();
  
  Mat feature_vecs = src_mat.reshape(1, 1);
  transpose(feature_vecs, feature_vecs);
  
  // d x (1) random matrix
  static Mat proj_matrix = gen_matrix(feature_vecs.rows, 2048);
  
  // return result as reshaped feature vector
  Mat last = proj_matrix * feature_vecs;
  
  /*for (int j = 0; j < last.rows; j++) {
    cout << last.at<float>(0, j) << endl;
  }*/
  
  return last.reshape(1, 64);
}

//------------------------------------------------------------------------------
// cv::elbp
//------------------------------------------------------------------------------
namespace cv {
  template <typename _Tp> static
  inline void elbp_(InputArray _src, OutputArray _dst, int radius, int neighbors) {
    //get matrices
    Mat src = _src.getMat();
    // allocate memory for result
    _dst.create(src.rows-2*radius, src.cols-2*radius, CV_32SC1);
    Mat dst = _dst.getMat();
    // zero
    dst.setTo(0);
    for(int n=0; n<neighbors; n++) {
      // sample points
      float x = static_cast<float>(-radius) * sin(2.0*CV_PI*n/static_cast<float>(neighbors));
      float y = static_cast<float>(radius) * cos(2.0*CV_PI*n/static_cast<float>(neighbors));
      // relative indices
      int fx = static_cast<int>(floor(x));
      int fy = static_cast<int>(floor(y));
      int cx = static_cast<int>(ceil(x));
      int cy = static_cast<int>(ceil(y));
      // fractional part
      float ty = y - fy;
      float tx = x - fx;
      // set interpolation weights
      float w1 = (1 - tx) * (1 - ty);
      float w2 =      tx  * (1 - ty);
      float w3 = (1 - tx) *      ty;
      float w4 =      tx  *      ty;
      // iterate through your data
      for(int i=radius; i < src.rows-radius;i++) {
        for(int j=radius;j < src.cols-radius;j++) {
          // calculate interpolated value
          float t = w1*src.at<_Tp>(i+fy,j+fx) + w2*src.at<_Tp>(i+fy,j+cx) + w3*src.at<_Tp>(i+cy,j+fx) + w4*src.at<_Tp>(i+cy,j+cx);
          // floating point precision, so check some machine-dependent epsilon
          dst.at<int>(i-radius,j-radius) += ((t > src.at<_Tp>(i,j)) || (std::abs(t-src.at<_Tp>(i,j)) < std::numeric_limits<float>::epsilon())) << n;
        }
      }
    }
  }
  
}

void elbp(InputArray src, OutputArray dst, int radius, int neighbors) {
  switch (src.type()) {
    case CV_8SC1:   elbp_<char>(src,dst, radius, neighbors); break;
    case CV_8UC1:   elbp_<unsigned char>(src, dst, radius, neighbors); break;
    case CV_16SC1:  elbp_<short>(src,dst, radius, neighbors); break;
    case CV_16UC1:  elbp_<unsigned short>(src,dst, radius, neighbors); break;
    case CV_32SC1:  elbp_<int>(src,dst, radius, neighbors); break;
    case CV_32FC1:  elbp_<float>(src,dst, radius, neighbors); break;
    case CV_64FC1:  elbp_<double>(src,dst, radius, neighbors); break;
    default: break;
  }
}


//------------------------------------------------------------------------------
// cv::elbp, cv::olbp, cv::varlbp wrapper
//------------------------------------------------------------------------------

Mat elbp(InputArray src, int radius, int neighbors) {
  Mat dst;
  elbp(src, dst, radius, neighbors);
  return dst;
}

int main( void ) {
  // Train LBPH face recognizer
  vector<Mat> images;
  vector<int> labels;
  
  for (int i = 2; i <= 10; ++i) {
    char buff[100];
    sprintf(buff, "a_%02d_05.jpg", i);
    std::string path1 = buff;
    
    sprintf(buff, "a_%02d_15.jpg", i);
    std::string path2 = buff;
    
    sprintf(buff, "b_%02d_15.jpg", i);
    std::string path3 = buff;
    
    images.push_back(imread(path1, 0));
    images.push_back(imread(path2, 0));
    images.push_back(imread(path3, 0));
    labels.push_back(i);
    labels.push_back(i);
    labels.push_back(i);
  }
  
  cout << "Loaded images, training recognizer" << endl;
  
  Ptr<FaceRecognizer> model = createLBPHFaceRecognizer(1, 8, 8, 8, 70.0f);
  model->train(images, labels);
  
  // show histograms
  vector<Mat> histograms = model->getMatVector("histograms");
  
  const int USER_LABEL = 11;
  int num_images = 0;
  bool is_set = false;
  
  //-- 1. Load the cascade
  if( !face_cascade.load( face_cascade_name ) ){ printf("--(!)Error loading\n"); return -1; };
  
  //-- 2. Read the video stream
  CvCapture* capture = cvCaptureFromCAM( -1 );
  if( capture ) {
    for(;;) {
      Mat frame = cvQueryFrame( capture );
      resize(frame, frame, Size(840, 473));
      
      Mat frame_gray;
      cvtColor(frame, frame_gray, CV_BGR2GRAY);
      
      Mat formatted;
      frame_gray.convertTo(formatted, CV_32SC1);
      Mat destination = elbp(formatted, 1, 8);
      
      double minVal, maxVal;
      minMaxLoc(destination, &minVal, &maxVal); //find minimum and maximum intensities
      Mat draw;
      destination.convertTo(draw, CV_8U, 255.0/(maxVal - minVal), -minVal * 255.0/(maxVal - minVal));
      
      Mat histogram = spatial_histogram(destination, /* lbp_image */
                                       static_cast<int>(std::pow(2.0, static_cast<double>(8))), /* number of possible patterns */
                                       8, /* grid size x */
                                       8, /* grid size y */
                                       true);
      minMaxLoc(histogram, &minVal, &maxVal); //find minimum and maximum intensities
      
      Mat draw_histogram;
      histogram.convertTo(draw_histogram, CV_8U, 10 * 255.0/(maxVal - minVal), -minVal * 255.0/(maxVal - minVal));
      
      Size current_size = draw_histogram.size();
      Mat draw_histogram_bigger;
      resize(draw_histogram, draw_histogram_bigger, current_size * 3);
      
      Mat prj_histogram = projected_histogram(histogram);
      minMaxLoc(prj_histogram, &minVal, &maxVal); //find minimum and maximum intensities
      prj_histogram.convertTo(prj_histogram, CV_8U, 255.0/(maxVal - minVal), -minVal * 255.0/(maxVal - minVal));
      resize(prj_histogram, prj_histogram, prj_histogram.size() * 3);
      
      //-- 3. Apply the classifier to the frame
      std::vector<Rect> faces;
      if( !draw.empty() ){
        faces.clear();
        face_cascade.detectMultiScale( frame_gray, faces, 1.1, 2, 0, Size(80, 80) );

        if (faces.size() > 0) {
          try {
           Mat faceROI = frame_gray( faces[0] );
           //-- Draw the face
           imshow("Face", faceROI);
           Point center( faces[0].x + faces[0].width/2, faces[0].y + faces[0].height/2 );
           ellipse( frame, center, Size( faces[0].width/2, faces[0].height/2), 0, 0, 360, Scalar( 255, 0, 0 ), 2, 8, 0 );
          } catch(Exception& e) {
            
          }
        } else {
          destroyWindow("Face");
        }
        
        if (is_set) {
          if (faces.size() > 0) {
            Mat faceROI = frame_gray( faces[0] );
            int predicted = model->predict(faceROI);
            cout << "predicted: " << predicted << endl;
            if (predicted == USER_LABEL) {
              putText( frame, "PASS", Point(0, 60), 0,
                      2, Scalar( 0, 255, 0), 6, 8);
            } else {
              putText( frame, "FAIL", Point(0, 60), 0,
                      2, Scalar( 0, 0, 255), 6, 8);
            }
          } else {
            putText( frame, "FAIL", Point(0, 60), 0,
                    2, Scalar( 0, 0, 255), 6, 8);
          }
          
          char buff[100];
          sprintf(buff, "Number of Images: %d", num_images);
          std::string num_images_str = buff;
          
          putText( frame, num_images_str, Point(0, 80), 0,
                  1.0, Scalar( 255, 0, 0), 6, 5);
        } else {
          putText( frame, "NOT SET", Point(0, 60), 0,
                  2, Scalar( 255, 0, 0), 6, 8);
        }        
        
        imshow("Original Image", frame);
        
        imshow("LBPH Image", draw);
        imshow("Histogram", draw_histogram_bigger);
        imshow("Projected Histogram", prj_histogram);
        //detectAndDisplay( draw );
      }
      else
      { printf(" --(!) No captured frame -- Break!"); break; }
      
      int c = waitKey(10);
      if( (char)c == 'c' ) {
        break;
      } else if ( (char)c == 's') {
        if (faces.size() > 0) {
          try {
            Mat faceROI = frame_gray( faces[0] );
            images.push_back(faceROI);
            labels.push_back(USER_LABEL);
            model->update(images, labels);
            num_images += 1;
            is_set = true;
          } catch(Exception& e) {
            
          }
        }
      }
      
    }
  }
  return 0;
}
