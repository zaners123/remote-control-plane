package net.datadeer.CarClient;

import net.datadeer.Car.CarServer;
import net.datadeer.module.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.font.TextLayout;
import java.io.IOException;
import java.net.*;
import java.util.TreeSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

//todo new InetSocketAddress("127.0.0.1", port) somethin like that
//todo basic visuals
//todo maybe some sort of web view for camera???

public class CarClient {

	private static final String ip = "192.168.0.252";

	private static boolean running = true;
	private InetSocketAddress SEND_ADDR = new InetSocketAddress(ip, CarServer.PORT);
//	private static final JSONObject KILL_OBJECT = new JSONObject().put("kill","kill");
	private int manualPowerLevel = 100;
	private final Font twentyFont = new Font("Default", Font.BOLD, 20);

	TreeSet<String> modulesEnabled = new TreeSet<>();

	//ui
	Panel panel;
	//to send/recieve stuff to car
	static DatagramSocket sock;
	//map of pressed keys
	TreeSet<Integer> heldKeys = new TreeSet<>();
	//for heartbeat
	ScheduledExecutorService heart;


	void done() {
		heart.shutdown();
	}

	void heartbeat() {
		JSONObject toSend = new JSONObject()
				.put("modules", new JSONArray(modulesEnabled.toArray()));

		//manual module
		int left = 0;
		int right = 0;
		if (heldKeys.contains(KeyEvent.VK_W)) {
			left=manualPowerLevel;
			right=manualPowerLevel;
		} else if (heldKeys.contains(KeyEvent.VK_S)) {
			left=-manualPowerLevel;
			right=-manualPowerLevel;
		} else if (heldKeys.contains(KeyEvent.VK_A)) {
			left=-manualPowerLevel;
			right=manualPowerLevel;
		} else if (heldKeys.contains(KeyEvent.VK_D)) {
			left=manualPowerLevel;
			right=-manualPowerLevel;
		}
		if (modulesEnabled.contains(ModuleManual.NAME)) {
			toSend = toSend.put(ModuleManual.NAME,new JSONObject()
					.put("left",left)
					.put("right",right)
					.put("ms",100* ModuleGroup.TIME_MULTIPLIER)
			);
		}
		if (modulesEnabled.contains(ModuleSafeManual.NAME)) {
			toSend = toSend.put(ModuleSafeManual.NAME, new JSONObject()
					.put("left", left)
					.put("right", right)
					.put("ms", 100 * ModuleGroup.TIME_MULTIPLIER)
			);
		}
		JSONObject returned = sendRequest(toSend);
	}

	class Panel extends JPanel {
		Graphics2D c;
		@Override
		public void paintComponent(Graphics comp) {
			c = (Graphics2D)comp;
			c.setFont(twentyFont);
			c.drawString("Type WASD",0,50);
		}
	}

	CarClient() {
		modulesEnabled.add(CarServer.NAME);
		modulesEnabled.add(HC_SR04.NAME);
		try {
			sock = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
			System.err.println("SOCK FAILED");
			System.exit(1);
		}
		heart = Executors.newScheduledThreadPool(2);
		heart.scheduleAtFixedRate(this::heartbeat, 0, 25* ModuleGroup.TIME_MULTIPLIER, TimeUnit.MILLISECONDS);
		panel = new Panel();
		JFrame disp = new JFrame("Plane Client (This is Laptop)");
//		disp.setLayout(new BoxLayout(disp.getContentPane(), BoxLayout.PAGE_AXIS));
		disp.setSize(1300, 700);
		disp.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent keyEvent) {}

			@Override
			public void keyPressed(KeyEvent keyEvent) {
				keyUsed(keyEvent, true);
			}

			@Override
			public void keyReleased(KeyEvent keyEvent) {
				keyUsed(keyEvent, false);
			}
		});
		disp.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
//		JPanel boxLayoutPanel = new JPanel();
//		disp.add(new JButton("Use M to enage/disengage manual (allows WASD)"));
//		disp.add(new JButton("Use WASD to move/rotate robot"));
//		disp.add(new JButton("Type 0-9 to set top speed"));
		disp.add(panel);
//		disp.getContentPane().add(boxLayoutPanel, BorderLayout.CENTER);
		disp.setVisible(true);
	}

	public static void main(String[] args) {
		System.out.println("Plane Client Started");
		new CarClient();
	}

	/**
	 * @param request - The JSON request to be sent, formatted according to the CarController protocol
	 * @return response from car
	 * */
	JSONObject sendRequest(JSONObject request) {
		byte[] requestData = request.toString().getBytes();
		if (requestData.length> 1024) {
			System.err.println("AH HECK THIS PACKET MIGHT BE TOO LARGE FOR UDP");
		}
		//todo should reuse socket
		System.out.println("TX:"+request.toString());
		DatagramPacket packet = new DatagramPacket(requestData,requestData.length, SEND_ADDR);
		try {
			sock.setSoTimeout(1000);
			sock.send(packet);
			sock.receive(packet);

			byte[] data = packet.getData();
			String response = new String(data, 0, Math.min(data.length,packet.getLength()));
			System.out.println("RX:"+response);
			try {
				return new JSONObject(response);
			} catch (JSONException e) {
				return null;
			}
		} catch (SocketTimeoutException timeout) {
			System.err.println("UDP timeout");
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Couldn't send UDP");
		}
		return null;
	}

	private void sleep(int ms) {
		try {Thread.sleep(ms);} catch (InterruptedException e) {e.printStackTrace();}
	}

	void keyUsed (KeyEvent event, boolean pressed) {
		int id = event.getKeyCode();
		//already held
		if (pressed && heldKeys.contains(id)) return;
		if (pressed) {
			heldKeys.add(id);
		} else {
			heldKeys.remove(id);
		}
		//control keys
		switch (id) {
			//speed settings
			case KeyEvent.VK_0:manualPowerLevel=0;  return;
			case KeyEvent.VK_1:manualPowerLevel=11; return;
			case KeyEvent.VK_2:manualPowerLevel=22; return;
			case KeyEvent.VK_3:manualPowerLevel=33; return;
			case KeyEvent.VK_4:manualPowerLevel=44; return;
			case KeyEvent.VK_5:manualPowerLevel=55; return;
			case KeyEvent.VK_6:manualPowerLevel=66; return;
			case KeyEvent.VK_7:manualPowerLevel=77; return;
			case KeyEvent.VK_8:manualPowerLevel=88; return;
			case KeyEvent.VK_9:manualPowerLevel=100;return;
			case KeyEvent.VK_M://manual controls
				if (event.isShiftDown()) {
					modulesEnabled.remove(ModuleManual.NAME);
				} else {
					modulesEnabled.add(ModuleManual.NAME);
				}
				break;
			case KeyEvent.VK_B://safe manual controls
				if (event.isShiftDown()){
					modulesEnabled.remove(ModuleSafeManual.NAME);
				} else {
					modulesEnabled.add(ModuleSafeManual.NAME);
				}
				break;
			case KeyEvent.VK_N://automatic controls
				if (event.isShiftDown()) {
					modulesEnabled.remove(Automatic.NAME);
				} else {
					modulesEnabled.add(Automatic.NAME);
				}
				break;
			case KeyEvent.VK_H://horn
				if (pressed) {
					modulesEnabled.add(Horn.NAME);
				} else {
					modulesEnabled.remove(Horn.NAME);
				}
				break;
			case KeyEvent.VK_K:modulesEnabled.add(Kill.NAME);break;
		}
		//update from key
		new Thread(this::heartbeat).start();
	}
}