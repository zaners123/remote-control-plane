package plane;

import com.pi4j.io.gpio.*;
import com.pi4j.wiringpi.SoftPwm;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Scanner;

/*

UART help:
https://medium.com/@erleczarmantos/connecting-the-bosch-bno055-shuttleboard-to-the-raspberry-pi-72e8a86048d4


*/
public class App {

    private final GpioController gpio = GpioFactory.getInstance();
    private HashMap<Integer, GpioPinDigitalOutput> pinMap = new HashMap<>();

    private final static int GPIO_STATUS_LIGHT   = 0;
    private final static int GPIO_PROP           = 1;//has to be pin 1,24,26, or 23 (in that order, maybe)
    private final static int GPIO_LEFT_AILERON   = 2;
    private final static int GPIO_RIGHT_AILERON  = 3;
    private final static int GPIO_RUDDER         = 4;
    private final static int GPIO_LEFT_ELEVATOR  = 5;
    private final static int GPIO_RIGHT_ELEVATOR = 6;
    private final static int[] pwmPins = new int[]{GPIO_RUDDER, GPIO_LEFT_AILERON, GPIO_RIGHT_AILERON, GPIO_LEFT_ELEVATOR, GPIO_RIGHT_ELEVATOR};
    private final static int SERVO_PWM_MIN = 3;
    private final static int SERVO_PWM_MAX = 27;
    private final static int SERVO_PWM_MID = (SERVO_PWM_MAX+SERVO_PWM_MIN)/2;


    public static void main(String[] args) {
        new App();
    }


    private volatile BNO055 sensor                      = null;
    private volatile FlapController flaps               = null;
    private volatile StatusLight statusLight            = null;
    private volatile boolean keepRunningThreads         = true;

    private App() {
        //main start flight
        System.out.println("Running.");
        //flash LED
        new Thread(statusLight = new StatusLight(GPIO_STATUS_LIGHT)).start();
        //start reading
        sensor = new BNO055();
        //start flaps
        flaps = new FlapController();
        System.out.println("All initializers called.");
        Scanner s = new Scanner(System.in);
        do {System.out.println("type 'end' to end");} while (!s.nextLine().equals("end"));
        System.out.println("ENDING; THIS BETTER BE ON THE GROUND");
        System.out.println("\tStopping threads");
        keepRunningThreads = false;
        sleep(500);
        System.out.println("\tClosing classes");
        //main done flying, deconstruct all
        sensor.close();
        flaps.close();
        sleep(500);
        System.out.println("Done... Exiting...");
        System.exit(0);
    }

    class FlapController {
        private void initializeFlaps() {
            //run softPwmCreate on every GPIO in pwmPins (unless it's -1, then it isn't yet made)
            for (int flap : pwmPins) {
                if (flap==-1) continue;
                //start them off in the middle
                SoftPwm.softPwmCreate(flap,SERVO_PWM_MID,SERVO_PWM_MAX);
            }
        }
        /**@ground please be on it*/
        void close() {
            //run softPwmStop on every GPIO in pwmPins (unless it's -1, then it isn't yet made)
            for (int flap : pwmPins) {
                if (flap==-1) continue;
                SoftPwm.softPwmStop(flap);
            }
        }
        FlapController() {
            initializeFlaps();
        }
        private void writeFlap(int id, int value) {
            if (id==-1) {
                System.err.println("writeFlap bad ID. ID="+id+" Value="+value);
            }

            if (value < SERVO_PWM_MIN) {
                System.err.println("writeFlap too low.  ID="+id+" Value="+value);
                value = SERVO_PWM_MIN;
            } else if (value > SERVO_PWM_MAX) {
                System.err.println("writeFlap too high. ID="+id+" Value="+value);
                value = SERVO_PWM_MAX;
            }
            SoftPwm.softPwmWrite(id,value);
        }
    }
    class PropController {
        //values calibrated as the literal max and min of signal input
        private final static int CALIBRATED_UNSAFE_MIN = 0;
        private final static int CALIBRATED_UNSAFE_MAX = 24;

        //values that are the safe input to use
        private final static int SAFE_MIN = 15;
        private final static int SAFE_MAX = 19;



