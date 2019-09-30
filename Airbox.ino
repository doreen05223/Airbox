#include <SoftwareSerial.h> 
#include <SD.h>
#include <SPI.h>

// (新增)PMS5003T
static unsigned int pm_cf_10;           //定义全局变量
static unsigned int pm_cf_25;
static unsigned int pm_cf_100;
float Temperature;
float Humidity;

// Bluetooth(HC-06)
SoftwareSerial BT(8,9);   // 接收腳(RX), 傳送腳(TX)；接HC-06之TXD、RXD
bool startRecieve = false;
char val;
String recieveData = "";
 
// SD card
File myFile;
String buffer;

// CO2
SoftwareSerial K_30_Serial(2,3);  //Sets up a virtual serial port, using pin 12 for Rx and pin 13 for Tx
byte readCO2[] = {0xFE, 0X44, 0X00, 0X08, 0X02, 0X9F, 0X25};  //Command packet to read Co2 (see app note)
byte response[] = {0,0,0,0,0,0,0};  //create an array to store the response
// int valMultiplier = 1;  //multiplier for value. default is 1. set to 3 for K-30 3% and 10 for K-33 ICB

int x = 0;
int temp25 = 0;
long tempCO2 = 0;

String Time = "";// (新增) 時間
int hour;
int minute;
int sec;

void getG5(unsigned char ucData) // 获取G5(PMS5003T)的值
{
  static unsigned int ucRxBuffer[50];
  static unsigned int ucRxCnt = 0;
  ucRxBuffer[ucRxCnt++] = ucData;
  if (ucRxBuffer[0] != 0x42 && ucRxBuffer[1] != 0x4D)//数据头判断
  {
    ucRxCnt = 0;
    return;
  }
  if (ucRxCnt > 38)//数据位判断//G5S为32，G5ST为38
  {
    pm_cf_10=(int)ucRxBuffer[4] * 256 + (int)ucRxBuffer[5];      //大气环境下PM2.5浓度计算        
    pm_cf_25=(int)ucRxBuffer[6] * 256 + (int)ucRxBuffer[7];
    pm_cf_100=(int)ucRxBuffer[8] * 256 + (int)ucRxBuffer[9];
    Temperature = ((int)ucRxBuffer[24] * 256 +(int)ucRxBuffer[25])/10.000;//包含温度
    Humidity = ((int)ucRxBuffer[26] * 256 +(int)ucRxBuffer[27])/10.000;//包含湿度
       
    if (pm_cf_25 >  999)//如果PM2.5数值>=1000，返回重新计算
    {
      ucRxCnt = 0;
      return;
    }
    ucRxCnt = 0;
    return;
  }
}

int freeRam () // 測量記憶體
{
  extern int __heap_start, *__brkval; 
  int v; 
  return (int) &v - (__brkval == 0 ? (int) &__heap_start : (int) __brkval); 
}

void setup() {
  Serial.begin(9600);   
  
  BT.begin(9600); //HC-06 預設 baud

  Serial.print("Initializing SD card...");
  Serial.print("freeRam = ");Serial.println(freeRam()); // 測量記憶體
  pinMode(10, OUTPUT);   // pin10 is null for SD card
  if (!SD.begin(4)) {  //如果从CS口与SD卡通信失败，串口输出信息Card failed, or not present
    Serial.println("Card failed, or not present");
    return;
  }
  if(SD.exists("test.txt")){ // 初始化SD卡
    SD.remove("test.txt");
  }
  Serial.println("card initialized.");  //与SD卡通信成功，串口输出信息card initialized.

  // LED / 風扇
  pinMode(6, OUTPUT);
  pinMode(7, OUTPUT);
}

