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

	public static final String NAME = "ultrasonic";
	GpioPinDigitalOutput trigger;
	GpioPinDigitalInput echo;
	boolean shouldScan = false;
	GpioPinListenerDigital echoListener;
	net.datadeer.common.Heart heart;
	private final static ArrayList<DepthListener> externalListeners = new ArrayList<>();

	public String getName() {return NAME;}

	void beat() {
		try {
			trigger.pulse(10, PinState.HIGH, TimeUnit.MICROSECONDS);
		} catch (RuntimeException e) {
			System.out.println("Trigger throwin runtimes again");
		}
	}

	/**
	 * This makes sure the sensor is only on when it is needed.
	 * */
	private void onUpdateNumberOfListeners() {
		if (externalListeners.isEmpty()) {
			echo.removeListener(echoListener);
			heart.stop();
		} else {
			echo.addListener(echoListener);
			heart.start();
		}
	}

	public HC_SR04(int trigger, int echo) {
		heart = new Heart(this::beat,1000/25);
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
//		addListener((d)-> System.out.printf("Meters away %f, Nanoseconds passed: %d\n",scanToMeters(d),d));
	}

	interface DepthListener {
		void timeOn(long d);
	}

	public static double scanToMeters(long timeHeld) {
		//times speed of sound divided by (had to go there and back so 2) divided by (nanosecond to second)
		double meters = ((double)(timeHeld * 343))/2_000_000_000;
		//scanner seems to always be at at least .1 even with something immediately in front of it
		meters -= 0.1;
		return meters;
	}

	void addListener(DepthListener l) {
		if (l!=null) externalListeners.add(l);
		onUpdateNumberOfListeners();
	}
	void removeListener(DepthListener l) {
		if (l!=null) externalListeners.remove(l);
		onUpdateNumberOfListeners();
	}

	@Override public void onEnable() {
		System.out.println("ULTRASONIC SENSOR ENABLED");
		heart.start();
		shouldScan = true;
	}
	@Override public void onDisable() {
		heart.stop();
		shouldScan = false;
	}
}
