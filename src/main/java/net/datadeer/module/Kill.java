package net.datadeer.module;

public class Kill extends Module {
	public static final String NAME = "kill";
	public String getName() {return NAME;}
	@Override
	public void onEnable() {



		System.out.println("KILL MODULE ENABLED, DISABLING ALL MODULES");
		startDelayedKill();
		startSlowKill();
	}
	void startDelayedKill() {
		new Thread(() -> {
			System.out.println("Kill started, killing in 5 seconds if nothing else kills it");
			try {Thread.sleep(5000);} catch (InterruptedException e) {e.printStackTrace();}
			System.out.println("Nothing else killed it...");
			System.exit(100);
		}).start();
	}
	void startSlowKill() {
		try {
			getModuleGroup().disableAll();
		} catch (Exception e) {
			System.out.println("Some module gave an error");
			e.printStackTrace();
		}
		System.out.println("Kill module successfully ran");
		System.exit(0);
	}
}