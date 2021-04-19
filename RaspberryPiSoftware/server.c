#include <stdio.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <unistd.h>
#include <errno.h>
#include <string.h>
#include <stdlib.h>
#include <wiringPi.h>
#include <sys/time.h>
#include <wiringPiI2C.h>
#include "my_dht11_2.c"
#include "module.h"
#define SERV_PORT 8000
#define MAXLINE 1024 

// state some functions
char*  generate_response(char recv_buf[], char data[]);
int startServer();

int main(){
	/*
	 * "main" function
	 *  recieve request from server and send response back
	 */
	//start server
	int sock_fd = startServer();
	
	//initialize some global variables 	
	char data[5] = ""; // used to acquire humidity and temperarure
	int recv_length,  send_length; // length of receive buffer and send buffer
	char send_buf[MAXLINE];  //send buffer
	char recv_buf[MAXLINE]; // receive buffer 
	struct sockaddr_in addr_client; // client address
	int len = sizeof(addr_client); // length if client address

	//Receiving requests from client through a endless loop
	while(1) 
	{	
		//wait to receive any requests from client
		printf("server wait...\n");
		// receive data
		strcpy(recv_buf,"");
		recv_length = recvfrom(sock_fd, recv_buf, MAXLINE, 0, (struct sockaddr *)&addr_client, (socklen_t *)&len);
		if(recv_length < 0)
    	{
      		perror("recvfrom error:");
      		exit(1);
    	}
    	recv_buf[recv_length] = '\0';
    	printf("server receive %d bytes: %s\n", recv_length, recv_buf);
		
		// generate response (json format)
		strcpy(send_buf,"");
		strcpy(send_buf,generate_response(recv_buf,data));
		//send data to client
    	send_length = sendto(sock_fd, send_buf, sizeof(send_buf), 0, (struct sockaddr *)&addr_client, len);
    	if(send_length < 0)
    	{
      		perror("sendto fialed:");
      		exit(1);
    	}
    	printf("server send %d bytes: %s\n", send_length, send_buf);
  	}
	// close socket
	close(sock_fd);
	return 0;
}

int startServer(){
	/*
	 * This function check the connection of Raspberry Pi first
	 * and  setup a udp server.
	 * retrn a file descriptor of the socket
	 */
	 
	// Check the connection of Raspberry Pi
	if (wiringPiSetup()<0){
		perror("raspi start failure...");
		exit(1); 
	}
	
	//create a UDP server socket
	int sock_fd = socket(AF_INET, SOCK_DGRAM, 0);
	if(sock_fd < 0) // if wrong
	{
		perror("socket creation failed ");
		exit(1);
	}
		
	//server address
	struct sockaddr_in addr_serv;
	memset(&addr_serv, 0, sizeof(struct sockaddr_in));
	//Filling server infromation (port)
	addr_serv.sin_family = AF_INET;
	addr_serv.sin_port = htons(SERV_PORT);
	addr_serv.sin_addr.s_addr = htonl(INADDR_ANY);
	
	//bind the socket with server address
	if(bind(sock_fd, (struct sockaddr *)&addr_serv, sizeof(addr_serv)) < 0)
	{
		perror("bind failed:");
		exit(1);
	}
	
	// return socket file descriptor
	return sock_fd;
} 

char* generate_response(char recv_buf[], char data[]){
	/*
	 * This function used to generate respones according to the command that the
	 * server recieved. two inputs, recv_buf[] is the receive buffer and data[] 
	 * is a global variable used to acquire humidity and temperarure.
	 * This function will return a string char *send_buf, the send buffer.
	 */
	// initialize send buffer
	static char send_buf[1024];
	int i=0;
	// Judge the command
	if (strcmp(recv_buf ,"get enviroment\0")==1){
		// "get enviroment" command 
		printf("#command: get enviroment \n");
		
		//initialize variables
		char humidity_str[] = "0"; // humidity string format
		char temperature_str[] = "0"; // temperature string format
		char moisture_str[] = "0"; // moisture string format
		char light_str[] = "0"; // light String format
		int moisture=0; // moisture 
		int light = 0; // light
		int succeess = 1;  // identifier (flag) used to judge succeed or not 1:succeed 0:failed 2:Can not recognize Sensor 
		
		//get humidity adnd temperature
		int code = read_dht11_data(data);	
		// get moisture
		ad(0x00);
		moisture = (255-ad(0x00))*100/255; //range 0-1
		//get light intensity
		ad(0x02);
		light = (255-ad(0x02))*100/255;
		
		// check wthether success and change flag
		if (moisture==100 || light==100 || code == 4){
			printf("Moisture or light Sensor doesn't online'!\n");
			succeess = 2;
		}else if(code == 0 || code == 2 || code == 3){
			succeess = 0;
		}else {
			succeess = 0;
		}
		 
		switch(succeess){
			case 1:
				printf("get enviroment data succeed\n");
				printf("humidity:%d\n",data[0]);
				printf("temperature:%d\n",data[2]);
				printf("moisture:0.%d\n",moisture);
				printf("light:0.%d\n",light);

				//Converts integers to string
				sprintf(humidity_str,"%d",data[0]); 
				sprintf(temperature_str,"%d",data[2]); 
				//sprintf(humidity_str,"%d",10); 
				//sprintf(temperature_str,"%d",20); 
				sprintf(moisture_str,"%d",moisture);
				sprintf(light_str,"%d",light);
				
				//response (josn format) status code=1
				strcpy(send_buf,"{\'status\':1,\'humidity\':");
				strcat(send_buf,humidity_str);
				strcat(send_buf,",\'temperature\':");
				strcat(send_buf,temperature_str);
				strcat(send_buf,",\'moisture\':");
				strcat(send_buf,moisture_str); 
				strcat(send_buf,",\'light\':");
				strcat(send_buf,light_str);
				strcat(send_buf,"};");
				//strcpy(send_buf,"{\'status\':1,\'humidity\':12.0,\'temperature\':13.0,\'moisture\':1.0,\'light\':45}");
				break;
			
			case 0:
				// something wrong when getting humidity and temperature
				printf("get enviroment data failed\n");
				// send buffer status code = -1
				strcpy(send_buf,"{\'status\':-1};");
				break;
				
			case 2:
				// Seneor connection error
				printf("Seneor connection error\n");
				// send buffer status code = 2
				strcpy(send_buf,"{\'status\':2};");		
		}
	}else if (strcmp(recv_buf ,"open pump")==0){
		//open pump command
		printf("#command: open pump \n");
		//open pump
		relay(i);
		//response status code=1
		strcpy(send_buf,"{\'status\':1};");
	}else if (strcmp(recv_buf ,"close pump")==0){
		//close pump command
		printf("#command: close pump \n");
		//close pump
		relay(~i);
		//response status code=1
		strcpy(send_buf,"{\'status\':1};");
	}else {
		printf("#command does not exist: %s \n", recv_buf);
		strcpy(send_buf,"{\'status\':-1};");
	}
	// return send buffer
	return send_buf;
}  
