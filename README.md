# remote-control-plane
This is a repository of all the software for my RC plane. If you want any more info, add it as a github issue and I'll add it. I can share you an in-depth part list and plane info spreadsheet, too.

# Hardware Info
The plane has *very* simple hardware:
- A Raspberry Pi 3B
- A couple $30 EDFs that put out 1.32KG each (5.82lbs of force)
- A couple 50A ESCs (for a theoretical output of 1440W)
- A couple 7.4V 5200AH batteries
- 5 micro servos
- A wooden box frame and rectangular wing

# Software Info
It contains software to:
- read a BNO055 sensor (acceleration,rotation,etc) using Pi4J,
- software to control flaps (Ailerons, Flaps, Rudders, etc)

# Compiling
To compile, just get "pi4j-core-1.2.jar" and add it as a library.
Then, compile a JAR, put that on the Raspberry Pi, and run "java -jar plane.jar"
