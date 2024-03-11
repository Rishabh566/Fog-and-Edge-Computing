/*** Required libraries Imported from DCNSFog.java */
package org.fog.test.perfeval;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.cloudbus.cloudsim.sdn.overbooking.BwProvisionerOverbooking;
import org.cloudbus.cloudsim.sdn.overbooking.PeProvisionerOverbooking;
import org.fog.application.AppEdge;
import org.fog.application.AppLoop;
import org.fog.application.Application;
import org.fog.application.selectivity.FractionalSelectivity;
import org.fog.entities.Actuator;
import org.fog.entities.FogBroker;
import org.fog.entities.FogDevice;
import org.fog.entities.FogDeviceCharacteristics;
import org.fog.entities.Sensor;
import org.fog.entities.Tuple;
import org.fog.placement.Controller;
import org.fog.placement.ModuleMapping;
import org.fog.placement.ModulePlacementEdgewards;
import org.fog.placement.ModulePlacementMapping;
import org.fog.policy.AppModuleAllocationPolicy;
import org.fog.scheduler.StreamOperatorScheduler;
import org.fog.utils.FogLinearPowerModel;
import org.fog.utils.FogUtils;
import org.fog.utils.TimeKeeper;
import org.fog.utils.distribution.DeterministicDistribution;
public class CA_03 {	
	/*** List of Fog Devices */
	static List<FogDevice> fogDevices = new ArrayList<FogDevice>(); /*** Multiple fog devices from different houses will be stored in this ArrayList */
	static List<Sensor> sensors = new ArrayList<Sensor>();
	static List<Actuator> actuators = new ArrayList<Actuator>(); /*** Local Screens in different houses */
	/*** Number of Fog Nodes per House */
	static int numOfAreas = 4; 
	/*** Number of Cameras per House */
	static int numOfCamerasPerArea1=4; 
	/*** Visual are captured in 'CAM_TRANSMISSION_TIME' seconds by cameras */
	static double CAM_TRANSMISSION_TIME = 5; 
	/*** 'False' means data is sent to Fog Node,'True' means data is sent to Cloud Directly */
	private static boolean CLOUD = false; 
	public static void main(String[] args) {
		Log.printLine("====Runing Smart Home Security System====");
		try {
			Log.disable();
			int num_user = 1; // number of cloud users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false; // mean trace events
			CloudSim.init(num_user, calendar, trace_flag);
			String appId = "dcns"; // identifier of the application
			FogBroker broker = new FogBroker("broker");
			Application application = createApplication(appId, broker.getId());
			application.setUserId(broker.getId());
			createFogDevices(broker.getId(), appId);
			Controller controller = null;	
			/*** Note: Multiple Modules(VMs/ Processing Machine)can be created in a single Fog Node to do tasks. */
			ModuleMapping moduleMapping = ModuleMapping.createModuleMapping(); // Initialising a module mapping
			
			/*** Getting the list of House cameras and if the device name starts with 'c' then it adds the module "video-capture" to the cameras. 
			 * Every Camera is a Fog Node  */
			for(FogDevice device : fogDevices){
				if(device.getName().startsWith("c")){ // names of all Smart Cameras start with 'm' 
					moduleMapping.addModuleToDevice("video-capture", device.getName());  // fixing 1 instance of the Threat Detector module to each Smart Camera
				}
			}
			
			//moduleMapping.addModuleToDevice("user_interface", "cloud"); // fixing instances of User Interface module in the Cloud
			/***  If data is sent directly to the cloud then "threat-detector", "video-capture" module is added */
			if(CLOUD){
				//moduleMapping.addModuleToDevice("video-capture", "cloud"); // placing all instances of Object Detector module in the Cloud
				moduleMapping.addModuleToDevice("threat-detector", "cloud"); // placing all instances of Object Tracker module in the Cloud
				moduleMapping.addModuleToDevice("generate-alert", "cloud");
			}else
			{
			/*** Getting the list of the Fog device and if the device name starts with 'a' then it adds the module "threat-detector" to the device*/
			for(FogDevice device : fogDevices){
				if(device.getName().startsWith("a")){ // names of all fog devices start with 'a' 
					moduleMapping.addModuleToDevice("threat-detector", device.getName());
					moduleMapping.addModuleToDevice("generate-alert", device.getName());// fixing 1 instance of the Threat Detector module to each Smart Camera
				}
			}
			}	
			/***  "master-controller", fogDevices, sensors, actuators are passed to Controller class which simulates environment. */
			controller = new Controller("master-controller", fogDevices, sensors, actuators);
			
			controller.submitApplication(application, 
					(CLOUD)?(new ModulePlacementMapping(fogDevices, application, moduleMapping))
							:(new ModulePlacementEdgewards(fogDevices, sensors, actuators, application, moduleMapping)));
			
			TimeKeeper.getInstance().setSimulationStartTime(Calendar.getInstance().getTimeInMillis());
			CloudSim.startSimulation();
			CloudSim.stopSimulation();

			Log.printLine("VRGame finished!");
		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("Unwanted errors happen");
		}
	}
	
