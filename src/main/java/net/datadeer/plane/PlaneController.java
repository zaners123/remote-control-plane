package net.datadeer.plane;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 The main abstract class in this whole thing. Reads sensors and user input, writes pwmController and flapController.
 */
class PlaneController {
    PlaneController() {
        initialize();
    }

    void initialize() {
        new Thread(this::networkThread).start();
        new Thread(this::controlThread).start();
    }

    //A super-optional feature assuming you want to actually control the damn thing. Starts a server on PORT, reading input
    void networkThread() {
        ServerSocket serverSocket = null;

        do {
            try {
                serverSocket = new ServerSocket(Plane.PORT);
            } catch (IOException e) {
                System.err.println("Failed at making server socket. Retrying...");
                e.printStackTrace();
            }
        } while (serverSocket == null);
        System.out.println("Running server on "+serverSocket.getInetAddress().toString());
        while (Plane.keepRunningThreads) {
            try {
                //respond to input, give output
                Socket sock = serverSocket.accept();
                BufferedReader read = new BufferedReader(new InputStreamReader(sock.getInputStream()));

                //todo have network input
            } catch (IOException IOE) {
                IOE.printStackTrace();
                System.err.println("Failed IO on networking input");
            }

            Plane.sleep(1000);//todo remove (or make 10 or so)
        }
    }

    //main VVVVVVVVVVVVVVVV
    //main Network thread writes to input variables
    //main VVVVVVVVVVVVVVVV

    //percent throttle (0=off, 100=full)
    float inputThrottle = 0;
    //roll (AILERON) offset percent (-100=counterclockwise/left,    0=straight, 100=clockwise/right)
    float inputTurn = 0;
    //pitch (ELEVATOR) offset percent (-100=down,   0=straight,   100=up)
    float inputPitch = 0;

    //todo consider not having YAW input (since not having it abstracts controls from server)
    //yaw (RUDDER) offset percent (-100=left,   0=straight,   100=right)
    //float inputYaw = 0;

    //main VVVVVVVVVVVVVVVV
    //main input variables are read by controlThread
    //main VVVVVVVVVVVVVVVV

    void controlThread() {
        while (Plane.keepRunningThreads) {
//            boolean useSensorData = Plane.sensor.readUpdatedSensorData();
            long start = System.currentTimeMillis();
            //todo base off of inputs

            //todo implement differential thrust (when turning, alternate ailerons, point rudder like a tire, increase outer throttle, decrease inner throttle)

            //todo autopilot mode that bases input off of sensor (but user input always overrides)
            Plane.pwmController.writePercent(PWMController.PwmPin.LEFT_AILERON,           50);

            //TODO prop controls here, too
//            Plane.pwmController.writePercent(0);

            System.out.println("Control thread takes "+(System.currentTimeMillis() - start)+"ms");
        }
    }
}

