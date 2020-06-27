package Car.module;

import Car.CarModule;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

import java.util.ArrayList;

/**
 * Used to be a "hardware driver"/encapsulator for a single Ultrasonic Sensor
 * */
public class HC_SR04 extends CarModule {

	public static String getName() {return "ultrasonic";}

	GpioPinDigitalOutput trigger;
	GpioPinDigitalInput echo;
	boolean shouldScan = false;
	GpioPinListenerDigital echoListener;

	private final static ArrayList<DepthListener> externalListeners = new ArrayList<>();

	public HC_SR04(int trigger, int echo) {
		System.out.println("ULTRASONIC SENSOR GENERATED");
		this.trigger = gpio.Controller.getDigitalGPIOOutput(trigger);
		this.echo = gpio.Controller.getDigitalGPIOInput(echo);
		echoListener = new GpioPinListenerDigital() {
			long heldSince = System.nanoTime();
			long timeHeld = 0;
			@Override
			public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
				//first thing done, for whatever minimal accuracy it will give
				long now = System.nanoTime();
				boolean signalStarted = event.getState().isLow();

				if (signalStarted) {
					heldSince = now;
				} else {
					if (shouldScan) for (DepthListener dl : externalListeners) {
						dl.timeOn(timeHeld);
					}
				}
			}
		};
		//testing
		addListener((d)-> System.out.println("METERS AWAY: "+d));
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

	@Override public void enable() {
		System.out.println("ULTRASONIC SENSOR ENABLED");
		super.enable();
		shouldScan = true;
		echo.addListener(echoListener);
	}
	@Override public void disable() {
		super.disable();
		shouldScan = false;
		echo.removeListener(echoListener);
	}
}
