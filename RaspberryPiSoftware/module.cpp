#include "module.h"
#include <time.h>
#include <iostream>
#include <iomanip>
#include <unistd.h>
#include <wiringPi.h>
#include <sys/time.h>
#include <wiringPiI2C.h>
using namespace std;
#define ad_address 0x48
#define relay_gpio 0
#define DAT 2


void write(int data){
	
	int i = 0;
	int j = 0x80;
	for(i=0;i<8;i++){
		
		digitalWrite(9,LOW);
		usleep(5);
		if(data&j){

			digitalWrite(8,HIGH);
			j=j>>1;
	
		
		}else{
		
			digitalWrite(8,LOW);

			j=j>>1;
		
			}

		digitalWrite(9,HIGH);
		usleep(5);
	}

	digitalWrite(9,LOW);
	usleep(5);
	digitalWrite(8,HIGH);
	pinMode(8,INPUT);
	pullUpDnControl(8,PUD_UP);
	usleep(5);
}

void ack(void){
	
	while(digitalRead(8) == 1);
	digitalWrite(9,HIGH);
	usleep(5);
	digitalWrite(9,LOW);
	while(digitalRead(8) == 0);
	usleep(5);
	
}

void noack(void){
	
	pinMode(8,OUTPUT);
	digitalWrite(8,HIGH);
	digitalWrite(9,HIGH);
	usleep(5);
	digitalWrite(9,LOW);
	usleep(5);
	
}

int read(void){
	
	int i;
	int x = 0x00;
	pinMode(8,INPUT);
	pullUpDnControl(8,PUD_UP);
	digitalWrite(9,LOW);
	for(i=0;i<7;i++){
		
		digitalWrite(9,HIGH);
		usleep(5);
		x = x<<1;
		x = x|digitalRead(8);
		digitalWrite(9,LOW);
		usleep(5);

	}
	return x;

}

int ad(int add){	
	pinMode(8,OUTPUT);
	pinMode(9,OUTPUT);
	int fd = wiringPiI2CSetup(ad_address);
	digitalWrite(9,HIGH);
	usleep(1);
	digitalWrite(8,HIGH);
	usleep(2);
	
	//start
	digitalWrite(8,LOW);
	usleep(1);

	//wrute
	write(0x90);
	ack();
	pinMode(8,OUTPUT);
	write(add);
	ack();
	pinMode(8,OUTPUT);

	//stop
	digitalWrite(8,LOW);
	usleep(5);
	digitalWrite(9,HIGH);
	usleep(5);
	digitalWrite(8,HIGH);

	//¶Á
	//ÊäËÍµØÖ·
	digitalWrite(8,HIGH);
	digitalWrite(9,HIGH);
	usleep(5);
	digitalWrite(8,LOW);
	usleep(5);
	write(0x91);
	usleep(5);
	ack();
	pinMode(8,OUTPUT);
	
	//read
	int data = read(); 
	noack();
	pinMode(8,OUTPUT);
	digitalWrite(8,LOW);
	usleep(5);
	digitalWrite(9,HIGH);
	usleep(5);
	digitalWrite(8,HIGH);
	usleep(5);
	return data;
}


void relay(void){

	pinMode(relay_gpio, OUTPUT);
	digitalWrite(relay_gpio, LOW);
	sleep(5);
	digitalWrite(relay_gpio, HIGH);
}

