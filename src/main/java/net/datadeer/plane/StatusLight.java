package net.datadeer.plane;

import com.pi4j.io.gpio.GpioPinDigitalOutput;
import net.datadeer.gpio.PinController;

class StatusLight implements Runnable {
    public void run() {
        if (Plane.GPIO_STATUS_LIGHT < 0) return;
        GpioPinDigitalOutput statusLight = PinController.getDigitalGPIOOutput(Plane.GPIO_STATUS_LIGHT);
        while (Plane.keepRunningThreads) {
            statusLight.high();
            Plane.sleep(250);
            statusLight.low();
            Plane.sleep(250);
        }
    }
}