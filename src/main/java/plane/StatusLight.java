package plane;

import com.pi4j.io.gpio.GpioPinDigitalOutput;

class StatusLight implements Runnable {
    public void run() {
        GpioPinDigitalOutput statusLight = Plane.getDigitalGPIO(Plane.GPIO_STATUS_LIGHT);
        while (Plane.keepRunningThreads) {
            statusLight.high();
            Plane.sleep(250);
            statusLight.low();
            Plane.sleep(250);
        }
    }
}