package Car;

import Car.module.Horn;
import Car.module.Kill;
import Car.module.Manual;
import Car.module.HC_SR04;
import gpio.L298NDriver;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * This is CARLOS (Car Lightweight Operating System)
 *
 * It contains a modular hardware control system
 *
 *
 * */
public class Car {



	//lower to send more packets and to stop the car quicker on lost connection
	public static final int TIME_MULTIPLIER = 5;

	private static final boolean DEBUG = true;
	static boolean running = true;

	//set by modules
	public static L298NDriver l298NDriver;

	TreeMap<String,CarModule> modules = new TreeMap<String, CarModule>();

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
	 *      modules: (off, manual, etc.)// Required for write operations. Specifies modules that should be on
	 *          Ex: ["manual","honk","info"]
	 *      (moduleName): // Sends data to specified module. See module for more documentation
	 *          Example: honk:honk would send honk to the honk module
	 *
	 *      info: info // optional - if sent by client, the server will send back the "info" data
	 *      kill: kill // If "kill" key sent, the server software ends. Before calling, maybe set manualSpeed to 0,0 and receive a 200
	 *      //todo more stuff, such as reading sensors and sending sensor data back
	 * }
	 * main RESPONSE:
	 * {
	 *      moduleData: //optional - Whatever the module is responding with, if anything
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
		 * @param request the client input controls
		 * @return the server response
		 * */
		JSONObject applyControls(JSONObject request) {
			JSONObject ret = new JSONObject();
			//by default returns 404 (operation not found, in this case), but could be overwritten
			ret.put("code",404);

			if (request.has("modules")) {
				//change modules as necessary
				JSONArray requestedModulesJSON = request.getJSONArray("modules");
				String[] requestedModulesArr = new String[requestedModulesJSON.length()];
				for (int m=0;m<requestedModulesArr.length;m++) {
					requestedModulesArr[m] = requestedModulesJSON.getString(m);
				}
				List<String> requestedModulesList = Arrays.asList(requestedModulesArr);

				for(Map.Entry<String, CarModule> module : modules.entrySet()) {
					//enable all disabled modules that were requested to be enabled
					if (!module.getValue().isEnabled() && requestedModulesList.contains(module.getKey())) module.getValue().enable();
					//disable all enabled modules that were requested to be disabled
					if (module.getValue().isEnabled() && !requestedModulesList.contains(module.getKey())) module.getValue().disable();
					//process all module input
					if (request.has(module.getKey())) {
						//todo might not work?
						module.getValue().moduleInput(request.getJSONObject(module.getKey()));
					}
				}

			}

			if (!DEBUG) {
				ret.remove("info");
				ret.remove("error");
			}

			return ret;
		}
	}

	Car() {
		System.out.println("Adding modules");
		//put every potential module here

//		modules.put(Off.getName(),new Off());
		modules.put(Manual.getName(),new Manual());
		modules.put(Horn.getName(), new Horn(3));
		modules.put(Kill.getName(), new Kill());
		modules.put(HC_SR04.getName(), new HC_SR04(-1,0));
		modules.get(HC_SR04.getName()).enable();

		System.out.println("Initializing driver");
		l298NDriver = new L298NDriver(25,27,28,29,26,23,true,false);
		System.out.println("Initializing JSON Server");
		server = new CarServer();
		System.out.println("JSON server initialized");
	}

	public static void done() {
		running = false;
		if (l298NDriver != null) l298NDriver.stop();
		sleep(100);
		System.exit(0);
	}

	private static void sleep(int ms) {
		try {Thread.sleep(ms);} catch (InterruptedException e) {e.printStackTrace();}
	}

}
