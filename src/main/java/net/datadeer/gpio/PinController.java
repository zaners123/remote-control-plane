package net.datadeer.gpio;

import com.pi4j.io.gpio.*;
import java.util.HashMap;

public class PinController {
	private final static GpioController gpio = GpioFactory.getInstance();

	private static final HashMap<Integer, GpioPinDigitalOutput> pinMapOutput = new HashMap<>();
	public static GpioPinDigitalOutput getDigitalGPIOOutput(int addr) {
		if (addr==-1) return null;
		GpioPinDigitalOutput ret;
		if (pinMapOutput.containsKey(addr)) {
			//get it from the map
			ret = pinMapOutput.get(addr);
		} else {
			//generate it
			pinMapOutput.put(addr,
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

	private static final HashMap<Integer, GpioPinDigitalInput> pinMapInput = new HashMap<>();
	public static GpioPinDigitalInput getDigitalGPIOInput(int addr) {
		if (addr==-1) return null;
		GpioPinDigitalInput ret;
		if (pinMapInput.containsKey(addr)) {
			//get it from the map
			ret = pinMapInput.get(addr);
		} else {
			//generate it
			pinMapInput.put(addr,
					ret = gpio.provisionDigitalInputPin(
							RaspiPin.getPinByAddress(addr),//pin number
							""+addr,//pin name
							PinPullResistance.PULL_DOWN));
			//if the program is closed, the pin will default back to LOW
			ret.setShutdownOptions(true, PinState.LOW);
		}
		return ret;
	}
}
