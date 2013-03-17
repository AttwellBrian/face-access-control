project( gaussian )
find_package( OpenCV REQUIRED )
add_executable( gaussian gaussian )
target_link_libraries( gaussian ${OpenCV_LIBS} )
