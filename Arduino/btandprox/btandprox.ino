#include <SoftwareSerial.h>  
#include <AFMotor.h>

#define echoPin 7 // Echo Pin
#define trigPin 8 // Trigger Pin

AF_DCMotor rightMotor(1); // create motor #2, 64KHz pwm
AF_DCMotor leftMotor(2); // create motor #2, 64KHz pwm

int bluetoothTx = 2;  // TX-O pin of bluetooth mate, Arduino D2
int bluetoothRx = 3;  // RX-I pin of bluetooth mate, Arduino D3

int maximumRange = 200; // Maximum range needed
int minimumRange = 0; // Minimum range needed
long duration, distance; // Duration used to calculate distance

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
}


void loop()
{
  if(bluetooth.available())  // If the bluetooth sent any characters
  {
    // Send any characters the bluetooth prints to the serial monitor
    char signal = (char)bluetooth.read();
    Serial.println(signal);
    if(signal == 'l' || signal == 'r' || signal=='f' || signal == 'b'|| signal == 's') {
      moveDirection(signal);
    } else {
       bluetooth.println("Invalid character " + signal); 
    }
    
  }
  // Send any characters the Serial monitor prints to the bluetooth
  long prox = readProximity();
  Serial.println(prox);
  bluetooth.println(prox);
}

void moveDirection(char dir) {
   switch(dir) {
     case 'l':
       bluetooth.println("Moving left");
       moveMotors(FORWARD, 150, FORWARD, 150);
      break;
     case 'r':
       bluetooth.println("Moving right");
       moveMotors(BACKWARD, 150, BACKWARD, 150);
      break;
     case 'f':
       bluetooth.println("Moving forward");
       moveMotors(BACKWARD, 155, FORWARD, 255);
      break;
     case 'b':
       bluetooth.println("Moving backward");
       moveMotors(FORWARD, 100, BACKWARD, 200);
      break;  
     default:
       bluetooth.println("Stopping motors");
       moveMotors(RELEASE, 0, RELEASE, 0);
      break;
   }   
}

void moveMotors(int lmDir, int lmSpeed, int rmDir, int rmSpeed) {
  leftMotor.setSpeed(lmSpeed); 
  rightMotor.setSpeed(rmSpeed);     // set the speed to 200/255
  leftMotor.run(lmDir);    // set the speed to 200/255
  rightMotor.run(rmDir);
}

long readProximity() {
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
