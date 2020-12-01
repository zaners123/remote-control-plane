package net.datadeer.module;

import net.datadeer.gpio.L298NDriver;
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
public class ModuleManual extends Module {

	private ScheduledExecutorService heart;
	private long stopLeftAt = 0;
	private long stopRightAt = 0;
	private L298NDriver driver;

	public ModuleManual(L298NDriver driver) {
		this.driver = driver;
	}

	public static final String NAME = "manual";
	public String getName() {return NAME;}

	@Override
	public JSONObject moduleInput(JSONObject request) {
		JSONObject ret = new JSONObject();
		//have it stop driving when the countdown hits, using something like a clock and if(timeStarted+ms<system.curtimems) stop
		if (request.has("ms") && request.has("left") && request.has("right")) {
			int leftPower = request.getInt("left");
			int rightPower = request.getInt("right");

			int ms = request.getInt("ms");
			driver.setLeftPowerLevel(leftPower);
			stopLeftAt = System.currentTimeMillis() + ms;
			ret.put("left", leftPower);

			driver.setRightPowerLevel(rightPower);
			stopRightAt = System.currentTimeMillis() + ms;
			ret.put("right", rightPower);
		}
		heartbeat();
		return ret;
	}

	@Override
	public void onEnable() {
		System.out.println("Starting heartbeat");
		heart = Executors.newScheduledThreadPool(2);
		heart.scheduleAtFixedRate(this::heartbeat, 0, 25, TimeUnit.MILLISECONDS);
	}
	@Override
	public void onDisable() {
		if (heart!=null) {
			heart.shutdown();
		}
	}
	void heartbeat() {
		if (stopLeftAt!=0 && System.currentTimeMillis() >= stopLeftAt) {
			driver.setLeftPowerLevel(0);
			stopLeftAt = 0;
		}
		if (stopRightAt!=0 && System.currentTimeMillis() >= stopRightAt) {
			driver.setRightPowerLevel(0);
			stopRightAt = 0;
		}
	}
}