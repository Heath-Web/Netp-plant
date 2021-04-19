#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <time.h>
#include <wiringPi.h>
#include <string.h>
#define DAT    3    //wiringPi mode
void dht11_init(void)
{
/*
 * this function is used to set raspi gpio output
 */
   pinMode(DAT,OUTPUT);//set the DATApin as output
   digitalWrite(DAT,1);//continue put highlevel
   usleep(10000);
}
void dht11_start()
{
/*
 *this function is used to sent start signal
 */
   digitalWrite(DAT,0);//putdown MCU signal make dht11 detect start signal
   delayMicroseconds(19000);//make the lowlevel signal longer than 18ms
   digitalWrite(DAT,1);//pullup MCU signal as highlevel 20-40us to read dht11 respond signal 
   delayMicroseconds(30);//delay 30us waiting for dht11 respond signal
   pinMode(DAT,INPUT);//waiting for dht11 respond and set DATApin as input
}

int read_bit(){
/*
 *this function is used to read dht11 0/1 digital signal bit which depends on length of time of highlevel.  
 */
   time_t t1,t2;
   while(digitalRead(DAT)==0);//waiting for lowlevel finish
   delayMicroseconds(40);//goto 70um perido,check it is highlevel or not
   if(digitalRead(DAT)==1)//read DAT state as 1 so return bit as 1
   {
       t1 = time(NULL);//mark time as t1 and t2 to figure out overtime problem
       while(digitalRead(DAT)==1){//when output is highlevel,continue mark t2 until lowlevel is end
            t2 = time(NULL);
            if(t2-t1)
            {
               printf("overtime error....\n");
               return 0xcc;//return overtime error when the time of dht11 signal continue output highlevel as 1s
            }
       }
       return 1;//read data as 1
   }else{
       return 0;//read data as 0
   }
}

int read_byte()
{
/*
 *this function is used to read 8bit of 5 byte,which are high humidity, low humidity ,hightemp,lowtemp and parity bit
 */
     char data = 0,retval,i;
     for(i=0;i<8;i++)//read the 8bit binary number
     {
        data = data << 1;//data move left
        retval = read_bit();//call function to read digital signal bits
        if(retval==0xcc)  return 0xcc;//when signal is continue highlevel,return overtime error
        data |= retval;
     }
     return data;//return the 40 bits data from dht11
}
int wait_res()
{
/*
 * this function is used to waiting for response from dht11 
 */
   delayMicroseconds(40);//go to period of dht11 80us lowlevel response signal )
   return digitalRead(DAT);//check DATpin state is 0 or 1
}
int check_data(char data[],char len)
{
/*
 *this function is used to calculate parity bit
 */
     int i,sum=0;
     for(i=0;i<4;i++)//i is 4 bytes of humility and tempuerature
     {
        sum += data[i];//make summary of first four byte data
     }
     if(sum==data[4]){
        return 1;
     }else{
        //printf("parity error....\n");
        return 0;
     }
}
int read_dht11_data(char *data)
{
/*
 *this function is used to read dht11 data 
 */
      int i=0;   
      //1:dht11 init
      dht11_init();
      //2:send strat signal
      dht11_start();
      //3:waiting for respond
      if(wait_res()==0)//0=respond 1=no respond 
      {
         //4:waiting for dht11  respond end
         while(wait_res()==0);//waiting for lowlevel end
         while(wait_res()==1);//waiting for highllevel end
         //5:start reading data
         for(i=0;i<5;i++)
         {
   	      data[i] = read_byte();
              if(data[i]==0xcc)
              {
                  return 0;//overtime error
              };
         }
         while(digitalRead(DAT)==0);//when dht signal state at lowlevel,it is strat to send data
         pinMode(DAT,OUTPUT);//set DATpin as output
         digitalWrite(DAT,1);
         printf("temperature:%d,humidity:%d\n",data[2],data[0]);
         //6:check parity have error or not
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

/*
int main()
{
   char data[5]="";
   if(wiringPiSetup()<0)//set raspi encoding mode as wiringPi
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
                //puts("parity error....");
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
}
*/













