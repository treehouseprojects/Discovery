#include <Servo.h>

// all motorXA pins are connected to PWM, motorXB pins are digital written to
int motor1A = 3;
int motor1B = A0;

int motor2A = 5;
int motor2B = A1;

int motor3A = 6;
int motor3B = A2;

int motor4A = 11;
int motor4B = A3;

int rotVelocity = 190;

Servo armServo, gripperServo, releaseServo, panServo, tiltServo;
int armServoPos = 0;
int gripperServoPos = 0;
int pressServoPos = 0;
boolean released = false;
boolean gripped = false;
boolean lifted = true;

void setup() {
  Serial.begin(9600);

  pinMode(motor1B, OUTPUT);
  pinMode(motor2B, OUTPUT);
  pinMode(motor3B, OUTPUT);
  pinMode(motor4B, OUTPUT);

  armServo.attach(2);
  gripperServo.attach(4);
  releaseServo.attach(8);
  panServo.attach(A4);
  tiltServo.attach(A5);

  armServo.write(90);
  gripperServo.write(20);
  releaseServo.write(47);
  panServo.write(90);
  tiltServo.write(90);

  analogWrite(motor1A, 255);
  digitalWrite(motor1B, HIGH);  
  analogWrite(motor2A, 255);  
  digitalWrite(motor2B, HIGH);

  analogWrite(motor3A, 255);
  digitalWrite(motor3B, HIGH);  
  analogWrite(motor4A, 255);  
  digitalWrite(motor4B, HIGH);
}

void loop()  {
  if (Serial.available() > 0) {
    // read the incoming byte:
    int command = Serial.read();

    if (command == 'R' || command == 'L' || command == 'F' || command == 'B')  {
      while (Serial.available() <= 0)  {
      }
      int velocity = Serial.read();
      moveMotors(command, velocity);
    }

    else if (command == 'S')  {
      while (Serial.available() <= 0)  {
      }
      char missionCommand = Serial.read();
      engageMission(missionCommand);
    }

    else if (command == '>' || command == '<' || command == '^' || command == 'v')  {
      while (Serial.available() <= 0)  {
      }
      int pos = Serial.read();
      moveCamera(command, pos);
    }

    else if (command == 'T')  {
      while (Serial.available() <= 0)  {
      }
      char rotationCommand = Serial.read();
      rotateAnemo(rotationCommand);
    }
  }
}

void moveMotors(char dirn, int velocity)  {
  if (dirn == 'R')  {
    analogWrite(motor1A, (velocity - 254) * -2);
    digitalWrite(motor1B, HIGH);  
    analogWrite(motor2A, (velocity - 254) * -2);  
    digitalWrite(motor2B, HIGH);
  }
  else if (dirn == 'L')  {
    analogWrite(motor1A, (velocity - 127) * 2);
    digitalWrite(motor1B, LOW);  
    analogWrite(motor2A, (velocity - 127) * 2);  
    digitalWrite(motor2B, LOW);
  }
  else if (dirn == 'F')  {
    analogWrite(motor3A, (velocity - 254) * -2);
    digitalWrite(motor3B, HIGH);  
    analogWrite(motor4A, (velocity - 254) * -2);  
    digitalWrite(motor4B, HIGH);
  }
  else if (dirn == 'B')  {
    analogWrite(motor3A, (velocity - 127) * 2);
    digitalWrite(motor3B, LOW);  
    analogWrite(motor4A, (velocity - 127) * 2);  
    digitalWrite(motor4B, LOW);
  }
}

void engageMission(char missionCommand)  {
  if (missionCommand == 'r')  {
    if (released == false)  {
      releaseServo.write(120);
      released = true;
    }
    else  {
      releaseServo.write(47);
      released = false;
    }
  }
  else if (missionCommand == 'g')  {
    if (gripped == false)  {
      gripperServo.write(80);
      gripped = true;
    }
    else  {
      gripperServo.write(20);
      gripped = false;
    }
  }
  else if (missionCommand == 'm')  {
    if (lifted == true)  {
      armServo.write(30);
      lifted = false;
    }
    else  {
      armServo.write(90);
      lifted = true;
    }
  }
}

void moveCamera(char dirn, int pos)  {
  int servoPos = pos - 125; //will be between 0 and 90

  if (dirn == '>')
    panServo.write(90 + servoPos - 5);
  else if (dirn == '<')
    panServo.write(90 - servoPos + 5);
  else if (dirn == '^')
    tiltServo.write(90 + servoPos - 5);
  else if (dirn == 'v')
    tiltServo.write(90 - servoPos + 5);
}

void rotateAnemo(char dirn)  {
  if (dirn == 'w')  {
    analogWrite(motor1A, (rotVelocity - 254) * -2);
    digitalWrite(motor1B, HIGH);
    analogWrite(motor2A, (rotVelocity - 127) * 2);  
    digitalWrite(motor2B, LOW);  
    analogWrite(motor3A, (rotVelocity - 254) * -2);
    digitalWrite(motor3B, HIGH);
    analogWrite(motor4A, (rotVelocity - 127) * 2);  
    digitalWrite(motor4B, LOW);
  }
  else if (dirn == 'c')  {
    analogWrite(motor1A, (rotVelocity - 127) * 2);
    digitalWrite(motor1B, LOW);
    analogWrite(motor2A, (rotVelocity - 254) * -2);  
    digitalWrite(motor2B, HIGH);
    analogWrite(motor3A, (rotVelocity - 127) * 2);
    digitalWrite(motor3B, LOW); 
    analogWrite(motor4A, (rotVelocity - 254) * -2);  
    digitalWrite(motor4B, HIGH);
  }
  else if (dirn == 's')  {
    analogWrite(motor1A, 0);  
    digitalWrite(motor1B, LOW);  
    analogWrite(motor2A, 0);  
    digitalWrite(motor2B, LOW);  
    analogWrite(motor3A, 0);  
    digitalWrite(motor3B, LOW);  
    analogWrite(motor4A, 0);  
    digitalWrite(motor4B, LOW);  
  }
}







