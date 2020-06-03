package Car;

import org.json.JSONObject;

public interface CarModule {
	JSONObject moduleInput(JSONObject data);
	void enable();
	void disable();
}
