package org.cloudsimplus.haps;

import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudbus.cloudsim.brokers.DatacenterBrokerLambda;
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
import org.apache.commons.math3.distribution.WeibullDistribution;


import java.io.IOException;
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
    private static final int NUMBER_OF_CLOUDLETS = 25;

    private final CloudSim simulation;
    private final List<Vm> vmList;
    private final List<Cloudlet> cloudletList;
    private final List<Datacenter> datacenterList;
    private final DatacenterBroker broker;
    private List<Cloudlet> finishedCloudletList;
    private static Map<Double, Integer> finishedSimulationTimes;
    private static WeibullDistribution weibullDistribution;
    private List<Integer> weibullDistList;

    /**
     * Starts the example execution, calling the class constructor\
     * to build and run the simulation.
     *
     * @param args command line parameters
     */
    public static void main(String[] args) throws IOException {
        RandomGenerator rg = new JDKRandomGenerator();
        weibullDistribution = new WeibullDistribution(rg,1.0,25, WeibullDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
        DecimalFormat newFormat = new DecimalFormat("#.#");
        List<Lambda> simulationList = new ArrayList<>(10);
        for(double i=0.0; i<1.0; i+=0.1) {
            //double twoDecimal =  Double.parseDouble(newFormat.format(i));
            double twoDecimal =  Double.parseDouble(newFormat.format(i).replaceAll(",", "."));
            simulationList.add(
                    new Lambda(twoDecimal)
            );
        }

        simulationList.parallelStream().forEach(Lambda::run);
        System.out.println();
        simulationList.forEach(Lambda::printResults);

        List<Double> x = new ArrayList<Double>();
        List<Double> y = new ArrayList<Double>();
        Map<Double, Integer> treeMap = new TreeMap<>(finishedSimulationTimes);
        for(Map.Entry entry : treeMap.entrySet()) {
            System.out.println("Lambda with " + entry.getKey() + " Finish time: " + entry.getValue());
            x.add((Double) entry.getKey());
            y.add(Double.valueOf((Integer)entry.getValue()));
        }
        Plot plot = Plot.plot(Plot.plotOpts().
                title("Lambda").
                legend(Plot.LegendFormat.BOTTOM)).
                xAxis("Lambda", Plot.axisOpts().
                        range(0, 1)).
                yAxis("Time", Plot.axisOpts().
                        range(0, 3000).format(Plot.AxisFormat.NUMBER_INT)).
                series("Data", Plot.data().
                                xy(x,y),
                        Plot.seriesOpts().marker(Plot.Marker.CIRCLE));

        plot.save("lambda", "png");
    }

    /**
     * Default constructor that builds and starts the simulation.
     */
    private Lambda(double lambda) {
        /*Enables just some level of log messages.
          Make sure to import org.cloudsimplus.util.Log;*/
        //Log.setLevel(ch.qos.logback.classic.Level.WARN);

        //System.out.println("Starting " + getClass().getSimpleName());
        simulation = new CloudSim();
        this.vmList = new ArrayList<>();
        this.cloudletList = new ArrayList<>();
        this.datacenterList = new ArrayList<>();
        this.finishedSimulationTimes = new HashMap<>();
        this.weibullDistList = new ArrayList<>();

        createWeibullDist();
        createDatacenter();

        this.broker = new DatacenterBrokerLambda(simulation);
        this.broker.setLocation(new Location(15.0,60.0,0.0));
        ((DatacenterBrokerLambda) this.broker).setLambdaValue(lambda);
        createAndSubmitVms();
        createAndSubmitCloudlets();
    }

    public void createWeibullDist() {
        for(int i=0; i<NUMBER_OF_CLOUDLETS; i++) {
            weibullDistList.add((int)weibullDistribution.sample());
        }
        weibullDistList.sort(Comparator.comparingDouble(Integer::intValue));
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
     * param eventInfo information about the happened event
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

        finishedCloudletList.sort(Comparator.comparingDouble(Cloudlet::getActualCpuTime));
        finishedSimulationTimes.put(((DatacenterBrokerLambda) this.broker).getLambdaValue(), (int)finishedCloudletList.get(finishedCloudletList.size()-1).getActualCpuTime());
    }

    /**
     * Creates VMs and submit them to the broker.
     */
    private void createAndSubmitVms() {
        Vm vm;
        for(int i=0; i<NUMBER_OF_BASE+NUMBER_OF_HAPS;i++) {
            if(i < NUMBER_OF_BASE) {
                vm = createVm(i, false);
                this.vmList.add(vm);
            } else {
                vm = createVm(i, true);
                this.vmList.add(vm);
                vm = createVm(++i, true);
                this.vmList.add(vm);
            }

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
            mips = 1000*3;
            size = 10000*3; // image size (Megabyte)
            ram = 512*3; // vm memory (Megabyte)
            bw = 1000*3;
        }

        Vm vm = new VmSimple(id, mips, VM_PES_NUMBER)
                .setRam(ram).setBw(bw).setSize(size)
                .setCloudletScheduler(new CloudletSchedulerSpaceShared());
        return vm;
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
            cloudletList.add(cloudlet);
        }
        //cloudletList.sort(Comparator.comparingDouble(Cloudlet::getId));
        broker.submitCloudletList(cloudletList);
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

        cloudlet.setSubmissionDelay(weibullDistList.get((int)id));
        cloudlet.setExecStartTime(weibullDistList.get((int)id));
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
            Datacenter datacenter = new DatacenterSimple(simulation, new VmAllocationPolicySimple());
            double a = (Math.random() * (maxX-minX)) + minX;
            double b = (Math.random() * (maxY-minY)) + minY;
            double c = (Math.random() * (maxZ-minZ)) + minZ;
            if(i < NUMBER_OF_BASE) {
                host = createHost(i, false);
                datacenter.addHost(host);
                datacenter.setLocation(new Location(a,b,c));
            } else {
                host = createHost(i, true);
                datacenter.addHost(host);
                datacenter.setLocation(new Location(a,b,c));
                host = createHost(i, true);
                datacenter.addHost(host);
                datacenter.setLocation(new Location(a,b,c));
            }
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
            mips = 1000*3;
            ram = 2048*3; // host memory (Megabyte)
            storage = 1000000*3; // host storage (Megabyte)
            bw = 10000*3; //Megabits/s
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
        //finishedCloudletList.sort(Comparator.comparingDouble(Cloudlet::getId));
        return finishedCloudletList;
    }
}
