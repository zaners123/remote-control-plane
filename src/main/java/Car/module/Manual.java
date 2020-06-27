package Car.module;

import Car.Car;
import Car.CarModule;
import org.json.JSONObject;
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
 *
 * */
public class Manual extends CarModule {

	ScheduledExecutorService heart;
	long stopLeftAt = 0;
	long stopRightAt = 0;

	public static String getName() {return "manual";}

	@Override
	public JSONObject moduleInput(JSONObject data) {
		JSONObject ret = new JSONObject();
		//have it stop driving when the countdown hits, using something like a clock and if(timeStarted+ms<system.curtimems) stop
		if (data.has("ms")) {
			int ms = data.getInt("ms");
			if (data.has("left")) {
				Car.l298NDriver.setLeftPowerLevel(data.getInt("left"));
				stopLeftAt = System.currentTimeMillis() + ms;
				ret.put("left",data.getInt("left"));
			}
			if (data.has("right")) {
				Car.l298NDriver.setRightPowerLevel(data.getInt("right"));
				stopRightAt = System.currentTimeMillis() + ms;
				ret.put("right",data.getInt("right"));
			}
		}
		return ret;
	}
	@Override
	public void enable() {
		super.enable();
		System.out.println("Starting heartbeat");
		heart = Executors.newScheduledThreadPool(2);
		heart.scheduleAtFixedRate(this::heartbeat, 0, 25, TimeUnit.MILLISECONDS);
	}
	@Override
	public void disable() {
		super.disable();
		heart.shutdown();
		Car.l298NDriver.setLeftPowerLevel(0);
		Car.l298NDriver.setRightPowerLevel(0);
	}
	void heartbeat() {
		if (stopLeftAt!=0 && System.currentTimeMillis() >= stopLeftAt) {
			Car.l298NDriver.setLeftPowerLevel(0);
			stopLeftAt = 0;
		}
		if (stopRightAt!=0 && System.currentTimeMillis() >= stopRightAt) {
			Car.l298NDriver.setRightPowerLevel(0);
			stopRightAt = 0;
		}
	}
}