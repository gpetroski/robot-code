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
  char* btData = readBluetoothData();
  if(btData != NULL) {
    Serial.println(btData);
    aJsonObject* command = aJson.parse(btData);
    aJsonObject* type = aJson.getObjectItem(command, "type");
    if(strcmp("MOVE",type->valuestring) == 0) {
      aJsonObject* dir = aJson.getObjectItem(command, "direction");
      aJsonObject* power = aJson.getObjectItem(command, "power");
      moveDirection(dir->valuestring[0], power->valuefloat);
    }
    aJson.deleteItem(command);
  }
  
  if(proxIndex % 100 == 0) {
    proxIndex = 0;
    int prox = readProximity();
    sendProximity(prox);
  } else {
    proxIndex++;
  }
}

char* readBluetoothData() {
  char buffer[255];
  int finished = 0;
  for(int i = 0; i < bluetooth.available() && !finished; i++ ) {
    Serial.println(bluetooth.available());
    char signal = bluetooth.read();
    buffer[bufferIndex] = signal;
    if(signal == '}') {
      buffer[bufferIndex + 1] = '\0';
      bufferIndex = 0; 
      finished = 1; 
    } else {
      bufferIndex++;
    }
  }
  if(finished) {
    return buffer;
  } else {
    return NULL;
  }  
}

void moveDirection(char dir, float power) {
  switch(dir) {
  case 'L':
    logMessage("Moving left");
    moveMotors(FORWARD, 150 * power, FORWARD, 150 * power);
    break;
  case 'R':
    logMessage("Moving right");
    moveMotors(BACKWARD, 150 * power, BACKWARD, 150 * power);
    break;
  case 'F':
    logMessage("Moving forward");
    moveMotors(BACKWARD, 155 * power, FORWARD, 255 * power);
    break;
  case 'B':
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

