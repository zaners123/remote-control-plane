package net.datadeer.Car;

import net.datadeer.common.Common;
import net.datadeer.gpio.L298NDriver;
import net.datadeer.module.*;

public class Car {
	public static final boolean DEBUG = true;
	private static CarServer server;
	private L298NDriver l298NDriver;
	private HC_SR04 hc_sr04;

	public static void main(String[] args) {
		new Car();
	}

	Car() {
		ModuleGroup group = new ModuleGroup();
		System.out.println("Version 1.3");

		System.out.println("Adding modules");

		l298NDriver = new L298NDriver(25,27,28,29,26,23,true,false);
		hc_sr04 = new HC_SR04(21,22);
		server = new CarServer();

		group.addModule(hc_sr04);
		group.getModule(hc_sr04.getName()).enable();
		group.addModule(new ModuleManual(l298NDriver));
		group.addModule(new Automatic(l298NDriver,hc_sr04));
		group.addModule(new Kill());
		group.addModule(new Horn(-1));

		System.out.println("Initializing driver");
		System.out.println("Initializing JSON Server");
		System.out.println("JSON server initialized");
	}

	public void done() {
		if (l298NDriver != null) l298NDriver.stop();
		Common.sleep(100);
		System.exit(0);
	}
}