	/**
	 * Creates the fog devices in the physical topology of the simulation.
	 * @param userId
	 * @param appId
	 */
	private static void createFogDevices(int userId, String appId) {
	/***  Adding cloud Server. */
		FogDevice cloud = createFogDevice("cloud", 44800, 40000, 100, 10000, 0, 0.01, 16*103, 16*83.25);
		cloud.setParentId(-1);
		fogDevices.add(cloud);
	
	/***  Adding Proxy Server. */
		FogDevice proxy = createFogDevice("proxy-server", 2800, 4000, 10000, 10000, 1, 0.0, 107.339, 83.43);
		proxy.setParentId(cloud.getId());
		
	/***  latency of connection between proxy server and cloud is 100 ms. */
		proxy.setUplinkLatency(100);
		fogDevices.add(proxy);
		for(int i=0;i<numOfAreas;i++){
			addArea(i+"", userId, appId, proxy.getId());
		}
	}

	/***  Adding Fog Node. Below method is called multiple times based on number of Houses as number of houses is equal to number of nodes */
	private static FogDevice addArea(String id, int userId, String appId, int parentId){
		FogDevice router = createFogDevice("a-"+id, 2800, 4000, 1000, 10000, 2, 0.0, 107.339, 83.43);
		fogDevices.add(router);
		
	/***  latency of connection between proxy server and fog node.(ms) */
		router.setUplinkLatency(2);
		for(int i=0;i<numOfCamerasPerArea1;i++){
			String mobileId = id+"-"+i;
			FogDevice camera = addCamera(mobileId, userId, appId, router.getId()); // adding a smart camera to the physical topology. Smart cameras have been modeled as fog devices as well.
			camera.setUplinkLatency(2); // latency of connection between camera and router is 2 ms
			fogDevices.add(camera);
		}
		router.setParentId(parentId);
		return router;
	}
	
	/***  Adding Cameras and adding Embedding sensors into camera */
	private static FogDevice addCamera(String id, int userId, String appId, int parentId){
		FogDevice camera = createFogDevice("c-"+id, 500, 1000, 10000, 10000, 3, 0, 87.53, 82.44);
		camera.setParentId(parentId);
		
		Sensor sensor = new Sensor("s-"+id, "CAMERA", userId, appId, new DeterministicDistribution(CAM_TRANSMISSION_TIME)); // inter-transmission time of camera (sensor) follows a deterministic distribution
		sensors.add(sensor);
		
		Actuator mob = new Actuator("ptz-"+id, userId, appId, "MOBILE_DEVICE");
		actuators.add(mob);
		
		Actuator los = new Actuator("los-"+id, userId, appId, "LOCAL_SCREEN");
		actuators.add(los);
		
		sensor.setGatewayDeviceId(camera.getId());
		sensor.setLatency(1.0);  // latency of connection between camera (sensor) and the parent Smart Camera is 1 ms
		
		mob.setGatewayDeviceId(parentId);
		mob.setLatency(1.0);  // latency of connection between MOBILE-DEVICE and the parent Smart Camera is 1 ms
		
		los.setGatewayDeviceId(parentId);
		los.setLatency(1.0);  // latency of connection between LOCAL_SCREEN and the parent Smart Camera is 1 ms
		
		return camera;
	}
	
