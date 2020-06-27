package CarClient;

import Car.module.Horn;
import Car.module.Kill;
import Car.module.Manual;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
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

	private static boolean running = true;
	private static final String ip = "192.168.0.252";
	private static final int port = 50303;
	private InetSocketAddress SEND_ADDR = new InetSocketAddress(ip, port);
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
		toSend = toSend.put(Manual.getName(),new JSONObject()
				.put("left",left)
				.put("right",right)
				.put("ms",100*Car.Car.TIME_MULTIPLIER)
		);

		sendRequest(toSend);
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
		try {
			sock = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
			System.err.println("SOCK FAILED");
			System.exit(1);
		}

		heart = Executors.newScheduledThreadPool(2);
		heart.scheduleAtFixedRate(this::heartbeat, 0, 25*Car.Car.TIME_MULTIPLIER, TimeUnit.MILLISECONDS);

		panel = new Panel();

		JFrame disp = new JFrame("Plane Client (This is Laptop)");
		disp.setSize(1300, 700);
		disp.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent keyEvent) {}

			@Override
			public void keyPressed(KeyEvent keyEvent) {
//				System.out.println("PRESSEDD");
				keyUsed(keyEvent.getKeyCode(), true);
			}

			@Override
			public void keyReleased(KeyEvent keyEvent) {
//				System.out.println("RELEASEDDD");
				keyUsed(keyEvent.getKeyCode(), false);
			}
		});
		disp.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		disp.add(panel);
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
		System.out.println("Sending "+request.toString());
		DatagramPacket packet = new DatagramPacket(requestData,requestData.length, SEND_ADDR);
		try {
			sock.setSoTimeout(1000);
			sock.send(packet);
			sock.receive(packet);
			String response = new String(packet.getData());
			System.out.println("CAR responded with "+response);
			return new JSONObject(response);
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

	void keyUsed (int id, boolean pressed) {

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
			//module settings
			case KeyEvent.VK_M:modulesEnabled.add(Manual.getName());break;
			case KeyEvent.VK_H:
				if (pressed) {
					modulesEnabled.add(Horn.getName());
				} else {
					modulesEnabled.remove(Horn.getName());
				}
				break;
			case KeyEvent.VK_K:modulesEnabled.add(Kill.getName());break;
		}

		//update from key
		new Thread(this::heartbeat).start();
	}
}