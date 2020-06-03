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

public class Manual implements CarModule {

	ScheduledExecutorService heart;

	@Override
	public JSONObject moduleInput(JSONObject data) {
		JSONObject ret = new JSONObject();
		if (data.has("ms")) {
			//todo have it stop driving when the countdown hits, using something like a clock and if(timeStarted+ms<system.curtimems) stop
			int ms = data.getInt("ms");
			if (data.has("left")) {
				Car.l298NDriver.setLeftPowerLevel(ms==0?0:data.getInt("left"));
				ret.put("status",202);
			}
			if (data.has("right")) {
				Car.l298NDriver.setRightPowerLevel(ms==0?0:data.getInt("right"));
				ret.put("status",202);
			}
		}
		return ret;
	}

	@Override public void enable() {
		System.out.println("Starting heartbeat");
		heart = Executors.newScheduledThreadPool(2);
		heart.scheduleAtFixedRate(this::heartbeat, 0, 50, TimeUnit.MILLISECONDS);
	}

	@Override
	public void disable() {
		heart.shutdown();
		Car.l298NDriver.setLeftPowerLevel(0);
		Car.l298NDriver.setRightPowerLevel(0);
	}

	void heartbeat() {

	}


}
