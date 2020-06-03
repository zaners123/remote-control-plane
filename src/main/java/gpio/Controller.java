package gpio;

import com.pi4j.io.gpio.*;
import java.util.HashMap;

public class Controller {
	private final static GpioController gpio = GpioFactory.getInstance();
	private static HashMap<Integer, GpioPinDigitalOutput> pinMap = new HashMap<>();
	public static GpioPinDigitalOutput getDigitalGPIO(int addr) {
		GpioPinDigitalOutput ret;
		if (pinMap.containsKey(addr)) {
			//get it from the map
			ret = pinMap.get(addr);
		} else {
			//generate it
			pinMap.put(addr,
					ret = gpio.provisionDigitalOutputPin(
							RaspiPin.getPinByAddress(addr),//pin number
							""+addr,//pin name
							PinState.LOW//pin initial set
					));
			//if the program is closed, the pin will default back to LOW
			ret.setShutdownOptions(true, PinState.LOW);
		}
		return ret;
	}
}
