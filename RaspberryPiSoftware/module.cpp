#include "module.h"
#include <time.h>
#include <iostream>
#include <iomanip>
#include <unistd.h>
#include <wiringPi.h>
#include <sys/time.h>
#include <wiringPiI2C.h>
#include <string.h>
using namespace std;
#define ad_address 0x48
#define relay_gpio 0
#define DAT 2


//Write
void write(int data){
	
	int i = 0;
	int j = 0x80;
	for(i=0;i<8;i++){
		
		if(data&j){
			digitalWrite(8,HIGH);
			j=j>>1;
			usleep(5);
		}else{
			digitalWrite(8,LOW);
			j=j>>1;
			usleep(5);
		}

		digitalWrite(9,HIGH);
		usleep(5);
		digitalWrite(9,LOW);
	}
	
	digitalWrite(8,HIGH);
}

//Acknowledge
int ack(void){
	
	int time=0;
	pinMode(8,INPUT);
	pullUpDnControl(8,PUD_UP);
	while(digitalRead(8) == 1){
		time++;
		if (time == 3000){
			return 0;
		}
	};
	digitalWrite(9,HIGH);
	usleep(5);
	digitalWrite(9,LOW);
	pinMode(8,OUTPUT);
	digitalWrite(8,HIGH);
	usleep(5);
	return 1;
}

//No acknowledge
void noack(void){
	
	pinMode(8,OUTPUT);
	digitalWrite(8,HIGH);
	digitalWrite(9,HIGH);
	usleep(5);
	digitalWrite(9,LOW);
	usleep(5);
	
}

//Read Byte
int read(void){
	
	int i;
	int x = 0x00;
	pinMode(8,INPUT);
	pullUpDnControl(8,PUD_UP);
	digitalWrite(9,LOW);

	for(i=0;i<8;i++){
		
		digitalWrite(9,HIGH);
		usleep(5);
		x = x<<1;
		x = x|digitalRead(8);
		digitalWrite(9,LOW);
		usleep(5);

	}
	return x;

}

//AD change
int ad(int add){	
	pinMode(8,OUTPUT);
	pinMode(9,OUTPUT);
	int fd = wiringPiI2CSetup(ad_address);
	digitalWrite(9,HIGH);
	usleep(5);
	digitalWrite(8,HIGH);
	usleep(5);
	
	//start
	digitalWrite(8,LOW);
	usleep(5);
	digitalWrite(9,LOW);

	//write		
	write(0x90);
	if (ack()==0){
		return 0;
	}
	write(add);
	if (ack()==0){
		return 0;
	}

	//stop I2C
	digitalWrite(8,LOW);
	usleep(5);
	digitalWrite(9,HIGH);
	usleep(5);
	digitalWrite(8,HIGH);
	usleep(10);
	
	//read
	//send address
	digitalWrite(8,HIGH);
	digitalWrite(9,HIGH);
	usleep(5);
	digitalWrite(8,LOW);
	usleep(5);
	digitalWrite(9,LOW);
	write(0x91);
	usleep(5);
	if (ack()==0){
		return 0;
	}
	usleep(10);
	//reading	
	int data = read(); 
	noack();

	//Stop I2C
	pinMode(8,OUTPUT);
	digitalWrite(8,LOW);
	usleep(5);
	digitalWrite(9,HIGH);
	usleep(5);
	digitalWrite(8,HIGH);
	usleep(5);
	return data;
}

//Open bump 5 sec
void relay(int i){

	int opt;
	if (i==0){
		 opt=0;
		}else{
		opt=1;
		}
	pinMode(relay_gpio, OUTPUT);
	digitalWrite(relay_gpio, opt);

}

