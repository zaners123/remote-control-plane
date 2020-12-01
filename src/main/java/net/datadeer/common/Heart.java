package net.datadeer.common;

public class Heart {
	final Runnable runnable;
	boolean running;
	Thread pacemakerThread;
	final long msDelay;

	public Heart(Runnable runnable) {
		this(runnable, 0);
	}
	public Heart(Runnable runnable, long msDelay) {
		if (msDelay < 10) throw new IllegalArgumentException("Heart should not be called with ms<10");
		this.runnable = runnable;
		this.msDelay = msDelay;
	}

	public void start() {
		//already running
		if (running) return;
		//run it
		running = true;
		pacemakerThread = new Thread(() -> {
			while (running) {
				new Thread(runnable).start();
				Common.sleep(msDelay);
			}
		});
		pacemakerThread.start();
	}

	public void stop () {
		running = false;
	}

}
