# Minor-Project: Autonomous Smart Waste Segregation

***This repository contains the code for the Minor-Project for Autonomous Smart Waste Segregation using Deep Learning.***

# Requirements

**1.** Android Studio

**2.** Arduino IDE

**3.** ESP8266 Board

**4.** Python 3

**5.** Tensorflow

# Usage

**1.** Install the Android application from [here](https://github.com/AashiDutt/SmartWasteSegregation/tree/master/SmartWasteSegregation) using Android Studio.

**2.** Upload the Arduino Server code from [here](https://github.com/AashiDutt/SmartWasteSegregation/tree/master/SmartWasteSegregator-Arduino-Code/arduino_server) to your arduino board. Make sure to change your WiFi SSID and Password [here](https://github.com/AashiDutt/SmartWasteSegregation/blob/fb4b08ac0044174a42bae6143078471ba0b9ac2f/SmartWasteSegregator-Arduino-Code/arduino_server/arduino_server.ino#L5)

**3.** Start the Android app and enter the URL as shown in the Arduino IDE Serial Terminal.

**4.** Click on Submit button and you will see the main screen.

**5.** Click on "Capture Image" button and take a picture of the waste you want to classify.

**6.** The ML model performs on-device image classification for the waste type and sends a command to the Arduino board over WiFi to control the servo motor controlling the lid for the appropriate bin.

# Project Report
Detailed Project Report [here](https://github.com/AashiDutt/SmartWasteSegregation/blob/master/Detailed_Report.pdf)

# Project Demo

You can check out this project in action in the demo video [here](https://www.youtube.com/watch?v=7i3ZSEmDEm4)

```
https://www.youtube.com/watch?v=7i3ZSEmDEm4
```
```
https://www.youtube.com/watch?v=RvYvGm7pPzU
```
