package net.datadeer.module;

import net.datadeer.gpio.L298NDriver;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * {
 * moduleData: // Optional. Only works if car is in manual state
 *     ms: (positive number) // Milliseconds car will follow manual direction until stopping, unless overwritten. Required so if connection is lost it doesn't go forever
 *     left: (-100 to 100)
 *     right: (-100 to 100)
 * }
 * */
public class Automatic extends Module {

	private static final long MAX_SENSE_TIME_BEFORE_STOP_MS = 1000;

	public static final String NAME = "automatic";
	public String getName() {return NAME;}

	private L298NDriver driver;
	private ScheduledExecutorService heart;
	long timeSinceLastSense = 0;
	HC_SR04 sensor;

	public Automatic(L298NDriver driver, HC_SR04 sensor) {
		this.driver = driver;
		this.sensor = sensor;
		sensor.addListener(d -> sense(HC_SR04.scanToMeters(d)));
	}

	@Override
	public void onEnable() {
		System.out.println("Starting heartbeat");
		heart = Executors.newScheduledThreadPool(2);
		heart.scheduleAtFixedRate(this::heartbeat, 0, 25, TimeUnit.MILLISECONDS);
	}
	@Override
	public void onDisable() {
		if (heart!=null) heart.shutdown();
		if (driver!=null)driver.setLeftPowerLevel(0);
		if (driver!=null)driver.setRightPowerLevel(0);
	}

	Deque<Double> sensorList = new ArrayDeque<>();
	Deque<Long> sensorTime = new ArrayDeque<>();

	void sense(double meters) {
		timeSinceLastSense = System.currentTimeMillis();
		sensorList.addFirst(meters);
		sensorTime.addFirst(System.currentTimeMillis());
		if (sensorList.size() > 50) {
			sensorList.removeLast();
			sensorTime.removeLast();
		}
		heartbeat();
	}
	void heartbeat() {
		if (!isEnabled()) return;

		if (System.currentTimeMillis() - timeSinceLastSense > MAX_SENSE_TIME_BEFORE_STOP_MS)  {
			driver.setLeftPowerLevel(0);
			driver.setRightPowerLevel(0);
			return;
		}

		double sum = sensorList.stream().reduce(0.0, Double::sum);

		double timePerMeasurement;
		if (sensorTime.isEmpty()) {
			timePerMeasurement = 0;
		} else {
			timePerMeasurement = ((double)(sensorTime.getLast() - sensorTime.getFirst())) / sensorTime.size();
		}

		System.out.println("timePerMeasurement: "+timePerMeasurement+"ms");
		System.out.printf("Time for pool: %dms (Size:%d)\n",sensorTime.getLast()-sensorTime.getFirst(),sensorList.size());
		double average = sum / sensorList.size();
		if (average > 0.5) {
			driver.setLeftPowerLevel(100);
			driver.setRightPowerLevel(100);
		} else {
			driver.setLeftPowerLevel(100);
			driver.setRightPowerLevel(-100);
		}
	}
}