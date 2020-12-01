package net.datadeer.common;

public class Common {
	public static void sleep(long ms) {
		if (ms <= 0) return;
		try {Thread.sleep(ms);} catch (InterruptedException e) {e.printStackTrace();}
	}
}
