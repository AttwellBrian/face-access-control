##Setup on Mac OS X
Setup the Android SDK with Eclipse as per Google's instructions. I don' think there should be any dependence on the Android NDK.

Import the application directory into an Eclipse workspace. There should be no additional setup.

##Current Status
Main Components
  1. One component is capable of finding faces on the screen in real time. It is a little slow, since I'm no longer using the C++ code.
  2. Another component recognizes faces from a training set. I've setup a very small training set (inside src). 

The two above components are not yet connected.

##Proposal

https://docs.google.com/document/d/1qCn1KuC45r-uz-HTVkwYc8mqkEIqjNAC2T6LP0xs_6E/pub

##Testing
Source code for a test project are contained inside the FacePreview_Test project.