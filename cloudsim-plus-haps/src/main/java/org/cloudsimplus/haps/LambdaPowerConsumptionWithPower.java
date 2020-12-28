package org.cloudsimplus.haps;
import org.apache.commons.math3.distribution.WeibullDistribution;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudbus.cloudsim.brokers.DatacenterBrokerLambda;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.cloudlets.CloudletSimple;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.datacenters.DatacenterPowerSupply;
import org.cloudbus.cloudsim.datacenters.DatacenterSimple;
import org.cloudbus.cloudsim.datacenters.Location;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.hosts.HostSimple;
import org.cloudbus.cloudsim.power.models.PowerModel;
import org.cloudbus.cloudsim.power.models.PowerModelLinear;
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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

public class LambdaPowerConsumptionWithPower {
    // Number of broker
    private static final int NUMBER_OF_BROKERS = 2;

    /**
     * Defines the minimum percentage of power a Host uses,
     * even it it's idle.
     */
    private double STATIC_HAPS_POWER_PERCENT = 0.7;

    /**
     * The max number of watt-second (Ws) of power a Host uses.
     */
    private int MAX_HAPS_POWER_WATTS_SEC = 25;

    /**
     * Defines the minimum percentage of power a Host uses,
     * even it it's idle.
     */
    private double STATIC_BASE_POWER_PERCENT = 0.7;

    /**
     * The max number of watt-second (Ws) of power a Host uses.
     */
    private int MAX_BASE_POWER_WATTS_SEC = 50;

    // Number of HAPS and BASE Stations
    private final int NUMBER_OF_BASE;
    private final int NUMBER_OF_HAPS;

    // Properties of HOSTS
    private int HOST_BASE_NUMBER = 20;
    private final int HOST_BASE_PES_NUMBER = NUMBER_OF_BROKERS; // Host bulamama yani VM droplama gibi sorunlar ile karşılaşmamk için broker sayısı kadar HOST PEsimiz olması lazım
    private final long mipsBaseHost = 1000;
    private final long ramBaseHost = 2048;
    private final long storageBaseHost = 1000000;
    private final long bwBaseHost = 10000;

    private int HOST_HAPS_NUMBER = 5;
    private final int HOST_HAPS_PES_NUMBER = NUMBER_OF_BROKERS;
    private final long mipsHAPSHost;
    private final long ramHAPSHost;
    private final long storageHAPSHost;
    private final long bwHAPSHost;

    // Properties of VMS
    private final int VMS_BASE_NUMBER;
    private final int VM_BASE_PES_NUMBER = HOST_BASE_PES_NUMBER/ NUMBER_OF_BROKERS;
    private final int mipsBaseVm = 1000;
    private final long sizeBaseVm = 10000;
    private final int ramBaseVm = 512;
    private final long bwBaseVm = 1000;

    private final int VMS_HAPS_NUMBER;
    private final int VM_HAPS_PES_NUMBER = HOST_HAPS_PES_NUMBER/ NUMBER_OF_BROKERS;
    private final int mipsHAPSVm;
    private final long sizeHAPSVm;
    private final int ramHAPSVm;
    private final long bwHAPSVm;

    // Properties of CLOUDLETS
    private static final int NUMBER_OF_CLOUDLETS = 1000;
    long lengthCLOUDLETS = 10000;


    private final CloudSim simulation;
    private final List<Vm> vmList;
    private final List<Cloudlet> cloudletList;
    private final List<Datacenter> datacenterList;
    private static WeibullDistribution weibullDistribution;
    private final List<Integer> weibullDistList;
    private final List<DatacenterBroker> brokers;
    private static Map<Double, Map<Long, Double>> brokerLambdaEnergyConsumption;

