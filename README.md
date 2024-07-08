Plugin app for Tasker and MacroDroid to provide Humans Detection

Licensed under GPL v3

V 1.0.2 - added file picker an 

Features:
* Provides a tasker action capable to open an image and return a score 0-100 in term of how likely the image contains a person
* simple home screen to test it against local images
* can parse file names in the form of content://media/external/images/something or in form file:///sdcard/somewhere/file.jpg
* uses OpenCV so can parse PNG and JPG

Limitations:
* uses old APIs so no Play store version
* sometime later I will provide pre-build APK for ease of use
* OpenCV integration is working but probably not optimal
* permission and battery management is still rudimentary

Ideas for future improvements:
* Integrate cloud-based AWS Rekognition service
* Integrate local GoogleML-based image recognition 

IMPORTANT Caveats:
* will use more battery than you want, until I understand why, the default plugin mechanism is not working as expected (problems with Foreground service)
* SECURITY: it's not yet using any secret but just in case change the value off SharedPreferencesHelper.PASSWORD: it's used to encrypt any local settings (currently unused) and it's ad additional form of security in case your phone gets compromised

HOW-TO use it:
* install the APK (you can download it from the GitHub releases) 
* start it: so that it's registered and available to Tasker/Macrodroid
* within Macrodroid/Tasker
    * go to the task you want to use
    * add action > external app > OpenCV4Tasker > Human Recognition
    * you get a window where to enter the name of the image, usually you'll want to use a variable instead of an hard-coded value (e.g. %my_image )
    * then you get a window where you say where to save the result, usually another variable (e.g. detection_score)
    * then do whatever you want with the information :-)

HOW-TO test it:
* start the app
* grant the permission it requires
* use the file picker to choose an image
* press the "test recognition" button
* see what's the score

E.g. my use case is simple: I want to reduce to almost zero the false positive alarms of some security cameras 
* listen for alerts from (cheap?) security cameras
* download the alert image locally
* pass the image to this plugin
* if the detection_score>=50 then start the siren/lights
* otherwise just ignore the false positive