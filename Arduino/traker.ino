#include <SoftwareSerial.h>
SoftwareSerial GSMport(3, 2); // RX, TX

String user = ""; //Write here your user name
String pass = ""; //Write here your password

typedef struct {
    String latitude;
    String longitude;
} MyDataGPS;

MyDataGPS dataGPS;
void gprs_init(void);
MyDataGPS getGPSLocation(void);
void gprs_send(String dataa, String datab, int dataA0, int dataA1);
String ReadGSM();

void setup() {
  Serial.begin(9600);
  GSMport.begin(9600);
  delay(300);
  gprs_init();
}

void loop() {
  char c;
  String str;

  dataGPS = getGPSLocation();
  gprs_send(dataGPS.latitude, dataGPS.longitude, 0, 0);

  while (GSMport.available()) {
    c = GSMport.read();
    Serial.write(c);
    delay(10);
  }

  while (Serial.available()) {
    c = Serial.read();
    GSMport.write(c);
    delay(10);
  }
}


MyDataGPS getGPSLocation(void) {
  String v,v1,v2;
  String data[5];
  MyDataGPS d;
  int a = 0,b = 0;
  GSMport.println("AT+CGNSPWR=1");
  delay(1000);
  Serial.println(ReadGSM());
  GSMport.println("AT+CGNSINF");
  delay(400);
  delay(400);
  v = ReadGSM();
  Serial.println(v);
    
  for(int i=0;i<5;i++){
    a = v.indexOf(",",a);
    if(a!=-1){
      b = v.indexOf(",",a+1);
      data[i] = v.substring(a+1,b);
      Serial.println(String("Received data: " + String(i)+" - "+ data[i]));
      a = b;
    }
  }

  d.latitude = data[2];
  d.longitude = data[3];

  return d;
}

void gprs_init(void) {
  int d = 500;
  Serial.println("---------------GPRS init start----------------");
  delay(d * 4);
  GSMport.println("AT+SAPBR=3,1,\"CONTYPE\",\"GPRS\"");
  delay(d * 5);
  Serial.println(ReadGSM());
  delay(d * 5);
  GSMport.println("AT+SAPBR=3,1,\"APN\",\"internet.ltmsk\""); //Write here APN of your internet provider
  delay(d);
  Serial.println(ReadGSM());
  delay(d * 2);
  GSMport.println("AT+SAPBR=3,1,\"USER\",\"\"");
  delay(d);
  Serial.println(ReadGSM());

  GSMport.println("AT+SAPBR=3,1,\"PWD\",\"\"");
  delay(d);
  Serial.println(ReadGSM());
  delay(d * 2);
  GSMport.println("AT+SAPBR=1,1");
  delay(d * 2);
  Serial.println(ReadGSM());
  delay(d * 2);
  GSMport.println("AT+SAPBR=2,1");
  delay(d);
  Serial.println(ReadGSM());
  delay(d * 5);

  Serial.println("----------------GPRS init complete-------------");
  Serial.println("");
}

String ReadGSM() {
  char c;
  String str;
  while (GSMport.available()) {
    c = GSMport.read();
    str += c;
    delay(20);
  }
  
  str = "<<< " + str;
  return str;
}


void gprs_send(String dataa, String datab, int dataA0, int dataA1) {
  String tempstr;
  Serial.println("Send start");
  GSMport.println("AT+HTTPINIT");
  delay(1000);
  Serial.println(ReadGSM());
  GSMport.println("AT+HTTPPARA=\"CID\",1");
  delay(1000);
  Serial.println(ReadGSM());
  Serial.println("setup url");
  tempstr = String("AT+HTTPPARA=\"URL\",\"146.185.144.144/gps/?user=" + user + "&password=" + pass + "&db=<db>&lat=" + dataa + "&lng=" + datab + "&a0=" + String(dataA0) +"&a1=" + String(dataA1) +"\"");
  GSMport.println(tempstr);
  Serial.println(tempstr);
  delay(1000);
  Serial.println(ReadGSM());
  Serial.println("GET url");
  GSMport.println("AT+HTTPACTION=0");
  delay(1000);
  Serial.println(ReadGSM());
  Serial.println("Send done");
  GSMport.println("AT+HTTPTERM");
  delay(200);
  Serial.println(ReadGSM());
}

