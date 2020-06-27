package Car.module;

import Car.CarModule;

public class Kill extends CarModule {

	public static String getName() {return "kill";}

	@Override
	public void enable() {
		super.enable();
		Car.Car.done();
	}

	@Override
	public void disable() {
		super.disable();
	}
}