void loop() {
  BT.begin(9600);
  delay(2000);
  while(BT.available()) //如果有收到資料  
  {  
    startRecieve = true;  
    val=BT.read(); //每次接收一個字元  
    recieveData += val; //字元組成字串  
    delay(200);  //每次傳輸間隔，如果太短會造成資料遺失或亂碼  
  }

  if(startRecieve)  
  {  
    startRecieve = false;  
    Serial.println(recieveData); //呈現收到字串
    
    if(recieveData!="close" && recieveData!="now" && recieveData!=NULL){                    // recieveData 為開始的時間
      
      Time = recieveData; // (新增) 紀錄時間，下面計算
      hour = Time.substring(11, 13).toInt();
      minute = Time.substring(14, 16).toInt();
      sec = Time.substring(17).toInt();
    
      BT.write(byte('S'));BT.write(byte(' '));BT.write(byte(' '));
      myFile = SD.open("test.txt", FILE_WRITE);
      Serial.print("freeRam = ");Serial.println(freeRam()); // 測量記憶體
    }
    else if(recieveData=="close"){
      digitalWrite(6, LOW); // close LED / 風扇
      digitalWrite(7, LOW);
      myFile.close();
      myFile = SD.open("test.txt", FILE_READ);
      while (myFile.available()) {
        buffer = myFile.readStringUntil('\n');
        for(int i=0;i<buffer.length();i++){
          BT.write(byte(buffer[i])); //把每次收到的字元轉成byte封包傳至手機端
          Serial.print(buffer[i]);
        }
        Serial.println();
        delay(1000);
      }
      BT.write(byte('V'));
      BT.write(byte(' '));
      BT.write(byte(' '));
      myFile.close();

    }else if(recieveData=="now"){ // 新增 NOW 即時資料
      myFile.close();
      myFile = SD.open("test.txt", FILE_READ);
      while (myFile.available()) {
        buffer = myFile.readStringUntil('\n');
      }
      buffer = buffer.substring(9);

      BT.write(byte('N'));
      BT.write(byte(' '));

      for(int i=0;i<buffer.length();i++){
        BT.write(byte(buffer[i])); //把每次收到的字元轉成byte封包傳至手機端
        Serial.print(buffer[i]);
      }
      Serial.println();
      
      myFile.close();
      myFile = SD.open("test.txt", FILE_WRITE);
    }
    recieveData = "";
  }
  
  delay(3000);
  Serial.print("freeRam = ");Serial.println(freeRam()); // 測量記憶體

  if (myFile) {    
    // (新增) 時間
    Serial.print("Time = ");
    Serial.print(hour);Serial.print(":");Serial.print(minute);Serial.print(":");Serial.println(sec);
    /*if(hour < 10){
      myFile.print("0");
      myFile.print(hour);
    }else{
      myFile.print(hour);
    }
    myFile.print(":");
    if(minute < 10){
      myFile.print("0");
      myFile.print(minute);
    }else{
      myFile.print(minute);
    }
    myFile.print(":");
    if(sec < 10){
      myFile.print("0");
      myFile.print(sec);
    }else{
      myFile.print(sec);
    }
    myFile.print(" ");*/
    
    Serial.println("Reading data...");
    Serial.print("freeRam = ");Serial.println(freeRam()); // 測量記憶體
    // (新增)PMS5003T
    while (Serial.available())
    {
      getG5(Serial.read());
    }
    
    Serial.print("   PM_CF1.0:");Serial.print(pm_cf_10);Serial.println(" ug/m3");//硬件串口输出数据
    Serial.print("   PM_CF2.5:");Serial.print(pm_cf_25);Serial.println(" ug/m3");
    Serial.print("   PM_CF10 :");Serial.print(pm_cf_100);Serial.println(" ug/m3");
    Serial.print("   TS:");Serial.print(Temperature+1,1);Serial.println(" C");  //包含温度
    Serial.print("   HS:");Serial.print(Humidity+5,1);Serial.println(" %RH");   //包含湿度 
      
    // write into SD
    myFile.print(pm_cf_10);
    myFile.print(" ");
    myFile.print(pm_cf_25);
    myFile.print(" ");
    myFile.print(pm_cf_100);
    myFile.print(" ");
    /*myFile.print(Temperature+1);
    myFile.print(" ");
    myFile.print(Humidity+5);
    myFile.print(" ");*/
    
    // CO2
    K_30_Serial.begin(9600);    //Opens the virtual serial port with a baud of 9600
    sendRequest(readCO2);
    int valCO2 = getValue(response);
    Serial.print("CO2 ppm = ");
    Serial.println(valCO2);
    K_30_Serial.end();
    myFile.print(valCO2);
    myFile.print(" ");
    // myFile.println();

    // average
    if(x < 11)
    {
      x++;
      temp25 += pm_cf_25;
      tempCO2 += valCO2;
      myFile.print("0");
      myFile.print(" ");
      myFile.print("0");
      myFile.println();
      // (新增) 時間計算
      if(sec < 55){
        sec += 5;
      }else{
        sec = sec + 5 - 60;
        if(minute < 59){
          minute += 1;
        }else if(minute == 59){
          minute = 0;
          if(hour < 23){
            hour += 1;
          }else if(hour == 23){
            hour = 0;
          }
        }
      }
    }else if(x == 11){
      temp25 += pm_cf_25;
      tempCO2 += valCO2;
      temp25 /= 12;
      tempCO2 /= 12;
      myFile.print(temp25);
      myFile.print(" ");
      myFile.print(tempCO2);
      myFile.println();

      // (新增)turn on the LED / 風扇
      if(temp25 < 24 && tempCO2 < 1000){
        digitalWrite(6, LOW);
        digitalWrite(7, LOW);
      }else{
        digitalWrite(6, HIGH);
        digitalWrite(7, HIGH);
      }
      // 初始化
      x = 0;
      temp25 = 0;
      tempCO2 = 0;
      // (新增) 時間計算
      if(sec < 53){
        sec += 7;
      }else{
        sec = sec + 7 - 60;
        if(minute < 59){
          minute += 1;
        }else if(minute == 59){
          minute = 0;
          if(hour < 23){
            hour += 1;
          }else if(hour == 23){
            hour = 0;
          }
        }
      }
    }
  }
  else {
    Serial.println("error opening test.txt");
  }
}

/*************************************for CO2***********************************************/
void sendRequest(byte packet[])
{
  while(!K_30_Serial.available())  //keep sending request until we start to get a response
  {
    K_30_Serial.write(readCO2,7);
    delay(50);
  }
  
  int timeout=0;  //set a timeoute counter
  while(K_30_Serial.available() < 7 ) //Wait to get a 7 byte response
  {
    timeout++;  
    if(timeout > 10)    //if it takes too long there was probably an error
      {
        while(K_30_Serial.available())  //flush whatever we have
          K_30_Serial.read();
          
          break;                        //exit and try again
      }
      delay(50);
  }
  
  for (int i=0; i < 7; i++)
  {
    response[i] = K_30_Serial.read();
  }  
}

int getValue(byte packet[])
{
    int high = packet[3];                        //high byte for value is 4th byte in packet in the packet
    int low = packet[4];                         //low byte for value is 5th byte in the packet
    
    int val = high*256 + low;                //Combine high byte and low byte with this formula to get value
    return val;
    // return val* valMultiplier;
}
/**********************************************************************************************/
