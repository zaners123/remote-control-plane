package net.datadeer.module;

import java.util.*;
import java.util.stream.Stream;

/**
 * This is CARLOS (net.datadeer.Car Lightweight Operating System)
 *
 * It contains a modular hardware control system
 *
 *
 * */
public class ModuleGroup {

	//lower to send more packets and to stop the car quicker on lost connection
	public static final int TIME_MULTIPLIER = 5;

	private final TreeMap<String, Module> modules = new TreeMap<>();

	public Set<String> getModulesKeyset() {return modules.keySet();}

	public Stream<String> getEnabledModules() {
		return modules.values().parallelStream().filter(Module::isEnabled).map(Module::getName);
	}

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
