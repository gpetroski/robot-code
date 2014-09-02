#include <SoftwareSerial.h>  
#include <AFMotor.h>
#include <NewPing.h>
#include <TimedAction.h>

#define ECHO_PIN A5 // Echo Pin
#define TRIGGER_PIN A4 // Trigger Pin
#define MAX_DISTANCE 200 // Maximum range needed

#define BT_TX A3  // TX-O pin of bluetooth mate, Arduino D2
#define BT_RX A2  // RX-I pin of bluetooth mate, Arduino D3

int useSerialLog = 1;
int bufferIndex = 0;
int proxIndex = 1;
int proxMin = 10;

// Message Type
const int MOVE_MESSAGE_TYPE = 1; // 00000001
const int MESSAGE_TYPE_BITS = 3; // 00000011
	
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

SoftwareSerial bluetooth(BT_TX, BT_RX);
TimedAction timedAction = TimedAction(100, measureAndTransmitProximity);
NewPing sonar(TRIGGER_PIN, ECHO_PIN, MAX_DISTANCE); // NewPing setup of pins and maximum distance.

AF_DCMotor rightMotor(1); // create motor #2, 64KHz pwm
AF_DCMotor leftMotor(2); // create motor #2, 64KHz pwm

void setup()
{ 
  Serial.begin(115200);           // set up Serial library at 9600 bps
  bluetooth.begin(115200);  // The Bluetooth Mate defaults to 115200bps
  bluetooth.print("$$$");  // Enter command mode
  delay(100);  // Short delay, wait for the Mate to send back CMD
  bluetooth.println("U,9600,N");  // Temporarily Change the baudrate to 9600, no parity
  // 115200 can be too fast at times for NewSoftSerial to relay the data reliably
  bluetooth.begin(9600);  // Start bluetooth serial at 9600
  logMessage("Arduino Started");
}


void loop()
{
  int message = 0;
  while((message = readBluetoothMessageByte()) > 0) {
    Serial.print("Message type ");
    Serial.println(message & MESSAGE_TYPE_BITS);
    if((message & MESSAGE_TYPE_BITS) == MOVE_MESSAGE_TYPE) {
      Serial.println(message & DIRECTION_BITS);
      Serial.println(message & SPEED_BITS);
      moveDirection(message & DIRECTION_BITS, message & SPEED_BITS);
    }
  }
  timedAction.check();
}

byte readBluetoothMessageByte() {
  byte message = 0;    
  
  if(bluetooth.available() > 0) {    
    message = bluetooth.read();
    Serial.print("Message received: ");
    Serial.println(message);
  }
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
    logMessage("Moving left");
    moveMotors(FORWARD, 255 * power, FORWARD, 255 * power);
    break;
  case RIGHT:
    logMessage("Moving right");
    moveMotors(BACKWARD, 255 * power, BACKWARD, 255 * power);
    break;
  case FW:
    logMessage("Moving forward");
    moveMotors(BACKWARD, 255 * power, FORWARD, 255 * power);
    break;
  case REVERSE:
    logMessage("Moving backward");
    moveMotors(FORWARD, 255 * power, BACKWARD, 255 * power);
    break;  
  default:
    logMessage("Stopping motors");
    moveMotors(RELEASE, 0, RELEASE, 0);
    break;
  }   
}

void logMessage(char* message) {
  if(useSerialLog) {
    Serial.println(message);
  } else {
    bluetooth.print("{ \"type\" : \"LOG\", \"message\" : \"");
    bluetooth.print(message);
    bluetooth.println("\" }");
  }
}

void measureAndTransmitProximity() {  
  int prox = readProximity();
  sendProximity(prox);
}

void sendProximity(int proximity) {
  Serial.print("Sending proximity ");
  Serial.println(proximity);
  bluetooth.print("{ \"type\" : \"PROXIMITY\", \"value\" : ");
  bluetooth.print(proximity);
  bluetooth.println(" }");
}

void moveMotors(int lmDir, int lmSpeed, int rmDir, int rmSpeed) {
  leftMotor.setSpeed(lmSpeed); 
  rightMotor.setSpeed(rmSpeed);     // set the speed to 200/255
  leftMotor.run(lmDir);    // set the speed to 200/255
  rightMotor.run(rmDir);
}

int readProximity() {
  int cm = sonar.ping_cm(); // Send ping, get ping time in microseconds (uS).
  Serial.print("Ping: ");
  Serial.print(cm); // Convert ping time to distance in cm and print result (0 = outside set distance range)
  Serial.println("cm");
  return cm;
}

