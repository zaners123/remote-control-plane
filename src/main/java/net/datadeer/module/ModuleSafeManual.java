package net.datadeer.module;

import net.datadeer.gpio.L298NDriver;
import org.json.JSONObject;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * The same as ModuleManual but stops if there's something infront of it
 * */
public class ModuleSafeManual extends Module implements HC_SR04.DepthListener {

	private ScheduledExecutorService heart;
	private long stopAt = 0;
	private L298NDriver driver;
	private HC_SR04 hc_sr04;

	double metersOfSpaceInfront = 0;
	long lastScan = 0;

	public ModuleSafeManual(L298NDriver driver, HC_SR04 hc_sr04) {
		this.driver = driver;
		this.hc_sr04 = hc_sr04;
		hc_sr04.addListener(this);
	}

	public static final String NAME = "SafeManual";
	public String getName() {return NAME;}

	/**
	 * {
	 * moduleData: // Optional. Only works if car is in manual state
	 *     ms: (positive number) // Milliseconds car will follow manual direction until stopping, unless overwritten. Required so if connection is lost it doesn't go forever
	 *     left: (-100 to 100)
	 *     right: (-100 to 100)
	 * }
	 * */
	@Override
	public JSONObject moduleInput(JSONObject request) {
		JSONObject ret = new JSONObject();
		if (request.has("ms") && request.has("left") && request.has("right")) {
			int leftPower = request.getInt("left");
			int rightPower = request.getInt("right");
			int ms = request.getInt("ms");

			long lastScanAgo = System.currentTimeMillis() - lastScan;

			//if there could be an obstruction, stop
			if (lastScanAgo > 100 || metersOfSpaceInfront < .3f) {
				leftPower = Math.min(0,leftPower);
				rightPower = Math.min(0,rightPower);
				ms = 501;
			}
			stopAt = System.currentTimeMillis() + ms;
			driver.setPowerLevels(leftPower, rightPower);
			ret.put("left", leftPower);
			ret.put("right", rightPower);
			ret.put("distance",metersOfSpaceInfront);
			ret.put("lastScanAgo",lastScanAgo);
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
		if (hc_sr04 != null) {
			hc_sr04.removeListener(this);
		}
		heart.shutdown();
	}
	void heartbeat() {
		if (stopAt != 0 && System.currentTimeMillis() >= stopAt) {
			driver.setPowerLevels(0,0);
			stopAt = 0;
		}
	}

	@Override
	public void timeOn(long d) {
		metersOfSpaceInfront = HC_SR04.scanToMeters(d);
		lastScan = System.currentTimeMillis();
	}
}