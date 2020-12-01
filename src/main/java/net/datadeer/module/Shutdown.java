package net.datadeer.module;

public class Shutdown extends Module {
	public static final String NAME = "shutdown";
	public String getName() {return NAME;}
	@Override
	public void onEnable() {
		System.out.println("Shutdown Module Enabled");
		//todo shutdown module, somethin like shift+K.
//		System.console().writer().println("shutdown +0");
	}
}