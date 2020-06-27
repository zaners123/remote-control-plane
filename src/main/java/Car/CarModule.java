package Car;

import org.json.JSONObject;

public abstract class CarModule {

	protected boolean enabled = false;

	public static String getName() {
		return "unnamed";
	}

	public JSONObject moduleInput(JSONObject data) {
		return null;
	}

	public void enable() {
		System.out.println("Module "+getName()+" Enabled");
		enabled = true;
	}

	public void disable() {
		System.out.println("Module "+getName()+" Disabled");
		enabled = false;
	}

	public boolean isEnabled() {
		return enabled;
	}
}
