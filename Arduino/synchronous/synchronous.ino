#include <SoftwareSerial.h>  
#include <AFMotor.h>
#include <NewPing.h>
#include <TimedAction.h>

#define ECHO_PIN A5 // Echo Pin
#define TRIGGER_PIN A4 // Trigger Pin
#define MAX_DISTANCE 200 // Maximum range needed

#define BT_TX A3  // TX-O pin of bluetooth mate, Arduino D2
#define BT_RX A2  // RX-I pin of bluetooth mate, Arduino D3

int useSerialLog = 0;
int bufferIndex = 0;
int proxIndex = 1;

// Message Type
const int MOVE_MESSAGE_TYPE = 1; // 0000_0001
const int GET_PING_MESSAGE_TYPE = 2; // 0000_0010
const int MESSAGE_TYPE_BITS = 3; // 0000_0011
	
// DIRECTION
const int FW = 4;  // 00000100
const int REVERSE = 8;  // 00001000
const int LEFT    = 12; // 00001100
const int RIGHT   = 16; // 00010000
const int STOP    = 20; // 00010100	
const int DIRECTION_BITS = 28; // 00011100
	
// SPEED
const int SLOW 	= 0;   // 00100000
const int MEDIUM  = 64;  // 01000000
const int FAST    = 96;  // 01100000
const int FULL    = 128; // 10000000	
const int SPEED_BITS   = 224; // 11100000

volatile boolean READING = false;
volatile boolean WRITING = false;

SoftwareSerial bluetooth(BT_TX, BT_RX);
NewPing sonar(TRIGGER_PIN, ECHO_PIN, MAX_DISTANCE); // NewPing setup of pins and maximum distance.

AF_DCMotor rightMotor(1); // create motor #2, 64KHz pwm
AF_DCMotor leftMotor(2); // create motor #2, 64KHz pwm

void setup()
{ 
  Serial.begin(9600);           // set up Serial library at 9600 bps
  bluetooth.begin(115200);  // The Bluetooth Mate defaults to 115200bps
  bluetooth.print("$");
  bluetooth.print("$");
  bluetooth.print("$");  // Enter command mode
  delay(100);  // Short delay, wait for the Mate to send back CMD
  bluetooth.println("U,9600,N");  // Temporarily Change the baudrate to 9600, no parity
  // 115200 can be too fast at times for NewSoftSerial to relay the data reliably
  bluetooth.begin(9600);  // Start bluetooth serial at 9600
  Serial.println("Arduino Started");
}


void loop()
{
  byte message = 0;
  while((message = readBluetoothMessageByte()) > 0) {
    int messageType = message & MESSAGE_TYPE_BITS;
    Serial.print("Message type: ");    
    Serial.println(messageType);
    switch(messageType) {
    case MOVE_MESSAGE_TYPE:
        Serial.print("Message direction: ");
        Serial.println(message & DIRECTION_BITS);
        moveDirection(message & DIRECTION_BITS, message & SPEED_BITS);
        break;
    case GET_PING_MESSAGE_TYPE:
        measureAndTransmitProximity();
        break;    
    }
  }
}

byte readBluetoothMessageByte() {
  while(WRITING) {};
  
  READING = true;
  byte message = 0;    
  
  if(bluetooth.available() > 0) {
    message = bluetooth.read();
    Serial.print("Message received: ");
    Serial.println(message);
  }
  
  READING = false;
  return message;
}

void moveDirection(int dir, int powerInt) {
  float power;
  switch(powerInt) {
  case SLOW:
    power = .25;
    break;
  case MEDIUM:
    power = .50;
    break;
  case FAST:
    power = .75;
    break;
  case FULL:
    power = 1;
    break;
  default:
    power = 0;
  }
  
  switch(dir) {
  case LEFT:
    moveMotors(FORWARD, 255 * power, FORWARD, 255 * power);
    break;
  case RIGHT:
    moveMotors(BACKWARD, 255 * power, BACKWARD, 255 * power);
    break;
  case FW:
    moveMotors(BACKWARD, 255 * power, FORWARD, 255 * power);
    break;
  case REVERSE:
    moveMotors(FORWARD, 255 * power, BACKWARD, 255 * power);
    break;  
  default:
    moveMotors(RELEASE, 0, RELEASE, 0);
    break;
  }   
}

//void logMessage(char* message) {
//  if(useSerialLog) {
//    Serial.println(message);
//  } else {
//    bluetooth.print("{ \"type\" : \"LOG\", \"message\" : \"");
//    bluetooth.print(message);
//    bluetooth.println("\" }");
//  }
//}

void measureAndTransmitProximity() {  
  int prox = readProximity();
  sendProximity(prox);
}

void sendProximity(int proximity) {
  String message = "{\"type\":\"PROXIMITY\",\"value\":";
  message += proximity;
  message += "}";
  sendBluetoothMessage(message);
}

void sendBluetoothMessage(String message) {
  while(READING) {};
  
  WRITING = true;
  bluetooth.println(message);
  WRITING = false;
}

void moveMotors(int lmDir, int lmSpeed, int rmDir, int rmSpeed) {
  leftMotor.setSpeed(lmSpeed); 
  rightMotor.setSpeed(rmSpeed);     // set the speed to 200/255
  leftMotor.run(lmDir);    // set the speed to 200/255
  rightMotor.run(rmDir);
}

int readProximity() {
  int cm = sonar.ping_cm(); // Send ping, get ping time in microseconds (uS).
  return cm;
}

