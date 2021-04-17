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

char*  generate_response(char recv_buf[], char data[]);

int main()
{
	char data[5] = "";
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
	//Filling server infromation
	addr_serv.sin_family = AF_INET;
	addr_serv.sin_port = htons(SERV_PORT);
	addr_serv.sin_addr.s_addr = htonl(INADDR_ANY);
	
	//bind the socket with server address
	if(bind(sock_fd, (struct sockaddr *)&addr_serv, sizeof(addr_serv)) < 0)
	{
		perror("bind failed:");
		exit(1);
	}
	
	int recv_length,  send_length;
	char send_buf[] = "i am server!";
	char recv_buf[MAXLINE];
	struct sockaddr_in addr_client; // client address
	int len = sizeof(addr_client);

	while(1)
	{
		//confirm to recieve request
		int confirm = 0;
		printf("1.confirtm  2.quit :");
		scanf("%d", &confirm);
		if (confirm == 2){
			break;
		}
		
		//wait to recieve any request from client
		printf("server wait:\n");
		recv_length = recvfrom(sock_fd, recv_buf, MAXLINE, 0, (struct sockaddr *)&addr_client, (socklen_t *)&len);
		if(recv_length < 0)
    	{
      		perror("recvfrom error:");
      		exit(1);
    	}
    	recv_buf[recv_length] = '\0';
    	printf("server receive %d bytes: %s\n", recv_length, recv_buf);

		char send_buf[1024];
		strcpy(send_buf,generate_response(recv_buf,data));
		printf("send buffer: %s\n", send_buf);
		
		//send data to client
    	send_length = sendto(sock_fd, send_buf, sizeof(send_buf), 0, (struct sockaddr *)&addr_client, len);
    	if(send_length < 0)
    	{
      		perror("sendto fialed:");
      		exit(1);
    	}
    	printf("server send %d bytes: %s\n", send_length, send_buf);
  	}

	close(sock_fd);
	return 0;
}

char* generate_response(char recv_buf[], char data[]){
	static char send_buf[1024];
	if (strcmp(recv_buf ,"get enviroment")){
		//tem and hum
		read_dht11_data(data);
		int code = read_dht11_data(data)
		printf("%d", code);
		switch(code){
			case 1:
				printf("succeed!!!!!!!!!!!!!!!!!!!!!!!!");
				break;
			default:
				printf("default\n");
				break;		
		}
		//moisture light
		int moisture = ad(0x00);
		printf("\nmoisture:%d",moisture);
		
		int light = ad(0x01);
		printf("\nlight:%d\n",light);

		
		//response
		strcpy(send_buf,"{\'status\':1,\'humidity\':12.0,\'temperature\':13.0,\'moisture\':1.0,\'light\':45}");
		printf("#generate_response get enviroment");
	}else if (strcmp(recv_buf ,"open pump")){
		//open pump
		relay();
		//response
		strcpy(send_buf,"{\'status\':1,\'humidity\':12.0,\'temperature\':13.0,\'moisture\':1.0,\'light\':45}");
		printf("#generate_response open pump");
	}else if (strcmp(recv_buf ,"close pump")){
		//response
		strcpy(send_buf,"{\'status\':1,\'humidity\':12.0,\'temperature\':13.0,\'moisture\':1.0,\'light\':45}");
		printf("#generate_response close pump");
	}else {
		strcpy(send_buf,"{\'status\':1,\'humidity\':12.0,\'temperature\':13.0,\'moisture\':1.0,\'light\':45}");
		printf("#generate_response else");
	} 
	return send_buf;
}  
