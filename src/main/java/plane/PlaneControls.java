package plane;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 The main abstract class in this whole thing. Reads sensors and user input, writes propController and flapController.
 */
class PlaneControls {
    PlaneControls() {

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
    float inputRoll = 0;
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
            Plane.sensor.getUpdatedSensorData();
            //todo demystify
            float power = Plane.sensor.getEuler().y/180;
            power = (power<=1) ? (-power) : (2-power);
            int writeval = (int)(5*power);
            System.out.println("Euler: "+Plane.sensor.getEuler().toString() +" into "+writeval);
            long start = System.currentTimeMillis();
            //todo base off of inputs


            //todo implement differential thrust (when turning, alternate ailerons, point rudder like a tire, increase outer throttle, decrease inner throttle)

            //todo autopilot mode that bases input off of sensor (but user input always overrides)
            Plane.flapController.writeFlap(FlapController.FlapPin.RUDDER,           writeval);
            //todo aileron differential (use rudder when turning so tail doesn't lower?)
            Plane.flapController.writeFlap(FlapController.FlapPin.LEFT_AILERON,     writeval);
            Plane.flapController.writeFlap(FlapController.FlapPin.RIGHT_AILERON,    writeval);
            Plane.flapController.writeFlap(FlapController.FlapPin.LEFT_ELEVATOR,    writeval);
            Plane.flapController.writeFlap(FlapController.FlapPin.RIGHT_ELEVATOR,   writeval);


            //TODO prop controls here, too
//            Plane.propController.writeProp(0);


            System.out.println("Control thread takes "+(System.currentTimeMillis() - start)+"ms");
        }
    }
}

