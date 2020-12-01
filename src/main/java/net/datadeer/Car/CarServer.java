package net.datadeer.Car;

import net.datadeer.module.Module;
import net.datadeer.module.ModuleGroup;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * CarController Protocol v1.0.0
 *      Used by net.datadeer.Car Remote to tell car what to do, whether it be manual controls or automatic path-findings
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
public class CarServer extends JsonServer {

	boolean running;

	public static final int PORT = 50303;
	public static final String NAME = "CarJSONServer";

	CarServer() {
		super(PORT);
	}

	@Override public String getName() {return NAME;}

	@Override
	protected void onEnable() {
		super.onEnable();
		running = true;
		listenForClients();
	}

	@Override
	public void onDisable() {
		super.onDisable();
		running = false;
	}

	/**
	 * Starts a new thread that listens for clients
	 * TODO replace with packet listener
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
//		System.out.println("INPUT: "+request.toString());
		JSONObject response = applyControls(request);
//		System.out.println("OUTPUT: "+response.toString());
		JSONObject.testValidity(response);
		byte[] responseData = (response.toString()+"\0").getBytes();
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
		JSONObject response = new JSONObject();
		if (request.has("modules")) {
			//change modules as necessary
			JSONArray requestedModulesJSON = request.getJSONArray("modules");
			String[] requestedModulesArr = new String[requestedModulesJSON.length()];
			for (int m=0;m<requestedModulesArr.length;m++) {
				requestedModulesArr[m] = requestedModulesJSON.getString(m);
			}
			List<String> requestedModulesList = Arrays.asList(requestedModulesArr);

			getModuleGroup().getModulesKeyset().forEach((key) -> {
				Module module = getModuleGroup().getModule(key);
				//enable all disabled modules that were requested to be enabled
				if (!module.isEnabled() && requestedModulesList.contains(key))
					module.enable();
				//disable all enabled modules that were requested to be disabled
				if (module.isEnabled() && !requestedModulesList.contains(key))
					module.disable();
				//process all module input
				JSONObject moduleInfoProvided = request.optJSONObject(key);
				if (moduleInfoProvided != null) {
					JSONObject moduleResponse = module.moduleInput(moduleInfoProvided);
					if (moduleResponse != null) {
						response.put(module.getName(), moduleResponse);
					}
				}
			});
			response.put("modules",getModuleGroup().getEnabledModules().collect(Collectors.joining(",")));
		}

		if (!Car.DEBUG) {
			response.remove("info");
			response.remove("error");
		}

		return response;
	}
}