	/**
	 * Creates a vanilla fog device
	 * @param nodeName name of the device to be used in simulation
	 * @param mips MIPS
	 * @param ram RAM
	 * @param upBw uplink bandwidth
	 * @param downBw downlink bandwidth
	 * @param level hierarchy level of the device
	 * @param ratePerMips cost rate per MIPS used
	 * @param busyPower
	 * @param idlePower
	 * @return
	 */
	private static FogDevice createFogDevice(String nodeName, long mips,
			int ram, long upBw, long downBw, int level, double ratePerMips, double busyPower, double idlePower) {
		
		List<Pe> peList = new ArrayList<Pe>();

		// 3. Create PEs and add these into a list.
		peList.add(new Pe(0, new PeProvisionerOverbooking(mips))); // need to store Pe id and MIPS Rating

		int hostId = FogUtils.generateEntityId();
		long storage = 1000000; // host storage
		int bw = 10000;

		PowerHost host = new PowerHost(
				hostId,
				new RamProvisionerSimple(ram),
				new BwProvisionerOverbooking(bw),
				storage,
				peList,
				new StreamOperatorScheduler(peList),
				new FogLinearPowerModel(busyPower, idlePower)
			);
		List<Host> hostList = new ArrayList<Host>();
		hostList.add(host);
		String arch = "x86"; // system architecture
		String os = "Linux"; // operating system
		String vmm = "Xen";
		double time_zone = 10.0; // time zone this resource located
		double cost = 3.0; // the cost of using processing in this resource
		double costPerMem = 0.05; // the cost of using memory in this resource
		double costPerStorage = 0.001; // the cost of using storage in this
										// resource
		double costPerBw = 0.0; // the cost of using bw in this resource
		LinkedList<Storage> storageList = new LinkedList<Storage>(); // we are not adding SAN
													// devices by now
		FogDeviceCharacteristics characteristics = new FogDeviceCharacteristics(
				arch, os, vmm, host, time_zone, cost, costPerMem,
				costPerStorage, costPerBw);
		FogDevice fogdevice = null;
		try {
			fogdevice = new FogDevice(nodeName, characteristics, 
					new AppModuleAllocationPolicy(hostList), storageList, 10, upBw, downBw, 0, ratePerMips);
		} catch (Exception e) {
			e.printStackTrace();
		}
		fogdevice.setLevel(level);
		return fogdevice;
	}

	/**
	 * Function to create the Intelligent Surveillance application in the DDF model. 
	 * @param appId unique identifier of the application
	 * @param userId identifier of the user of the application
	 * @return
	 */
	@SuppressWarnings({"serial" })
	private static Application createApplication(String appId, int userId){
		
		Application application = Application.createApplication(appId, userId);
		/*
		 * Adding modules (vertices) to the application model (directed graph)
		 */
		application.addAppModule("video-capture", 10);
		application.addAppModule("threat-detector", 10);
		application.addAppModule("generate-alert", 10);
		/*
		 * Connecting the application modules (vertices) in the application model (directed graph) with edges
		 */
		application.addAppEdge("CAMERA", "video-capture", 800, 25000, "CAMERA", Tuple.UP, AppEdge.SENSOR); // adding edge from CAMERA (sensor) to Threat Detector module carrying tuples of type CAMERA
		// adding edge from video-capture TO threat-detector
		application.addAppEdge("video-capture", "threat-detector",2000, 4000, "threats",Tuple.UP, AppEdge.MODULE);
		
		// adding edge from video-capture to LOCAL_SCREEN (actuator)
		application.addAppEdge("video-capture", "LOCAL_SCREEN", 600, 28, 100, "LOCAL_PARAMS", Tuple.DOWN, AppEdge.ACTUATOR);
		
		// adding edge from threat-detector TO generate-alert
		application.addAppEdge("threat-detector", "generate-alert",2000, 400, "alert",Tuple.UP, AppEdge.MODULE);
		// adding edge from generate-alert to MOBILE_DEVICE (actuator)
		application.addAppEdge("generate-alert", "MOBILE_DEVICE", 110, 28, 100, "MOBILE_PARAMS", Tuple.UP, AppEdge.ACTUATOR);
		
		application.addTupleMapping("video-capture", "CAMERA", "threats", new FractionalSelectivity(1.0));
		application.addTupleMapping("video-capture", "CAMERA", "LOCAL_PARAMS", new FractionalSelectivity(1.0));
		application.addTupleMapping("threat-detector", "threats", "alert", new FractionalSelectivity(1.0));
		application.addTupleMapping("generate-alert", "alert", "MOBILE_PARAMS", new FractionalSelectivity(1.0));
		
		final AppLoop loop1 = new AppLoop(new ArrayList<String>(){{add("CAMERA"); add("video-capture");add("threat-detector");add("generate-alert"); add("MOBILE_DEVICE");}});
		final AppLoop loop2 = new AppLoop(new ArrayList<String>(){{add("video-capture");add("LOCAL_SCREEN");}});
		List<AppLoop> loops = new ArrayList<AppLoop>(){{add(loop1);add(loop2);}};
		
		application.setLoops(loops);
		return application;
		}
	}
