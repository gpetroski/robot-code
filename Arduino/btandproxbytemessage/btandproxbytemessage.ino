#include <SoftwareSerial.h>  
#include <AFMotor.h>
#include <aJSON.h>

#define echoPin 7 // Echo Pin
#define trigPin 8 // Trigger Pin

AF_DCMotor rightMotor(1); // create motor #2, 64KHz pwm
AF_DCMotor leftMotor(2); // create motor #2, 64KHz pwm

int bluetoothTx = 2;  // TX-O pin of bluetooth mate, Arduino D2
int bluetoothRx = 3;  // RX-I pin of bluetooth mate, Arduino D3

int maximumRange = 200; // Maximum range needed
int minimumRange = 0; // Minimum range needed
int useSerialLog = 0;
int bufferIndex = 0;
int proxIndex = 0;

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

SoftwareSerial bluetooth(bluetoothTx, bluetoothRx);

void setup()
{ 
  Serial.begin(9600);           // set up Serial library at 9600 bps
  bluetooth.begin(115200);  // The Bluetooth Mate defaults to 115200bps
  bluetooth.print("$$$");  // Enter command mode
  delay(100);  // Short delay, wait for the Mate to send back CMD
  bluetooth.println("U,9600,N");  // Temporarily Change the baudrate to 9600, no parity
  // 115200 can be too fast at times for NewSoftSerial to relay the data reliably
  bluetooth.begin(9600);  // Start bluetooth serial at 9600
  pinMode(trigPin, OUTPUT);
  pinMode(echoPin, INPUT);
  logMessage("Arduino Started");
}


void loop()
{
  int message = 0;
  while(message = readBluetoothMessageByte()) {
    Serial.print("Message type ");
    Serial.println(message & MESSAGE_TYPE_BITS);
    if((message & MESSAGE_TYPE_BITS) == MOVE_MESSAGE_TYPE) {
      Serial.println(message & DIRECTION_BITS);
      Serial.println(message & SPEED_BITS);
      moveDirection(message & DIRECTION_BITS, message & SPEED_BITS);
    }
  }
  
  int prox = readProximity();
  sendProximity(prox);
}

int readBluetoothMessageByte() {
  int message = 0;    
  
  if(bluetooth.available()) {    
    Serial.println(bluetooth.available());
    message = bluetooth.read();
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
    moveMotors(FORWARD, 150 * power, FORWARD, 150 * power);
    break;
  case RIGHT:
    logMessage("Moving right");
    moveMotors(BACKWARD, 150 * power, BACKWARD, 150 * power);
    break;
  case FW:
    logMessage("Moving forward");
    moveMotors(BACKWARD, 155 * power, FORWARD, 255 * power);
    break;
  case REVERSE:
    logMessage("Moving backward");
    moveMotors(FORWARD, 100 * power, BACKWARD, 200 * power);
    break;  
  default:
    logMessage("Stopping motors");
    moveMotors(RELEASE, 0, RELEASE, 0);
    break;
  }   
}

void logMessage(char* message) {
  if(useSerialLog) {
    Serial.print("{ \"type\" : \"LOG\", \"message\" : \"");
    Serial.print(message);
    Serial.println("\" }");
  } else {
    bluetooth.print("{ \"type\" : \"LOG\", \"message\" : \"");
    bluetooth.print(message);
    bluetooth.println("\" }");
  }
}

void sendProximity(int proximity) {
  if(useSerialLog) {
    Serial.print("{ \"type\" : \"PROXIMITY\", \"value\" : ");
    Serial.print(proximity);
    Serial.println(" }");
  } else {
    bluetooth.print("{ \"type\" : \"PROXIMITY\", \"value\" : ");
    bluetooth.print(proximity);
    bluetooth.println(" }");
  }
}

void moveMotors(int lmDir, int lmSpeed, int rmDir, int rmSpeed) {
  leftMotor.setSpeed(lmSpeed); 
  rightMotor.setSpeed(rmSpeed);     // set the speed to 200/255
  leftMotor.run(lmDir);    // set the speed to 200/255
  rightMotor.run(rmDir);
}

int readProximity() {
  int duration, distance; // Duration used to calculate distance
  delay(50);
  /* The following trigPin/echoPin cycle is used to determine the
   distance of the nearest object by bouncing soundwaves off of it. */
  digitalWrite(trigPin, LOW); 
  delayMicroseconds(2); 

  digitalWrite(trigPin, HIGH);
  delayMicroseconds(10); 

  digitalWrite(trigPin, LOW);
  duration = pulseIn(echoPin, HIGH);

  //Calculate the distance (in cm) based on the speed of sound.
  distance = duration/58.2;

  if (distance >= maximumRange || distance <= minimumRange){
    /* Send a negative number to computer and Turn LED ON 
     to indicate "out of range" */
    return -1;
  }
  else {
    /* Send the distance to the computer using Serial protocol, and
     turn LED OFF to indicate successful reading. */
    return distance;
  }
}

