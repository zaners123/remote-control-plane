package Car;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class JsonServer {
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
	/*JSONObject getJson() throws IOException {
		Socket s = sock.accept();
		BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
		JSONObject o = new JSONObject(br.readLine());
		JSONObject.testValidity(o);
		return o;
	}*/
}
