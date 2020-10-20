package org.cloudsimplus.haps;

import ch.qos.logback.classic.Level;
import org.cloudbus.cloudsim.brokers.*;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.cloudlets.CloudletSimple;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.datacenters.DatacenterSimple;
import org.cloudbus.cloudsim.datacenters.GeoLocation;
import org.cloudbus.cloudsim.datacenters.Location;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.hosts.HostSimple;
import org.cloudbus.cloudsim.resources.Pe;
import org.cloudbus.cloudsim.resources.PeSimple;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelDynamic;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.vms.VmSimple;
import org.cloudsimplus.builders.tables.CloudletsTableBuilder;
import org.cloudsimplus.builders.tables.TextTableColumn;
import org.cloudsimplus.builders.tables.VmsTableBuilder;
import org.cloudsimplus.examples.brokers.DatacenterBrokersMappingComparison;
import org.cloudsimplus.util.Log;

import java.util.*;

public class HapsExample1 {

    /**
     * Datacenters Location
     */

    private static final int HOSTS = 5;
    private static final int HOST_PES = 8;

    /**
     * User locations to user
     */

    private static final ArrayList<Location> locationsDatacenter= new ArrayList<>();
    private static final ArrayList<Location> locationsVms= new ArrayList<>();

    private static final int VM_PES = 4;

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
        new HapsExample1();
    }

    public void locationCreate(ArrayList<Location> target1, ArrayList<Location> target2, int base, int haps, int Vm){
        double minX = -180;
        double minY = -90;
        double minZ = 0;

        double maxX = 180;
        double maxY = 90;
        double maxZ = 0;

        for(int i=0; i<Vm; i++){
            double a = (Math.random() * (maxX-minX)) + minX;
            double b = (Math.random() * (maxY-minY)) + minY;
            double c = (Math.random() * (maxZ-minZ)) + minZ;
            target1.add(new Location(a,b,c));
        }

        for(int i=0; i<base; i++){
            double a = (Math.random() * (maxX-minX)) + minX;
            double b = (Math.random() * (maxY-minY)) + minY;
            double c = (Math.random() * (maxZ-minZ)) + minZ;
            target2.add(new Location(a,b,c));
        }

        for(int i=0; i<haps; i++){
            minZ = 10000;
            maxZ = 20000;
            double a = (Math.random() * (maxX-minX)) + minX;
            double b = (Math.random() * (maxY-minY)) + minY;
            double c = (Math.random() * (maxZ-minZ)) + minZ;
            target2.add(new Location(a,b,c));
        }
    }

    private HapsExample1() {
        Log.setLevel(Level.WARN);
        locationCreate(locationsVms,locationsDatacenter,20,1,1000);

        simulation = new CloudSim();
        datacenterList = createDatacenters();

        //Creates a broker that is a software acting on behalf a cloud customer to manage his/her VMs and Cloudlets
        broker0 = new DatacenterBrokerHAPSPolicy(simulation);

        vmList = createVms();
        cloudletList = createCloudlets();

		/*Enables the selection of the closest datacenter for every VM,
        then submits Vms and Cloudlets.*/
        broker0.setSelectClosestDatacenter(true).submitVmList(vmList).submitCloudletList(cloudletList);

        simulation.start();

        final List<Vm> finishedVms = broker0.getVmCreatedList();
        final List<Cloudlet> finishedCloudlets = broker0.getCloudletFinishedList();
        finishedCloudlets.sort(Comparator.comparingDouble(cl -> cl.getId()));
        finishedVms.sort(Comparator.comparingDouble(vm -> vm.getId()));

        new CloudletsTableBuilder(finishedCloudlets)
                .addColumn(3, new TextTableColumn("  Latency  ", "VmToDc  "), this::getLatencyCloudlet)
                .build();

        new VmsTableBuilder(finishedVms)
                .addColumn(2, new TextTableColumn("   DC    ", "RangeToVM"), this::getDistanceVmtoDc)
                .addColumn(3, new TextTableColumn("  Latency ", "VM_to_DC"), this::getLatencyVm)
                .build();

    }

    public String getLatencyVm(final Vm vm) {
        if(vm.getLatecny() > 0) {
            return String.format("%.2f ms", vm.getLatecny());
        }
        return "Fail";
    }

    public String getLatencyCloudlet(final Cloudlet cloudlet) {
        if(cloudlet.getVm().getLatecny() > 0){
            return String.format("%.2f ms", (cloudlet.getVm().getLatecny()));
        }
        else {
            return "Fail";
        }

    }

    public String getDistanceVmtoDc(final Vm vm) {
        return String.format("%.2f", GeoLocation.distance(vm.getHost().getDatacenter().getLocation(), vm.getLocation())) + "km";
    }

    /**
     * Creates a List of Datacenters, each Datacenter having
     * Hosts with a number of PEs higher than the previous Datacenter.
     */
    private List<Datacenter> createDatacenters(){
        final List<Datacenter> list = new ArrayList<>(locationsDatacenter.size());
        for (int i=0; i<locationsDatacenter.size(); i++) {
            final Datacenter dc = createDatacenter();
            dc.setLocation(locationsDatacenter.get(i));

            list.add(dc);
      }
        return list;
    }

    /**
     * Creates a Datacenter in a given timezone.
     */
    private Datacenter createDatacenter() {
        final List<Host> hostList = new ArrayList<>(HOSTS);
        for(int i=0; i<HOSTS; i++) {
            Host host = createHost();
            hostList.add(host);
        }
        //Uses a VmAllocationPolicySimple by default to allocate VMs
        final Datacenter dc = new DatacenterSimple(simulation, hostList);
        return dc;
    }

    private Host createHost() {
        final List<Pe> peList = new ArrayList<>(HOST_PES);

        //List of Host's CPUs (Processing Elements, PEs)
        for(int i=0; i<HOST_PES; i++) {
            peList.add(new PeSimple(10000));
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
        final List<Vm> list = new ArrayList<>(locationsVms.size());
        for(int i=0; i<locationsVms.size(); i++) {

            //Uses a CloudletSchedulerTimeShared by default to schedule Cloudlets
            final Vm vm = new VmSimple(1000, VM_PES);
            vm.setRam(512).setBw(1000).setSize(10000).setLocation(locationsVms.get(i));
            list.add(vm);
        }
        return list;

    }

    /**
     * Creates a list of Cloudlets.
     */
    private List<Cloudlet> createCloudlets() {
        final List<Cloudlet> list = new ArrayList<>(locationsVms.size());

        //UtilizationModel defining the Cloudlets use only 50% of any resource all the time
        final UtilizationModelDynamic utilizationModel = new UtilizationModelDynamic(0.5);

        for(int i=0; i<locationsVms.size(); i++) {
            final Cloudlet cloudlet = new CloudletSimple(CLOUDLETS_LENGTH, CLOUDLETS_PES, utilizationModel);
            cloudlet.setSizes(10204);
            list.add(cloudlet);
        }
        return list;

    }
}
