# RC Manager
This is a repository of all the software for Remote Control devices I have made (A car and a plane).

Currently, the software used to control the RC car is very clean and well documented, and I would recommend using it as an example, if desired.
 
If you want any more info, add it as a github issue, and I'll gladly help you and add it. I can share you an in-depth part list and plane info spreadsheet, too.
 
# Software Info
It contains software to:

- Receive JSON commands from a server, which can enable, disable, or communicate with modules
- read a BNO055 sensor (acceleration,rotation,etc) using Pi4J,
- Write to a L298N Motor Controller
- Read a HC-SR04 Ultrasonic Depth Sensor
- software to control flaps (Ailerons, Flaps, Rudders, etc)

The best part is, all of these sensors are completely modular, so if you wanted to, say, have five L298N boards, you would only need to change one file (net.datadeer.car.Car)

## My RC Car

I built the RC car from:

- ~$30 A Raspberry Pi 3B (Code would work on any Raspberry Pi)
- ~$30 (~$20) Two 7.4v 5.2ah LiPo batteries (Later switched out with one 12v 7.2ah SLA battery)
- ~$20 A XH-M609 (Stops batteries from discharging too low and irreversibly dying, trust me you need one)
- ~$15 A bunch of HC-SR04 sensors
- ~$60 A Robot Car Chassis Kit (Since I didn't have a 3D printer or any machining tools)

## My RC Plane
I made a plane with a 3' wingspan, the control software's in here

### Hardware Info
The net.datadeer.plane has *very* simple hardware:
- A Raspberry Pi 3B
- A couple $30 EDFs that put out 1.32KG each (5.82lbs of force)
- A couple 50A ESCs (for a theoretical output of 1440W)
- A couple 7.4V 5200AH batteries
- 5 micro servos
- A wooden box frame and rectangular wing

# Compiling
To compile, just get "pi4j-core-1.2.jar" and add it as a library.
Then, compile a JAR, put that on the Raspberry Pi, and run "java -jar (jarname)"
