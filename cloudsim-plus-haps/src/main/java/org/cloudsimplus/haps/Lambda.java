package org.cloudsimplus.haps;

import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudbus.cloudsim.brokers.DatacenterBrokerLambda;
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.cloudlets.CloudletSimple;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.datacenters.DatacenterSimple;
import org.cloudbus.cloudsim.datacenters.Location;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.hosts.HostSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.ResourceProvisionerSimple;
import org.cloudbus.cloudsim.resources.Pe;
import org.cloudbus.cloudsim.resources.PeSimple;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModel;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelDynamic;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelFull;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.vms.VmSimple;
import org.cloudsimplus.builders.tables.CloudletsTableBuilder;
import org.cloudsimplus.examples.ParallelSimulationsExample;
import org.cloudsimplus.examples.listeners.CloudletListenersExample1;
import org.cloudsimplus.listeners.CloudletVmEventInfo;

import java.text.DecimalFormat;
import java.util.*;

public class Lambda {
    /**
     * Number of Processor Elements (CPU Cores) of each Host.
     */
    private static final int HOST_PES_NUMBER = 1;

    /**
     * Number of Processor Elements (CPU Cores) of each VM and cloudlet.
     */
    private static final int VM_PES_NUMBER = 1;

    /**
     * Number of Cloudlets to create.
     */
    private static final int NUMBER_OF_BASE = 20;
    private static final int NUMBER_OF_HAPS = 1;
    private static final int NUMBER_OF_CLOUDLETS = 1000;

    private final List<Host> hostList;
    private final List<Vm> vmList;
    private final List<Cloudlet> cloudletList;
    private final List<Datacenter> datacenterList;
    private final DatacenterBroker broker;
    private final CloudSim simulation;
    private List<Cloudlet> finishedCloudletList;
    private static Map<Double, Integer> finishedSimulationTimes;



    /**
     * Starts the example execution, calling the class constructor\
     * to build and run the simulation.
     *
     * @param args command line parameters
     */
    public static void main(String[] args) {
        DecimalFormat newFormat = new DecimalFormat("#.#");
        List<Lambda> simulationList = new ArrayList<>(10);
        for(double i=0.0; i<1.0; i+=0.1) {
            double twoDecimal =  Double.parseDouble(newFormat.format(i));
            simulationList.add(
                    new Lambda(twoDecimal)
            );
        }

        simulationList.parallelStream().forEach(Lambda::run);
        System.out.println();
        simulationList.forEach(Lambda::printResults);

        Map<Double, Integer> treeMap = new TreeMap<>(finishedSimulationTimes);
        for(Map.Entry entry : treeMap.entrySet()) {
            System.out.println("Lambda with " + entry.getKey() + " Finish time: " + entry.getValue());
        }

    }

    /**
     * Default constructor that builds and starts the simulation.
     */
    private Lambda(double lambda) {
        /*Enables just some level of log messages.
          Make sure to import org.cloudsimplus.util.Log;*/
        //Log.setLevel(ch.qos.logback.classic.Level.WARN);

        System.out.println("Starting " + getClass().getSimpleName());
        simulation = new CloudSim();

        this.hostList = new ArrayList<>();
        this.vmList = new ArrayList<>();
        this.cloudletList = new ArrayList<>();
        this.datacenterList = new ArrayList<>();
        this.finishedSimulationTimes = new HashMap<>();
        createDatacenter();

        this.broker = new DatacenterBrokerLambda(simulation);
        this.broker.setLocation(new Location(15.0,60.0,0.0));
        ((DatacenterBrokerLambda) this.broker).setLambdaValue(lambda);
        createAndSubmitVms();
        createAndSubmitCloudlets();
    }

    public void run() {
        simulation.start();
        this.finishedCloudletList = broker.getCloudletFinishedList();
    }

    /**
     * A Listener function that will be called every time a cloudlet
     * starts running into a VM. All cloudlets will use this same listener.
     *
     * @param eventInfo information about the happened event
     */
    /*
    private void onCloudletStartListener(CloudletVmEventInfo eventInfo) {
        System.out.printf(
                "%n\t#EventListener: Cloudlet %d just started running at Vm %d at time %.2f%n",
                eventInfo.getCloudlet().getId(), eventInfo.getVm().getId(), eventInfo.getTime());
    }

     */

    /**
     * A Listener function that will be called every time a cloudlet
     * finishes running into a VM. All cloudlets will use this same listener.
     *
     * @param eventInfo information about the happened event
     */
    /*
    private void onCloudletFinishListener(CloudletVmEventInfo eventInfo) {
        System.out.printf(
                "%n\t#EventListener: Cloudlet %d finished running at Vm %d at time %.2f%n",
                eventInfo.getCloudlet().getId(), eventInfo.getVm().getId(), eventInfo.getTime());
    }

     */

    private void printResults() {

        String title = "Simulation with lambda " + ((DatacenterBrokerLambda) this.broker).getLambdaValue();
        new CloudletsTableBuilder(getFinishedCloudletList())
                .setTitle(title)
                .build();


        finishedSimulationTimes.put(((DatacenterBrokerLambda) this.broker).getLambdaValue(), new Double(finishedCloudletList.get(finishedCloudletList.size()-1).getFinishTime()).intValue());
    }

