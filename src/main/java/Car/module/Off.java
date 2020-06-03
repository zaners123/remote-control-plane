package Car.module;

import Car.CarModule;
import org.json.JSONObject;

/**A simple module that does nothing*/
public class Off implements CarModule {
	@Override public JSONObject moduleInput(JSONObject data) {return null;}
	@Override public void enable() {}
	@Override public void disable() {}
}