    public static void main(String[] args) throws IOException {
        RandomGenerator rg = new JDKRandomGenerator();
        weibullDistribution = new WeibullDistribution(rg,1.0,25, WeibullDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
        DecimalFormat newFormat = new DecimalFormat("#.#");
        List<LambdaPowerConsumptionWithPower> simulationList = new ArrayList<>(10);
        for(int j=0; j<12; j++ ) {
            int powerConsumptionFactor = (j+1) * 2;
            if(j==0) {
                try(BufferedWriter br = new BufferedWriter(new FileWriter("outputEnergyPower.txt",false))){
                    br.write("Number of brokers: " + NUMBER_OF_BROKERS + "\n");
                }
                try(BufferedWriter br = new BufferedWriter(new FileWriter("outputOnlyNumbersEnergyPower.txt",false))){
                    br.write(NUMBER_OF_BROKERS + "\n");
                }
            }

            int HAPSPowerFactor = ((j+1) * 2)/2;
            for(double i=0.0; i<1.0; i+=0.1) {
                //double twoDecimal =  Double.parseDouble(newFormat.format(i));
                double twoDecimal =  Double.parseDouble(newFormat.format(i).replaceAll(",", "."));
                simulationList.add(
                        new LambdaPowerConsumptionWithPower(twoDecimal, powerConsumptionFactor, HAPSPowerFactor)
                );
            }
        }
        simulationList.parallelStream().forEach(LambdaPowerConsumptionWithPower::run);
        simulationList.forEach(LambdaPowerConsumptionWithPower::printResults);
        simulationList.forEach(LambdaPowerConsumptionWithPower::printResultsOnlyNumbers);

    }

    private LambdaPowerConsumptionWithPower(double lambda, int powerConsumptionFactor, int HAPSPowerFactor) {
        // Datacenter Properties
        NUMBER_OF_BASE = 20;
        HOST_BASE_NUMBER = 20;
        VMS_BASE_NUMBER = HOST_BASE_NUMBER;

        NUMBER_OF_HAPS = 5;
        HOST_HAPS_NUMBER = 5;
        VMS_HAPS_NUMBER = HOST_HAPS_NUMBER;

        mipsHAPSHost = 1000 * HAPSPowerFactor;
        ramHAPSHost = 2048 * HAPSPowerFactor;
        storageHAPSHost = 1000000 * HAPSPowerFactor;
        bwHAPSHost = 10000 * HAPSPowerFactor;

        mipsHAPSVm = 1000 * HAPSPowerFactor;
        sizeHAPSVm = 10000 * HAPSPowerFactor;
        ramHAPSVm = 512 * HAPSPowerFactor;
        bwHAPSVm = 1000 * HAPSPowerFactor;

        MAX_HAPS_POWER_WATTS_SEC *= powerConsumptionFactor;

        simulation = new CloudSim();
        this.vmList = new ArrayList<>(VMS_BASE_NUMBER+VMS_HAPS_NUMBER);
        this.cloudletList = new ArrayList<>(NUMBER_OF_CLOUDLETS);
        this.datacenterList = new ArrayList<>();
        this.brokers = createBrokers(lambda);
        this.weibullDistList = new ArrayList<>();
        brokerLambdaEnergyConsumption = new TreeMap<>();

        createWeibullDist();
        createDatacenter();
        createVmsAndCloudlets();

    }

    public void createWeibullDist() {
        for(int i = 0; i<NUMBER_OF_CLOUDLETS* NUMBER_OF_BROKERS; i++) {
            weibullDistList.add((int)weibullDistribution.sample());
        }
    }

    public void run() {
        simulation.start();
    }

    private void printResultsOnlyNumbers(){
        for (DatacenterBroker broker : brokers) {

            Double powerConsumptionInKWatt = 0.0;
            Map<Long,Double> datacenterEnergyConsumption = new TreeMap<>();
            for(int i = 0; i < broker.getCloudletFinishedList().size(); i++){
                DecimalFormat df = new DecimalFormat("#.##");
                powerConsumptionInKWatt = Double.parseDouble(df.format(broker.getCloudletCreatedList().get(i).getVm().getHost().getDatacenter().getPowerInKWatts()).replaceAll(",", "."));
                long datacenterID = broker.getCloudletCreatedList().get(i).getVm().getHost().getDatacenter().getId();
                datacenterEnergyConsumption.put(datacenterID,powerConsumptionInKWatt);
            }

            Double TotalPowerConsumptionInKWatt = 0.0;
            for(Map.Entry entry : datacenterEnergyConsumption.entrySet()) {
                DecimalFormat df = new DecimalFormat("#.##");
                TotalPowerConsumptionInKWatt += Double.parseDouble(df.format(entry.getValue()).replaceAll(",", "."));
            }

            List<Cloudlet> sortedFinishedCloudletList;
            sortedFinishedCloudletList = broker.getCloudletFinishedList();
            sortedFinishedCloudletList.sort(Comparator.comparingDouble(Cloudlet::getActualCpuTime));

            DecimalFormat df = new DecimalFormat("#.##");

            if(brokerLambdaEnergyConsumption.containsKey(((DatacenterBrokerLambda) broker).getLambdaValue())) {
                brokerLambdaEnergyConsumption.get(((DatacenterBrokerLambda) broker).getLambdaValue()).put(broker.getId(), Double.valueOf(df.format(TotalPowerConsumptionInKWatt).replaceAll(",", ".")));
            } else {
                Map<Long,Double> brokerEnergyConsumption = new TreeMap<>();
                brokerEnergyConsumption.put(broker.getId(), Double.valueOf(df.format(TotalPowerConsumptionInKWatt).replaceAll(",", ".")));
                brokerLambdaEnergyConsumption.put(((DatacenterBrokerLambda) broker).getLambdaValue(), brokerEnergyConsumption);
            }

            //if(((DatacenterBrokerLambda) broker).getLambdaValue() == 1.0) {
            if(brokerLambdaEnergyConsumption.size() == 11){
                if(brokerLambdaEnergyConsumption.get(1.0).size() == NUMBER_OF_BROKERS) {
                    try(BufferedWriter br = new BufferedWriter(new FileWriter("outputOnlyNumbersEnergyPower.txt",true))) {
                        //br.newLine();

                        // First Base Properties
                        br.write(MAX_HAPS_POWER_WATTS_SEC + "\n");
                        for(Map.Entry entry : brokerLambdaEnergyConsumption.entrySet()) {

                            for(Map.Entry value : ((Map<Long, Integer>)entry.getValue()).entrySet()) {
                                br.write("" + value.getValue());
                                br.newLine();
                            }
                        }
                        br.flush();
                        brokerLambdaEnergyConsumption.clear();
                    } catch (IOException e) {
                        System.out.println("Unable to read file ");
                    }
                }
            }
        }
    }

    private void printResults() {
        for (DatacenterBroker broker : brokers) {
/*
            String title = " Simulation with lambda " + ((DatacenterBrokerLambda) broker).getLambdaValue();
            new CloudletsTableBuilder(broker.getCloudletFinishedList())
                    .setTitle(broker.getName() + title)
                    .build();
*/
            Double powerConsumptionInKWatt = 0.0;
            Map<Long,Double> datacenterEnergyConsumption = new TreeMap<>();
            for(int i = 0; i < broker.getCloudletFinishedList().size(); i++){
                DecimalFormat df = new DecimalFormat("#.##");
                powerConsumptionInKWatt = Double.parseDouble(df.format(broker.getCloudletCreatedList().get(i).getVm().getHost().getDatacenter().getPowerInKWatts()).replaceAll(",", "."));
                long datacenterID = broker.getCloudletCreatedList().get(i).getVm().getHost().getDatacenter().getId();
                datacenterEnergyConsumption.put(datacenterID,powerConsumptionInKWatt);
            }

            Double TotalPowerConsumptionInKWatt = 0.0;
            for(Map.Entry entry : datacenterEnergyConsumption.entrySet()) {
                DecimalFormat df = new DecimalFormat("#.##");
                TotalPowerConsumptionInKWatt += Double.parseDouble(df.format(entry.getValue()).replaceAll(",", "."));
            }

            List<Cloudlet> sortedFinishedCloudletList;
            sortedFinishedCloudletList = broker.getCloudletFinishedList();
            sortedFinishedCloudletList.sort(Comparator.comparingDouble(Cloudlet::getActualCpuTime));

            DecimalFormat df = new DecimalFormat("#.##");

            if(brokerLambdaEnergyConsumption.containsKey(((DatacenterBrokerLambda) broker).getLambdaValue())) {
                brokerLambdaEnergyConsumption.get(((DatacenterBrokerLambda) broker).getLambdaValue()).put(broker.getId(), Double.valueOf(df.format(TotalPowerConsumptionInKWatt).replaceAll(",", ".")));
            } else {
                Map<Long,Double> brokerEnergyConsumption = new TreeMap<>();
                brokerEnergyConsumption.put(broker.getId(), Double.valueOf(df.format(TotalPowerConsumptionInKWatt).replaceAll(",", ".")));
                brokerLambdaEnergyConsumption.put(((DatacenterBrokerLambda) broker).getLambdaValue(), brokerEnergyConsumption);
            }

            if(brokerLambdaEnergyConsumption.size() == 11){
                if(brokerLambdaEnergyConsumption.get(1.0).size() == NUMBER_OF_BROKERS) {
                    try(BufferedWriter br = new BufferedWriter(new FileWriter("outputEnergyPower.txt",true))) {
                        br.newLine();
                        int powerFactor = MAX_HAPS_POWER_WATTS_SEC/(MAX_BASE_POWER_WATTS_SEC);
                        br.write("---------------------------------------------------------------------------------------------------------\n");
                        br.write("Base station number: 20, HAPS Number: 5, Power: " + powerFactor);
                        br.newLine();
                        br.write("MAX_BASE_POWER_WATTS_SEC: " + MAX_BASE_POWER_WATTS_SEC + "\nMAX_HAPS_POWER_WATTS_SEC: "+
                                MAX_HAPS_POWER_WATTS_SEC + "\n");
                        br.newLine();
                        br.write("Base Stations Properties \n" +
                                "------------------------------------------\n" +
                                "Number of Stations: " + NUMBER_OF_BASE +
                                ", Number of Hosts: " + HOST_BASE_NUMBER +
                                ", Number of Vms: " + VMS_BASE_NUMBER +
                                ", Mips for Host: " + mipsBaseHost +
                                ", Ram for Host: " + ramBaseHost +
                                ", Storage for Host: " + storageBaseHost +
                                ", BW for Host: " + bwBaseHost +
                                ", Mips for Vm: " + mipsBaseVm +
                                ", Size for Vm: " + sizeBaseVm +
                                ", Ram for Vm: " + ramBaseVm +
                                ", BW for Vm: " + bwBaseVm + "\n");
                        br.newLine();
                        br.write("HAPS Stations Properties \n" +
                                "------------------------------------------\n" +
                                "Number of Stations: " + NUMBER_OF_HAPS +
                                ", Number of Hosts: " + HOST_HAPS_NUMBER +
                                ", Number of Vms: " + VMS_HAPS_NUMBER +
                                ", Mips for Host: " + mipsHAPSHost +
                                ", Ram for Host: " + ramHAPSHost +
                                ", Storage for Host: " + storageHAPSHost +
                                ", BW for Host: " + bwHAPSHost +
                                ", Mips for Vm: " + mipsHAPSVm +
                                ", Size for Vm: " + sizeHAPSVm +
                                ", Ram for Vm: " + ramHAPSVm +
                                ", BW for Vm: " + bwHAPSVm + "\n");
                        br.newLine();
                        br.write("Lambda Results \n" +
                                "------------------------------------------");
                        br.newLine();
                        for(Map.Entry entry : brokerLambdaEnergyConsumption.entrySet()) {
                            br.write("For Lambda: " + entry.getKey());
                            br.newLine();
                            for(Map.Entry value : ((Map<Long, Integer>)entry.getValue()).entrySet()) {
                                br.write("Broker ID: " + value.getKey() + ", Total Energy Consumption in KWatt: " + value.getValue());
                                br.newLine();
                                /*if(value.getKey(). == NUMBER_OF_BROKERS){
                                    br.newLine();
                                }*/
                            }
                        }
                        br.flush();
                        brokerLambdaEnergyConsumption.clear();
                    } catch (IOException e) {
                        System.out.println("Unable to read file ");
                    }
                }
            }
        }
    }

    private List<DatacenterBroker> createBrokers(double lamda) {
        final List<DatacenterBroker> list = new ArrayList<>(NUMBER_OF_BROKERS);
        for(int i = 0; i < NUMBER_OF_BROKERS; i++) {
            DatacenterBroker broker = new DatacenterBrokerLambda(simulation,"",NUMBER_OF_HAPS,NUMBER_OF_BASE,VMS_HAPS_NUMBER,VMS_BASE_NUMBER);
            broker.setLocation(new Location(15.0,60.0,0.0));
            ((DatacenterBrokerLambda) broker).setLambdaValue(lamda);
            list.add(broker);
        }

        return list;
    }

    /**
     * Creates a Datacenter and its Hosts.
     */
    private void createDatacenter() {
        double minX = -180;
        double minY = -90;
        double minZ = 0;

        double maxX = 180;
        double maxY = 90;
        double maxZ = 0;

        for(int i=0; i<NUMBER_OF_BASE+NUMBER_OF_HAPS; i++) {

            double a = (Math.random() * (maxX-minX)) + minX;
            double b = (Math.random() * (maxY-minY)) + minY;
            double c = (Math.random() * (maxZ-minZ)) + minZ;

            if(i < NUMBER_OF_BASE) {
                final List<Host> hostList = new ArrayList<>();
                hostList.add(createHost(i, false));
                Datacenter datacenter = new DatacenterSimple(simulation,hostList, new VmAllocationPolicySimple());
                datacenter.setPowerSupply(new DatacenterPowerSupply());
                datacenter.setLocation(new Location(a,b,c));
                datacenterList.add(datacenter);
            } else {
                final List<Host> hostList = new ArrayList<>();
                hostList.add(createHost(i, true));
                hostList.add(createHost(i, true));
                Datacenter datacenter = new DatacenterSimple(simulation,hostList, new VmAllocationPolicySimple());
                datacenter.setPowerSupply(new DatacenterPowerSupply());
                datacenter.setLocation(new Location(a,b,c));
                datacenterList.add(datacenter);
            }

        }
    }

    private Host createHost(int id, boolean isHAPS) {
        final List<Pe> peList = new ArrayList<>();
        if(!isHAPS) {
            final PowerModel powerModel = new PowerModelLinear(MAX_BASE_POWER_WATTS_SEC, STATIC_BASE_POWER_PERCENT);
            for(int i = 0; i < HOST_BASE_PES_NUMBER; i++){
                peList.add(new PeSimple(mipsBaseHost, new PeProvisionerSimple()));
            }
            return new HostSimple(ramBaseHost, bwBaseHost, storageBaseHost, peList)
                    .setRamProvisioner(new ResourceProvisionerSimple())
                    .setBwProvisioner(new ResourceProvisionerSimple())
                    .setVmScheduler(new VmSchedulerTimeShared())
                    .setPowerModel(powerModel);
        } else {
            for(int i = 0; i < HOST_HAPS_PES_NUMBER; i++){
                peList.add(new PeSimple(mipsHAPSHost, new PeProvisionerSimple()));
            }
            final PowerModel powerModel = new PowerModelLinear(MAX_HAPS_POWER_WATTS_SEC, STATIC_HAPS_POWER_PERCENT);
            return new HostSimple(ramHAPSHost, bwHAPSHost, storageHAPSHost, peList)
                    .setRamProvisioner(new ResourceProvisionerSimple())
                    .setBwProvisioner(new ResourceProvisionerSimple())
                    .setVmScheduler(new VmSchedulerTimeShared())
                    .setPowerModel(powerModel);
        }
    }

    private void createVmsAndCloudlets() {
        int i = 0;
        for (DatacenterBroker broker : brokers) {
            vmList.addAll(createAndSubmitVms(broker));
            cloudletList.addAll(createAndSubmitCloudlets(broker));
        }
    }

    private List<Vm> createAndSubmitVms(DatacenterBroker broker) {
        final List<Vm> list = new ArrayList<>(VMS_BASE_NUMBER+VMS_HAPS_NUMBER);
        for(int i=0; i<VMS_BASE_NUMBER+VMS_HAPS_NUMBER;i++) {
            Vm vm;
            if(i < VMS_BASE_NUMBER) {
                vm = createVm(i, false);
                list.add(vm);
            } else {
                vm = createVm(i, true);
                list.add(vm);
            }

        }
        broker.submitVmList(list);
        return list;
    }

    private Vm createVm(int id, boolean isHAPS) {
        if(!isHAPS) {
            return new VmSimple(id, mipsBaseVm, VM_BASE_PES_NUMBER)
                    .setRam(ramBaseVm).setBw(bwBaseVm).setSize(sizeBaseVm)
                    .setCloudletScheduler(new CloudletSchedulerSpaceShared());
        } else {
            return new VmSimple(id, mipsHAPSVm, VM_HAPS_PES_NUMBER)
                    .setRam(ramHAPSVm).setBw(bwHAPSVm).setSize(sizeHAPSVm)
                    .setCloudletScheduler(new CloudletSchedulerSpaceShared());
        }
    }

    private List<Cloudlet> createAndSubmitCloudlets(DatacenterBroker broker) {
        final List<Cloudlet> list = new ArrayList<>(NUMBER_OF_CLOUDLETS);
        long cloudletId;
        for(long i = (broker.getId()-1) * NUMBER_OF_CLOUDLETS; i < NUMBER_OF_CLOUDLETS*broker.getId(); i++){
            cloudletId = i;
            Cloudlet cloudlet = createCloudlet(cloudletId,lengthCLOUDLETS);
            list.add(cloudlet);
        }
        broker.submitCloudletList(list);
        return list;
    }

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
        cloudlet.setSubmissionDelay(weibullDistList.get((int)id));
        cloudlet.setExecStartTime(weibullDistList.get((int)id));
        return cloudlet;
    }
}
