# Net+ Plant

description here one sentence

## Team Member

[*Ze Wang*](https://github.com/Heath-Web)         2550208W

[*Jiahui Ren*](https://github.com/paymorepatience)       2550208W

[*Feng Ni*](https://github.com/FENGN-AII)           2550208W

## Overview

#### System Flow chart

<img src="https://github.com/paymorepatience/Netp-plant/blob/main/Media/Images/SystemFlowChart.png">

#### File Structure

The repository consists of 3 folders:

- Android GUI 

  Contain the Source code of the Android application.

- APK

  Contain an Android application package of the *Net+ Plant* APP.

- RaspberryPiSoftware

  Involve all programs on Raspberry Pi.

  - my_dht11_2.c  

    Temperature and humidity module. Used to read temperature and air humidity.

  - module.c  and module.h

    Soil moisture detection, light intensity detection and water pump module. Used to read Soil moisture and light intensity and control water pump. 

  - server.c  

    UDP server. Used to receive commands from Mobile APP and make response.

  

## Raspberry Pi

In order to be able to run the server on Raspberry Pi:

1. Download Server folder to Respberry Pi
2. `cd  xxx`
3. 

**final:** 



## Android GUI

In order to download, install and use Graphical User Interface *Net+ Plant* APP :

1. Download `app-release.apk`  in APK folder to your Android mobile phone
2. Install *Net+ Plant*
3. Input IP address of Raspberry Pi. Make sure the address is right. ( If IP is wrong, the app will get stuck in waiting response form a non-exist server. )
4. Refresh the environment data
5. Open/Close water Pump 

Final