        PropController() {
            SoftPwm.softPwmCreate(GPIO_PROP, 0,100);
            calibrate();
        }
        //main calibrate props. Currently done manually
        //todo automate
        private void calibrate() {
            Scanner s = new Scanner(System.in);
            System.out.println("Press enter when ready?");
            s.nextLine();
            boolean t = true;
            while (t) {
                int out;
                try {
                    String line = s.nextLine();
                    if (line.equals("end")) break;
                    out = Integer.parseInt(line);
                } catch (NumberFormatException e) {
                    System.out.println("Not a number.");
                    continue;
                }
                SoftPwm.softPwmWrite(GPIO_PROP, out);
                System.out.println("Sending "+out);
            }
        }
        void writeProp(float percent) {
            if (percent < 0) {
                System.err.println("writeProp called with percent < 0 ("+percent+")");
                percent = 0;
            } else if (percent > 100) {
                System.err.println("writeProp called with percent > 100 ("+percent+")");
                percent = 100;
            }

            int power;
            if (percent==0) {
                //use actual zero on off
                power = 0;
            } else {
                power = (int)(SAFE_MIN + ((percent/100) * (SAFE_MAX-SAFE_MIN)));
            }
            System.out.println("writeProp percent "+percent+" turned into power "+power);
            SoftPwm.softPwmWrite(GPIO_PROP, power);
        }
        void close() {
            SoftPwm.softPwmStop(GPIO_PROP);
        }
    }
    class StatusLight implements Runnable {
        int pin;
        StatusLight(int pin) {
            this.pin = pin;
        }
        public void run() {
            GpioPinDigitalOutput statusLight = getDigitalGPIO(pin);
            while (keepRunningThreads) {
                statusLight.high();
                sleep(250);
                statusLight.low();
                sleep(250);
            }
        }
    }
    /**



     */
    class PlaneControls {
        final static int PORT = 51919;
        ServerSocket serverSocket = null;

        void controlThread() {
            while (keepRunningThreads) {
                sensor.getUpdatedSensorData();
                float power = sensor.getEuler().y;
                if (power <= 180) {
                    //pointing up
                    power = -power/180;
                } else {
                    //pointing down
                    power = 2-power/180;
                }
                int writeval = SERVO_PWM_MID + (int)(5*(power * (SERVO_PWM_MAX-SERVO_PWM_MIN)));
                System.out.println("Euler: "+sensor.getEuler().toString() +" into "+writeval);
                long start = System.currentTimeMillis();

                //testing write to all pins
                flaps.writeFlap(GPIO_LEFT_AILERON,writeval);
                flaps.writeFlap(GPIO_RIGHT_AILERON,writeval);
                flaps.writeFlap(GPIO_LEFT_ELEVATOR,writeval);
                flaps.writeFlap(GPIO_RIGHT_ELEVATOR,writeval);
                flaps.writeFlap(GPIO_RUDDER,writeval);

                System.out.println(System.currentTimeMillis() - start);
            }
        }

        //A super-optional feature assuming you want to actually control the damn thing. Starts a server on PORT, reading input
        void networkThread() {
            do {
                try {
                    serverSocket = new ServerSocket(PORT);
                } catch (IOException e) {
                    System.err.println("Failed at making server socket");
                    e.printStackTrace();
                }
            } while (serverSocket == null);
            System.out.println("Running server on "+serverSocket.getInetAddress().toString());
            while (keepRunningThreads) {
                //respond to input, give output
                sleep(1000);//todo remove
            }
        }
    }

    static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private GpioPinDigitalOutput getDigitalGPIO(int addr) {
        GpioPinDigitalOutput ret;
        if (pinMap.containsKey(addr)) {
            //get it from the map
            ret = pinMap.get(addr);
        } else {
            //generate it
            pinMap.put(addr,
                    ret = gpio.provisionDigitalOutputPin(
                            RaspiPin.getPinByAddress(addr),//pin number
                            ""+addr,//pin name
                            PinState.LOW//pin initial set
                    ));
            //if the program is closed, the pin will default back to LOW
            ret.setShutdownOptions(true, PinState.LOW);
        }
        return ret;
    }
}
