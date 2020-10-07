package org.cloudsimplus.haps;

import ch.qos.logback.classic.Level;
import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.cloudlets.CloudletSimple;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.datacenters.DatacenterSimple;
import org.cloudbus.cloudsim.datacenters.TimeZoned;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.hosts.HostSimple;
import org.cloudbus.cloudsim.resources.Pe;
import org.cloudbus.cloudsim.resources.PeSimple;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelDynamic;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.vms.VmSimple;
import org.cloudsimplus.builders.tables.CloudletsTableBuilder;
import org.cloudsimplus.builders.tables.TextTableColumn;
import org.cloudsimplus.util.Log;
import java.util.ArrayList;

import java.util.*;

public class HAPSProject {
	/**
	 * Datacenters Location
	 */
	
	private static final Double[][] DATACENTERS_LOCATIONS = {{15.5,5.0},{19.3,12.0}};
	
	private static final Double[][] VMS_LOCATIONS = {{19.0,10.0},{19.0,9.0}};
	
	/**
	 * DC locations to VM
	 */
	private static final Map<String, Double> DATACENTERS_RANGES = new TreeMap<String, Double>(){{
		put("Base", Math.sqrt(Math.pow((VMS_LOCATIONS[0][0] - DATACENTERS_LOCATIONS[0][0]), 2) + Math.pow((VMS_LOCATIONS[0][1] - DATACENTERS_LOCATIONS[0][1]), 2)));
		put("HAPS", Math.sqrt(Math.pow((VMS_LOCATIONS[0][0] - DATACENTERS_LOCATIONS[1][0]), 2) + Math.pow((VMS_LOCATIONS[0][1] - DATACENTERS_LOCATIONS[1][1]), 2)));
	}};
	
	private static final int HOSTS = 5;
	private static final int HOST_PES = 8;
	
	/**
	 * User locations to user
	 */
	private static final double[] VMS_RANGE = {0.0,1.0};
	
	private static final int VM_PES = 4;
	
	private static final int CLOUDLETS = VMS_RANGE.length;
	private static final int CLOUDLETS_PES = 2;
	private static final int CLOUDLETS_LENGTH = 10000;
	
	private final CloudSim simulation;
	private DatacenterBroker broker0;
	private List<Vm> vmList;
	private List<Cloudlet> cloudletList;
	private List<Datacenter> datacenterList;
	private ArrayList<Double> latencies = new ArrayList<Double>();
	private long lastHostId;
	
	public static void main(String[] args) {
		new HAPSProject();
	}
	
	private HAPSProject() {
		Log.setLevel(Level.WARN);
		
		simulation = new CloudSim();
		datacenterList = createDatacenters();
		
		//Creates a broker that is a software acting on behalf a cloud customer to manage his/her VMs and Cloudlets
		broker0 = new DatacenterBrokerSimple(simulation);
		
		vmList = createVms();
		cloudletList = createCloudlets();
		
		
		/*Enables the selection of the closest datacenter for every VM,
        then submits Vms and Cloudlets.*/
		broker0.setSelectClosestDatacenter(true).submitVmList(vmList).submitCloudletList(cloudletList);
		
		simulation.start();
		
		final List<Cloudlet> finishedCloudlets = broker0.getCloudletFinishedList();
		finishedCloudlets.sort(Comparator.comparingDouble(cl -> cl.getVm().getTimeZone()));
		
		new CloudletsTableBuilder(finishedCloudlets)
		.addColumn(3, new TextTableColumn("   DC    ", "RangeToVM"), this::getDatacenterTimeZone)
		.addColumn(8, new TextTableColumn("   VM    ", "RangeToVM"), this::getVmTimeZone)
		.addColumn(9, new TextTableColumn("  Latency ", "VM_to_DC"), this::getLatency)
		.build();
	}
	
	public String getLatency(final Cloudlet cloudlet) {
		return String.format("%.2f ms", (latencies.get((int) cloudlet.getVm().getId())));
	}
	
	public String getDatacenterTimeZone(final Cloudlet cloudlet) {
		return String.format("%.2f", cloudlet.getVm().getHost().getDatacenter().getTimeZone()) + "km";
	}
	
	public String getVmTimeZone(final Cloudlet cloudlet) {
		return cloudlet.getVm().getTimeZone() + "km";
	}
	/**
     * Creates a List of Datacenters, each Datacenter having
     * Hosts with a number of PEs higher than the previous Datacenter.
     */
	private List<Datacenter> createDatacenters(){
		final List<Datacenter> list = new ArrayList<>(DATACENTERS_RANGES.size());
		for (Map.Entry<String, Double> entry : DATACENTERS_RANGES.entrySet()) {
			final Datacenter dc = createDatacenter(entry.getValue());
			list.add(dc);
			final double latency = (double) ((double) ((1000*entry.getValue())/ 299792458)* Math.pow(10, 6));
			latencies.add(latency);
			System.out.printf("Created Datacenter %2d in %15s | %s%n  Latency to Datacenter: %f ms \n", dc.getId(), entry.getKey(),(String.format("%.2f", entry.getValue()) + "km"),latency);
//			System.out.printf("Created Datacenter %2d in %15s | %s%n", dc.getId(), entry.getKey(),(String.format("%.2f", entry.getValue()) + "km"));
		}
		System.out.println();
		
		return list;
	}
	
	/**
     * Creates a Datacenter in a given timezone.
     */
	private Datacenter createDatacenter(final double timeZone) {
		final List<Host> hostList = new ArrayList<>(HOSTS);
		for(int i=0; i<HOSTS; i++) {
			Host host = createHost();
			hostList.add(host);
		}
		
		//Uses a VmAllocationPolicySimple by default to allocate VMs
		final Datacenter dc = new DatacenterSimple(simulation, hostList);
		dc.setTimeZone(timeZone);
		return dc;
	}
	
	private Host createHost() {
		final List<Pe> peList = new ArrayList<>(HOST_PES);
		
		//List of Host's CPUs (Processing Elements, PEs)
		for(int i=0; i<HOST_PES; i++) {
			peList.add(new PeSimple(1000));
		}
		
		final long ram = 2048;
		final long bw = 10000;
		final long storage = 1000000;
		
		final Host host = new HostSimple(ram, bw, storage, peList);
		host.setId(++lastHostId);
		return host;
	}
	
	 /**
     * Creates a list of VMs, setting the timezone they are expected to be placed.
     * This way, the broker will try to place each VM at the closest
     * Datacenter as possible.
     */
	private List<Vm> createVms() {
		final List<Vm> list = new ArrayList<>(VMS_RANGE.length);
		for(final double timezone : VMS_RANGE) {
			
			//Uses a CloudletSchedulerTimeShared by default to schedule Cloudlets
			final Vm vm = new VmSimple(1000, VM_PES);
			vm.setRam(512).setBw(1000).setSize(10000).setTimeZone(timezone);
			list.add(vm);
		}
		return list;
		
	}
	
	/**
     * Creates a list of Cloudlets.
     */
	private List<Cloudlet> createCloudlets() {
		final List<Cloudlet> list = new ArrayList<>(CLOUDLETS);
		
		//UtilizationModel defining the Cloudlets use only 50% of any resource all the time
		final UtilizationModelDynamic utilizationModel = new UtilizationModelDynamic(0.5);
		
		for(int i=0; i<CLOUDLETS; i++) {
			final Cloudlet cloudlet = new CloudletSimple(CLOUDLETS_LENGTH, CLOUDLETS_PES, utilizationModel);
			cloudlet.setSizes(10204);
			list.add(cloudlet);
		}
		return list;
		
	}
}
