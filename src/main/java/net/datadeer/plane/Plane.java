package net.datadeer.plane;

import net.datadeer.module.BNO055;

import java.util.Scanner;

/*

UART help:
https://medium.com/@erleczarmantos/connecting-the-bosch-bno055-shuttleboard-to-the-raspberry-pi-72e8a86048d4

*/

public class Plane {


    /**TODO VITALS:
     *  Install Raspbian Buster Lite, and follow bookmarked page on making it headless safely (then rsync sshd_config and get keys)
     *
     *
     * */
    public final static String VERSION_NUMBER = "0.7.0";

    //todo add status light
    final static int GPIO_STATUS_LIGHT  = -1;
    final static int GPIO_BUZZER        = -1;//could use 21;

    final static int PORT = 51919;

    public static void main(String[] args) {
        new Plane();
    }

    //physical static stuff that should only exist once
    static volatile BNO055 sensor                   = null;
    static volatile PWMController pwmController     = null;
    static volatile MorseController morseController = null;
    static volatile PlaneController planeController = null;
    static volatile boolean keepRunningThreads      = true;

    private Plane() {
        initChildren();
        System.out.println("All initializers called.");
        //when u type end, it crashes
        Scanner s = new Scanner(System.in);
        do {
            System.out.println("type 'end' to END ME");
        } while (!s.nextLine().equals("end"));
        close();
        System.exit(0);
    }

    private void initChildren() {
        //main start
        System.out.println("Running version " + VERSION_NUMBER);
        new Thread(new StatusLight()).start();
        //testing without sensor plugged in
        //sensor = new BNO055();
        //testing without morseController plugged in
        //morseController = new MorseController(GPIO_BUZZER, 1000/5);
        pwmController = new PWMController();
        //testing without planeController (todo set up laptop GUI)
//        planeController = new PlaneController();
    }

    private void close() {
        System.out.println("ENDING; THIS BETTER BE ON THE GROUND");
        System.out.println("\tStopping threads");
        keepRunningThreads = false;
        sleep(500);
        System.out.println("\tClosing classes");
        //main done flying, deconstruct all
        if (sensor!=null) sensor.disable();
        if (pwmController!=null) pwmController.close();
        if (morseController!=null) morseController.close();
        sleep(500);
        System.out.println("Done... Exiting...");
    }

    static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}