package net.datadeer.module;

import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import net.datadeer.common.Heart;
import net.datadeer.gpio.PinController;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * A "hardware driver"/encapsulator for a single Ultrasonic Sensor
 * */
public class HC_SR04 extends Module {

	public String getName() {return "ultrasonic";}

	GpioPinDigitalOutput trigger;
	GpioPinDigitalInput echo;
	boolean shouldScan = false;
	GpioPinListenerDigital echoListener;
	net.datadeer.common.Heart heart;

	private final static ArrayList<DepthListener> externalListeners = new ArrayList<>();

	void beat() {
//		System.out.println("HC_SR04 Sensor Beat");
		trigger.pulse(10, PinState.HIGH, TimeUnit.MICROSECONDS);
//		Common.sleep(5000);
	}

	public HC_SR04(int trigger, int echo) {
		heart = new Heart(this::beat,50);//at least 20
		System.out.println("ULTRASONIC SENSOR GENERATED");
		this.trigger = PinController.getDigitalGPIOOutput(trigger);
		this.echo = PinController.getDigitalGPIOInput(echo);
		echoListener = new GpioPinListenerDigital() {
			long heldSince = System.nanoTime();
			long timeHeld = 0;
			@Override
			public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
				//get time first
				long now = System.nanoTime();

				boolean signalStarted = event.getState().isHigh();

				if (signalStarted) {
					heldSince = now;
				} else {
					timeHeld = now - heldSince;
					if (shouldScan) {
						for (DepthListener dl : externalListeners) {
							dl.timeOn(timeHeld);
						}
					}
				}
			}
		};

		//testing accuracy
		addListener((d)-> System.out.printf("Meters away %f, Nanoseconds passed: %d\n",scanToMeters(d),d));

	}

	interface DepthListener {
		void timeOn(long d);
	}

	public static double scanToMeters(long timeHeld) {
		//times speed of sound divided by (had to go there and back so 2) divided by (nanosecond to second)
		return ((double)(timeHeld * 343))/2_000_000_000;
	}

	void addListener(DepthListener l) {if (l!=null) externalListeners.add(l);}
	void removeListener(DepthListener l) {if (l!=null) externalListeners.remove(l);}

	@Override public void onEnable() {
		System.out.println("ULTRASONIC SENSOR ENABLED");
		heart.start();
		shouldScan = true;
		echo.addListener(echoListener);
	}
	@Override public void onDisable() {
		heart.stop();
		shouldScan = false;
		echo.removeListener(echoListener);
	}
}
