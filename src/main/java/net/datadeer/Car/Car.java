package net.datadeer.Car;

import com.sun.org.apache.xpath.internal.operations.Mod;
import net.datadeer.common.Common;
import net.datadeer.gpio.L298NDriver;
import net.datadeer.module.*;

public class Car {
	public static final boolean DEBUG = false;

	public static void main(String[] args) {
		new Car();
	}

	Car() {
		ModuleGroup group = new ModuleGroup();
		System.out.println("Version 1.5 - now with Modules!");

		CarServer server = new CarServer();
		L298NDriver l298NDriver = new L298NDriver(25,27,28,29,26,23,true,false);
		HC_SR04 hc_sr04 = new HC_SR04(21, 22);

		group.addModule(hc_sr04);
		group.addModule(new ModuleManual(l298NDriver));
		group.addModule(new ModuleSafeManual(l298NDriver, hc_sr04));
		group.addModule(new Automatic(l298NDriver, hc_sr04));
		group.addModule(new Kill());
		group.addModule(new Horn(-1));
		group.addModule(server);

		//modules to run on startup
		group.getModule(server.getName()).enable();
		group.getModule(hc_sr04.getName()).enable();
//		group.getModule(Automatic.NAME).enable();
		group.addModule(new Module() {
			@Override public String getName() {return "L298N Driver Stopper";}
			@Override protected void onDisable() {if (l298NDriver != null) l298NDriver.stop();}
		});
	}
}
