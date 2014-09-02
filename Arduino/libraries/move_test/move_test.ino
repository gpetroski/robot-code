#include <AFMotor.h>

#define echoPin 7 // Echo Pin
#define trigPin 8 // Trigger Pin

AF_DCMotor rightMotor(1); // create motor #2, 64KHz pwm
AF_DCMotor leftMotor(2); // create motor #2, 64KHz pwm

void setup()
{ 
  // do nothing
}

int count = 0;
float speedFactor = .8;

void loop()
{
  if(count < 5) {
    moveTest();
    count++;
    Serial.println(count);
  }
}

void moveTest() {
  Serial.println("Moving forward");
  moveMotors(FORWARD, 255, FORWARD, 255);
  delay(1000);
  
  Serial.println("Moving right");
  moveMotors(BACKWARD, 255, BACKWARD, 255);
  delay(1000);
     
  Serial.println("Moving forward");
  moveMotors(BACKWARD, 255, FORWARD, 255);
  delay(1000);
       
  Serial.println("Moving backward");
  moveMotors(FORWARD, 255, BACKWARD, 255);
  delay(1000);
       
  Serial.println("Stopping motors");
  moveMotors(RELEASE, 0, RELEASE, 0);
}  

void moveMotors(int lmDir, int lmSpeed, int rmDir, int rmSpeed) {
  leftMotor.setSpeed(lmSpeed); 
  rightMotor.setSpeed(rmSpeed);     // set the speed to 200/255
  leftMotor.run(lmDir);    // set the speed to 200/255
  rightMotor.run(rmDir);
}
