package Car;

import Car.module.Manual;
import Car.module.Off;
import gpio.L298NDriver;
import org.json.JSONObject;

import java.io.IOException;
import java.io.StringReader;
import java.net.DatagramPacket;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Car {

	boolean running = true;

	//set by modules
	public static L298NDriver l298NDriver;

	TreeMap<String,CarModule> modules;
	String currentModule;

	CarServer server;

	public static void main(String[] args) {
		new Car();
	}

	/**
	 * CarController Protocol v1.0.0
	 *      Used by Car Remote to tell car what to do, whether it be manual controls or automatic path-findings
	 * A JSON based protocol, structured as such:
	 *
	 * main REQUEST:
	 * {
	 *      module: (off, manual, etc.)// Required for write operations. Specifies module to be used
	 *      moduleData: // Optional - updates specified module. See module for more documentation
	 *
	 *      info: info // optional - if sent by client, the server will send back the "info" data
	 *      kill: kill // If "kill" key sent, the server software ends. Before calling, maybe set manualSpeed to 0,0 and receive a 200
	 *      //todo more stuff, such as reading sensors and sending sensor data back
	 * }
	 * main RESPONSE:
	 * {
	 *      moduleData: //optional - Whatever the module is responding with
	 *      info: // Optional. Sent when requested. Sends back what request would be necessary to put the car in its current state
	 *          state: (off, manual, etc.)
	 *          manualSpeeds: //containing data you would expect to put it in this state
	 *          //etc... Should include everything the REQUEST would to put car in this state
	 *     	status: Required. (formatted like HTTP Status code) // returned by server, usually returns 200 or 400
	 * }
	 * */
	class CarServer extends JsonServer {
		CarServer() {
			super(50303);
			listenForClients();
		}
		/**
		 * Starts a new thread that listens for clients
		 * */
		void listenForClients() {
			new Thread(() -> {
				while (running) {
					//code to keep the client socket up / available
					try {
						DatagramPacket packet = getPacket();
						new Thread(() -> processPacket(packet)).start();
					} catch (IOException e) {
						System.err.println("Failed to initialize socket with client...");
						e.printStackTrace();
					}
				}
			}).start();
		}

		private void processPacket(DatagramPacket requestPacket) {
			String requestStr = new String(requestPacket.getData());
			JSONObject request = new JSONObject(requestStr);
			JSONObject.testValidity(request);
			System.out.println("INPUT: "+request.toString());
			JSONObject response = applyControls(request);
			System.out.println("OUTPUT: "+response.toString());
			JSONObject.testValidity(response);
			byte[] responseData = response.toString().getBytes();
			DatagramPacket responsePacket = new DatagramPacket(
					responseData,
					responseData.length,
					requestPacket.getAddress(),
					requestPacket.getPort());
			try {
				sock.send(responsePacket);
			} catch (IOException e) {
				System.err.println("Failed sending response packet");
				e.printStackTrace();
			}
		}
		/**
		 * The CarController protocol implementation
		 * @param controls the client input controls
		 * @return the server response
		 * */
		JSONObject applyControls(JSONObject controls) {
			JSONObject ret = new JSONObject();
			//by default returns 404 (operation not found, in this case), but could be overwritten
			ret.put("status",404);

			if (ret.has("module")) {
				//change modules as necessary
				if (!ret.getString("module").equals(currentModule)) {
					String newModule = ret.getString("module");
					if (!modules.containsKey(newModule)) {
						System.err.println("Unknown module "+newModule);
						return ret;
					}
					System.out.println("Changing modules to "+newModule);
					modules.get(currentModule).disable();
					currentModule = newModule;
					modules.get(currentModule).enable();
				}
				//update modules by given inputs
				if (ret.has("moduleData")) {
					JSONObject moduleResponse = modules.get(currentModule).moduleInput(ret.getJSONObject("moduleData"));
					if (moduleResponse != null) {
						ret.put("module",moduleResponse);
					}
				}
			}

			if (controls.has("kill")) {
				System.out.println("KILL REQUEST SENT, adios");
				done();
			}

			return ret;
		}
	}

	Car() {
		System.out.println("Adding modules");
		modules.put("manual",new Manual());
		modules.put("off",new Off());
		System.out.println("Initializing driver");
		l298NDriver = new L298NDriver(25,27,28,29,26,23,true,false);
		System.out.println("Initializing JSON Server");
		server = new CarServer();
	}

	void done() {
		running = false;
		if (l298NDriver != null) l298NDriver.stop();
	}

	private void sleep(int ms) {
		try {Thread.sleep(ms);} catch (InterruptedException e) {e.printStackTrace();}
	}

}
