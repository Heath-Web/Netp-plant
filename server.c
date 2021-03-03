#include <stdio.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <unistd.h>
#include <errno.h>
#include <string.h>
#include <stdlib.h>

#define SERV_PORT 8000
#define MAXLINE 1024 

int main()
{
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
