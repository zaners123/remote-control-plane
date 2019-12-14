package plane;

import com.pi4j.io.gpio.*;

import java.util.HashMap;
import java.util.Scanner;

/*

UART help:
https://medium.com/@erleczarmantos/connecting-the-bosch-bno055-shuttleboard-to-the-raspberry-pi-72e8a86048d4

*/

public class Plane {

    public final static String VERSION_NUMBER = "0.6.1";

    final static int GPIO_STATUS_LIGHT   = 0;

    final static GpioController gpio = GpioFactory.getInstance();
    static HashMap<Integer, GpioPinDigitalOutput> pinMap = new HashMap<>();

    final static int PORT = 51919;

    public static void main(String[] args) {
        new Plane();
    }

    //physical static stuff that should only exist once
    volatile static BNO055 sensor                   = null;
    volatile static FlapController flapController   = null;
    private static volatile StatusLight statusLight = null;
    static volatile PropController propController   = null;
    volatile static boolean keepRunningThreads      = true;

    private Plane() {
        //main start
        System.out.println("Running version " + VERSION_NUMBER);
        new Thread(statusLight = new StatusLight()).start();
        sensor = new BNO055();
        //flapController = new FlapController();
        propController = new PropController();
        System.out.println("All initializers called.");

        //when u type end, it crashes
        Scanner s = new Scanner(System.in);
        do {
            System.out.println("type 'end' to end");
        } while (!s.nextLine().equals("end"));

        System.out.println("ENDING; THIS BETTER BE ON THE GROUND");
        System.out.println("\tStopping threads");
        keepRunningThreads = false;
        sleep(500);
        System.out.println("\tClosing classes");
        //main done flying, deconstruct all
        sensor.close();
        flapController.close();
        sleep(500);
        System.out.println("Done... Exiting...");
        System.exit(0);
    }

    static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static GpioPinDigitalOutput getDigitalGPIO(int addr) {
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