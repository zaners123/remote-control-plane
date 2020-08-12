package net.datadeer.module;

import java.util.*;

/**
 * This is CARLOS (net.datadeer.Car Lightweight Operating System)
 *
 * It contains a modular hardware control system
 *
 *
 * */
public class ModuleGroup {

	ModuleGroup car;
	/*public static ModuleGroup getSingleton() {
		synchronized (car) {
			if (car==null) {
				synchronized (car) {
					if (car != null) return car;
					car = new ModuleGroup();
				}
			}
			return car;
		}
	}*/

	//lower to send more packets and to stop the car quicker on lost connection
	public static final int TIME_MULTIPLIER = 5;

	private final TreeMap<String, Module> modules = new TreeMap<>();

	public Set<String> getModulesKeyset() {return modules.keySet();}

	public Module getModule(String name) {return modules.get(name);}

	public ModuleGroup() {}

	public void addModule(Module module) {
		if (module==null) throw new NullPointerException();
		module.setModuleGroup(this);
		modules.put(module.getName(),module);
	}
	public void disableAll() {
		modules.forEach((key, m)-> m.disable());
	}
}