    /**
     * Creates cloudlets and submit them to the broker.
     *
     * @see #createCloudlet(long, long)
     */
    private void createAndSubmitCloudlets() {
        long cloudletId;
        long length = 10000;
        for(int i = 0; i < NUMBER_OF_CLOUDLETS; i++){
            cloudletId = i;
            Cloudlet cloudlet = createCloudlet(cloudletId,length);
            this.cloudletList.add(cloudlet);
        }

        this.broker.submitCloudletList(cloudletList);
    }

    /**
     * Creates VMs and submit them to the broker.
     */
    private void createAndSubmitVms() {
        Vm vm;
        for(int i=0; i<NUMBER_OF_BASE+NUMBER_OF_HAPS;i++) {
            if(i < NUMBER_OF_BASE) {
                vm = createVm(i, false);
            } else {
                vm = createVm(i, true);
            }
            this.vmList.add(vm);
        }
        this.broker.submitVmList(vmList);
    }

    /**
     * Creates a VM with pre-defined configuration.
     *
     * @param id the VM id
     * @return the created VM
     */
    private Vm createVm(int id, boolean isHAPS) {
        int mips;
        long size;
        int ram;
        long bw;
        if(!isHAPS) {
            mips = 1000;
            size = 10000; // image size (Megabyte)
            ram = 512; // vm memory (Megabyte)
            bw = 1000;
        } else {
            mips = 1000*5;
            size = 10000*5; // image size (Megabyte)
            ram = 512*5; // vm memory (Megabyte)
            bw = 1000*5;
        }

        Vm vm = new VmSimple(id, mips, VM_PES_NUMBER)
                .setRam(ram).setBw(bw).setSize(size)
                .setCloudletScheduler(new CloudletSchedulerSpaceShared());
        return vm;
    }

    /**
     * Creates a cloudlet with pre-defined configuration.
     *
     * @param id Cloudlet id
     * @param length the cloudlet length in number of Million Instructions (MI)
     * @return the created cloudlet
     */
    private Cloudlet createCloudlet(long id, long length) {
        final long fileSize = 300;
        final long outputSize = 300;
        final int pesNumber = 1;
        final UtilizationModel utilizationModel = new UtilizationModelDynamic(0.2);
        Cloudlet cloudlet
                = new CloudletSimple(id, length, pesNumber)
                .setFileSize(fileSize)
                .setOutputSize(outputSize)
                .setUtilizationModelCpu(new UtilizationModelFull())
                .setUtilizationModelBw(utilizationModel)
                .setUtilizationModelRam(utilizationModel);
        /*
                .addOnStartListener(this::onCloudletStartListener)
                .addOnFinishListener(this::onCloudletFinishListener);

         */
        return cloudlet;
    }

    /**
     * Creates a Datacenter with pre-defined configuration.
     *
     * @return the created Datacenter
     */
    private void createDatacenter() {

        double minX = -180;
        double minY = -90;
        double minZ = 0;

        double maxX = 180;
        double maxY = 90;
        double maxZ = 0;


        Host host;
        for(int i=0; i<NUMBER_OF_BASE+NUMBER_OF_HAPS; i++) {
            double a = (Math.random() * (maxX-minX)) + minX;
            double b = (Math.random() * (maxY-minY)) + minY;
            double c = (Math.random() * (maxZ-minZ)) + minZ;
            if(i < NUMBER_OF_BASE) {
                host = createHost(i, false);
            } else {
                host = createHost(i, true);
            }
            Datacenter datacenter = new DatacenterSimple(simulation, new VmAllocationPolicySimple());
            datacenter.addHost(host);
            datacenter.setLocation(new Location(a,b,c));
            datacenterList.add(datacenter);
        }
    }

    /**
     * Creates a host with pre-defined configuration.
     *
     * @param id The Host id
     * @return the created host
     */
    private Host createHost(int id, boolean isHAPS) {
        List<Pe> peList = new ArrayList<>();
        long mips;
        long ram;
        long storage;
        long bw;
        if(!isHAPS) {
            mips = 1000;
            ram = 2048; // host memory (Megabyte)
            storage = 1000000; // host storage (Megabyte)
            bw = 10000; //Megabits/s
        } else {
            mips = 1000*5;
            ram = 2048*5; // host memory (Megabyte)
            storage = 1000000*5; // host storage (Megabyte)
            bw = 10000*5; //Megabits/s
        }
        for(int i = 0; i < HOST_PES_NUMBER; i++){
            peList.add(new PeSimple(mips, new PeProvisionerSimple()));
        }


        return new HostSimple(ram, bw, storage, peList)
                .setRamProvisioner(new ResourceProvisionerSimple())
                .setBwProvisioner(new ResourceProvisionerSimple())
                .setVmScheduler(new VmSchedulerTimeShared());

    }
    public List<Cloudlet> getFinishedCloudletList() {
        return finishedCloudletList;
    }
}
