package Car.module;

import Car.CarModule;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

/**
 * A CarModule that honks the entire time it's enabled
 * */
public class Horn extends CarModule {

	public static String getName() {return "honk";}

	GpioPinDigitalOutput pin;

	//todo have constructor require pin
	public Horn(int pinLoc) {
		this.pin = gpio.Controller.getDigitalGPIOOutput(pinLoc);
	}

	@Override
	public JSONObject moduleInput(JSONObject data) {
		return null;
	}

	@Override
	public void enable() {
		super.enable();
		//todo PWM stuff maybe
		System.out.println("PINHIGH blink");
		//todo start honking
	}

	@Override
	public void disable() {
		super.disable();
		pin.high();
		System.out.println("PINHIGH");
		//todo stop honking
	}
}
