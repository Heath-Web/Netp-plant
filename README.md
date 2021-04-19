# Net+ Plant

Auto irrigation through Raspberry Pi and monitor environment data of home plants .

## Team Member

[*Ze Wang*](https://github.com/Heath-Web)               2550208W

[*Jiahui Ren*](https://github.com/paymorepatience)             2438565R

[*Feng Ni*](https://github.com/FENGN-AII)                 2511633N

## Overview

The aim of this project is to design and fabricate a prototype auto irrigation system for home plants. Users can monitor the environment data like air humidity and temperature soil moisture and light intensity.  It also have a water pump button to start or stop the water flow. in addition, the irrigation time and the yield of water is available to show in the interface.  

#### System Flow chart

<img src="https://github.com/paymorepatience/Netp-plant/blob/main/Media/Images/SystemFlowChart.png">

#### Final Product

the demonstration video is here [Demonstration Vidio](https://youtu.be/O6kg-7maIwA).

<img src="https://github.com/paymorepatience/Netp-plant/blob/main/Media/Images/Net%2B%20Plant.jpg">

<img src="https://github.com/paymorepatience/Netp-plant/blob/main/Media/Images/background.jpg">

#### File Structure

The repository consists of 3 folders:

- Android GUI 

  Contain the Source code of the Android application.

- APK

  Contain an Android application package of the *Net+ Plant* APP.

- Media

  Involves all images in this project and our demonstration video.

- RaspberryPiSoftware

  Involve all programs on Raspberry Pi.

  - my_dht11_2.c  

    Temperature and humidity module. Used to read temperature and air humidity.

  - module.c  and module.h

    Soil moisture detection, light intensity detection and water pump module. Used to read Soil moisture and light intensity and control water pump. 

  - server.c  

    UDP server. Used to receive commands from Mobile APP and make response.

  

## Direction

#### Raspberry Pi server

In order to be able to run the server on Raspberry Pi:

1. Download RaspberryPiSoftware folder to Respberry Pi

2. `cd  RaspberryPiSoftware`

3. Compile `g++ -c server.c`

4. `g++ -c module.cpp`

5. `g++ module.o server.o -o server -lwiringPi`

6. Run  `./server`

   if you see `server wait....` on the terminal , it means server started successfully.



#### Android GUI

In order to download, install and use Graphical User Interface *Net+ Plant* APP :

1. Download `app-release.apk`  in *APK/release* folder to your Android mobile phone

2. Install *Net+ Plant* APP.

3. Input IP address of Raspberry Pi. Make sure the address is right. ( If IP is wrong, the app will get stuck in waiting response form a non-exist server. )

   <img src="https://github.com/paymorepatience/Netp-plant/blob/main/Media/Images/Android_interface1.jpg">

4. Refresh the environment data

5. Open/Close water Pump 

   <img src="https://github.com/paymorepatience/Netp-plant/blob/main/Media/Images/Android_interface2.png">

