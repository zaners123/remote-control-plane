package net.datadeer.module;

import org.json.JSONObject;

public abstract class Module {

	protected boolean enabled = false;
	protected ModuleGroup group = null;

	public abstract String getName();

	/**
	 * Commonly used by clients to send/receive data to a module
	 * @param request The request sent, ex: to move the tires
	 * @return The response to the client. Examples:
	 *    - status such as that the operation was successful
	 *    - info such as the temperature, speed, etc
	 * */
	public JSONObject moduleInput(JSONObject request) {
		return null;
	}

	public final void enable() {
		System.out.println("Module "+this.getName()+" Enabled");
		enabled = true;
		onEnable();
	}

	public final void disable() {
		System.out.println("Module "+this.getName()+" Disabled");
		enabled = false;
		onDisable();
	}

	//hooks
	protected void onEnable() {};
	protected void onDisable() {};

	public final void setModuleGroup(ModuleGroup group) {
		if (group==null) throw new NullPointerException();
		this.group = group;
	}
	public final ModuleGroup getModuleGroup() {return group;}

	public final boolean isEnabled() {
		return enabled;
	}
}
