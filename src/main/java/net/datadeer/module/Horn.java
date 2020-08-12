package net.datadeer.module;

import com.pi4j.io.gpio.GpioPinDigitalOutput;
import net.datadeer.gpio.PinController;
import org.json.JSONObject;

/**
 * A CarModule that honks the entire time it's enabled
 * */
public class Horn extends Module {

	public static final String NAME = "horn";
	public String getName() {return NAME;}

	GpioPinDigitalOutput pin;

	//todo have constructor require pin
	public Horn(int pinLoc) {
		this.pin = PinController.getDigitalGPIOOutput(pinLoc);
	}

	@Override
	public JSONObject moduleInput(JSONObject request) {
		System.out.println("HONK RECEIVED INPUT "+ request.toString());


		//		pin.high();
		//todo PWM stuff maybe


		return null;
	}

	@Override
	public void onEnable() {
		System.out.println("Honk module enabled");
	}

	@Override
	public void onDisable() {
		System.out.println("Honk Module Disabled");
	}
}
