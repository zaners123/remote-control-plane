package CarClient;

import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
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
	private static final JSONObject KILL_OBJECT = new JSONObject().put("kill","kill");

	private InetSocketAddress SEND_ADDR = new InetSocketAddress(ip, port);

	private int manualPowerLevel = 100;

	private final Font twentyFont = new Font("Default", Font.BOLD, 20);

	//ui
	Panel panel;

	static DatagramSocket sock;

	//map of pressed keys
	HashMap<Integer,Boolean> pressedMap = new HashMap<>();

	//for heartbeat
	Heart heart;

	class Heart {
		ScheduledExecutorService service;
		Heart() {
			service = Executors.newScheduledThreadPool(1);
			service.scheduleAtFixedRate(this::heartbeat, 0, 250, TimeUnit.MILLISECONDS);
		}

		void done() {
			service.shutdown();
		}

		void heartbeat() {
//			System.out.println("BEAT");
			int left = 0;
			int right = 0;
			for(Map.Entry<Integer,Boolean> e : pressedMap.entrySet()) {
				boolean manualMovement = false;
				switch (e.getKey()) {
					case KeyEvent.VK_S:
						manualMovement = true;
						if (!e.getValue()) continue;
						left=-manualPowerLevel;
						right=-manualPowerLevel;
						break;
					case KeyEvent.VK_W:
						manualMovement = true;
						if (!e.getValue()) continue;
						left=manualPowerLevel;
						right=manualPowerLevel;
						break;
					case KeyEvent.VK_A:
						manualMovement = true;
						if (!e.getValue()) continue;
						left=-manualPowerLevel;
						right=manualPowerLevel;
						break;
					case KeyEvent.VK_D:
						manualMovement = true;
						if (!e.getValue()) continue;
						left=manualPowerLevel;
						right=-manualPowerLevel;
						break;
				}
			}
			sendRequest(new JSONObject()
					.put("manualSpeeds",
							new JSONObject()
									.put("left",left)
									.put("right",right)
									.put("ms",1000)
					));
		}
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

		heart = new Heart();
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
	 * */
	void sendRequest(JSONObject request) {
		byte[] requestData = request.toString().getBytes();
		if (requestData.length> 1024) {
			System.err.println("AH HECK THIS PACKET MIGHT BE TOO LARGE FOR UDP");
		}
		//todo should reuse socket
		System.out.println("Sending "+request.toString());
		DatagramPacket packet = new DatagramPacket(requestData,requestData.length, SEND_ADDR);
		try {
			sock.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Couldn't send UDP");
		}
	}

	private void sleep(int ms) {
		try {Thread.sleep(ms);} catch (InterruptedException e) {e.printStackTrace();}
	}

	void keyUsed (int id, boolean pressed) {
		//already held
		if (pressed == pressedMap.getOrDefault(id,!pressed)) return;

		if (pressed) {
			//control keys
			switch (id) {
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
				case KeyEvent.VK_M:sendRequest(new JSONObject().put("state","manual"));return;
				case KeyEvent.VK_K:sendRequest(KILL_OBJECT);return;
			}
		}

		pressedMap.put(id,pressed);

		//update from key
		heart.heartbeat();
	}
}