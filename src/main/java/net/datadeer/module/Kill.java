package net.datadeer.module;

public class Kill extends Module {

	public static final String NAME = "kill";
	public String getName() {return NAME;}

	@Override
	public void onEnable() {
		getModuleGroup().disableAll();
	}

}
