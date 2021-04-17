#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <time.h>
#include <wiringPi.h>
#include <string.h>
#define DAT 2    //wiringPi mode
void dht11_init(void)
{
   pinMode(DAT,OUTPUT);
   digitalWrite(DAT,1);//continue put highlevel
   usleep(10000);
}
void dht11_start()
{
   digitalWrite(DAT,0);//putdown signal
   delayMicroseconds(19000);//>18ms
   digitalWrite(DAT,1);//
   delayMicroseconds(30);//30us
   pinMode(DAT,INPUT);//waiting for dht11 respond
}
int read_bit(){
   time_t t1,t2;
   while(digitalRead(DAT)==0);//waiting for synchronization
   delayMicroseconds(40);//goto 70um perido,check highlevel
   if(digitalRead(DAT)==1)
   {
       t1 = time(NULL);
       while(digitalRead(DAT)==1){//waiting for data1 highlevel end
            t2 = time(NULL);//if time is not over 1s, so two times are equal
            if(t2-t1)
            {
               printf("overtime error....\n");
               return 0xcc;//overtime
            }
       }
       return 1;//read data 1
   }else{
       return 0;//read data 0
   }
}
int read_byte()
{
     char data = 0,retval,i;
     for(i=0;i<8;i++)
     {
        data = data << 1;
        retval = read_bit();
        if(retval==0xcc)  return 0xcc;
        data |= retval;
     }
     return data;
}
int wait_res()
{
   delayMicroseconds(40);//go to period of dht11 respond(80us)
   return digitalRead(DAT);//check DATpin state  
}
int check_data(char data[],char len){
     int i,sum=0;
     for(i=0;i<4;i++)
     {
        sum += data[i];
     }
     if(sum==data[4]){
        return 1;
     }else{
        //printf("sumdata error....\n");
        return 0;
     }
}
int read_dht11_data(char *data)
{
      int i=0;   
      //1:dht11 init
      dht11_init();
      //2:send strat signal
      dht11_start();
      //3:waiting for respond
      if(wait_res()==0)//0=respond 1=no respond 
      {
         //4:等待响应结束
         while(wait_res()==0);//waiting for lowlevel end
         while(wait_res()==1);//waiting for highllevel end
         //5:start readingdata
         for(i=0;i<5;i++)
         {
   	      data[i] = read_byte();
              if(data[i]==0xcc)
              {
                  return 0;//overtime error
              };
         }
         while(digitalRead(DAT)==0);
         pinMode(DAT,OUTPUT);
         digitalWrite(DAT,1);
         printf("temperature:%d,humidity:%d\n",data[2],data[0]);
         //6:chieck
         if(check_data(data,5)==1){
            //printf("temperature:%d,humidity:%d\n",data[2],data[0]);
            return 1;//right data
         }else{
            return 2;//sumdata error
         }
      }else{
           return 3;//no respond
      }
}
/*int main()
{
   char data[5]="";
   if(wiringPiSetup()<0)
   {
     perror("raspi start failure...");
     exit(1);//exit program
   }
   while(1)
   {
        switch(read_dht11_data(data))
        {
           case 0:
                //puts("overtime...");
                continue;
           case 1://正确
                printf("temperature:%d,humidity:%d\n",data[2],data[0]);
                break;
           case 2://校验和错误
                //puts("sumdata error....");
                continue;
           case 3:
                //puts("dht11 no respond...");
                continue;
           default:
                puts("other error....");
                break;
        }
        sleep(2);
   }
   return 0;
}*/
















