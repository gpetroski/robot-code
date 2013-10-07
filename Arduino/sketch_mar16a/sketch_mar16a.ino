#include <AFMotor.h>

AF_DCMotor motor1(1); // create motor #2, 64KHz pwm
AF_DCMotor motor2(2); // create motor #2, 64KHz pwm

void setup() {
  Serial.begin(9600);           // set up Serial library at 9600 bps
  Serial.println("Motor test!");
  
  motor1.setSpeed(150);     // set the speed to 200/255
  motor2.setSpeed(150);     // set the speed to 200/255
}

void loop() {
  Serial.print("tick");
  
  motor1.run(RELEASE);      // turn it on going forward
  motor2.run(RELEASE);      // turn it on going forward
  delay(5000);
  motor1.run(FORWARD);      // stopped
  motor2.run(FORWARD);      // stopped
  delay(2000);
//
//  Serial.print("tock");
//  motor.run(BACKWARD);     // the other way
//  delay(1000);
//  
//  Serial.print("tack");
//  motor.run(RELEASE);      // stopped
//  delay(1000);
}
