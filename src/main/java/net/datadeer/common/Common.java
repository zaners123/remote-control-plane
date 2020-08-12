package net.datadeer.common;

public class Common {
	public static void sleep(long ms) {
		try {Thread.sleep(ms);} catch (InterruptedException e) {e.printStackTrace();}
	}
}
