package net.datadeer.Car;

import net.datadeer.module.Module;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public abstract class JsonServer extends Module {
	int port;
	DatagramSocket sock;
	JsonServer(int port) {
		this.port = port;
		retrySetup: while(true) {
			try {
				sock = new DatagramSocket(port);
				break retrySetup;
			} catch (IOException e) {
				System.err.println("FAILED TO START JSON SERVER");
				e.printStackTrace();
			}
		}
	}
	DatagramPacket getPacket() throws IOException {
		DatagramPacket packet = new DatagramPacket(new byte[1024],1024);
		sock.receive(packet);
		//System.out.println("RECEIVED PACKET");
		return packet;
	}
}
