package net.datadeer.common;

public class Heart {

	final Runnable runnable;
	boolean running;
	final long msDelay;

	public Heart(Runnable runnable) {
		this(runnable, 0);
	}
	public Heart(Runnable runnable, long msDelay) {
		this.runnable = runnable;
		this.msDelay = msDelay;
	}

	public void start() {
		//already running
		if (running) return;
		//run it
		running = true;
		new Thread(() -> {
			while (running) {
				new Thread(runnable).start();
				if (msDelay>0) Common.sleep(msDelay);
			}
		}).start();
	}

	public void stop () {
		running = false;
	}

}
