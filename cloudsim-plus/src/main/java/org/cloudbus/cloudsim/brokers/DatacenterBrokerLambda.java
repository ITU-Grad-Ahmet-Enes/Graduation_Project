package org.cloudbus.cloudsim.brokers;

import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.datacenters.GeoLocation;
import org.cloudbus.cloudsim.datacenters.Location;
import org.cloudbus.cloudsim.vms.Vm;

public class DatacenterBrokerLambda extends DatacenterBrokerAbstract{
    private Location location;
    private double lambdaValue;

    public double getLambdaValue() {
        return lambdaValue;
    }

    public void setLambdaValue(double lambdaValue) {
        this.lambdaValue = lambdaValue;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public GeoLocation setLocation(Location location) {
        this.location = location;
        return this;
    }
    /**
     * Index of the last Base VM selected from the {@link #getVmExecList()}
     * to run some Cloudlet.
     */
    private int lastSelectedBaseVmIndex;

    /**
     * Index of the last Base Datacenter selected to place some VM.
     */
    private int lastSelectedBaseDcIndex;
    /**
     * Index of the last HAPS VM selected from the {@link #getVmExecList()}
     * to run some Cloudlet.
     */
    private int lastSelectedHAPSVmIndex;

    /**
     * Index of the last HAPS Datacenter selected to place some VM.
     */
    private int lastSelectedHAPSDcIndex;

    private int numberOfDcHAPS;

    private int numberOfDcBase;

    private int numberOfVmHAPS;

    private int numberOfVmBase;

    /**
     * Creates a new DatacenterBroker.
     *
     * @param simulation the CloudSim instance that represents the simulation the Entity is related to
     */
    public DatacenterBrokerLambda(final CloudSim simulation) {
        this(simulation, "");
    }

    /**
     * Creates a DatacenterBroker giving a specific name.
     *
     * @param simulation the CloudSim instance that represents the simulation the Entity is related to
     * @param name the DatacenterBroker name
     */
    public DatacenterBrokerLambda(final CloudSim simulation, final String name) {
        super(simulation, name);
        this.lastSelectedBaseDcIndex = -1;
        this.lastSelectedBaseVmIndex = -1;
        this.lastSelectedHAPSDcIndex = -1;
        this.lastSelectedHAPSVmIndex = -1;
    }

    /**
     * Creates a DatacenterBroker giving a specific name.
     *
     * @param simulation the CloudSim instance that represents the simulation the Entity is related to
     * @param name the DatacenterBroker name
     * @param numberOfDcHAPS number of datacenter HAPS
     * @param numberOfDcBase number of datacenter Base Stations
     * @param numberOfVmHAPS number of vm HAPS
     * @param numberOfVmBase number of vm Base Stations
     */
    public DatacenterBrokerLambda(final CloudSim simulation, final String name, int numberOfDcHAPS, int numberOfDcBase, int numberOfVmHAPS, int numberOfVmBase) {
        super(simulation, name);
        this.lastSelectedBaseDcIndex = -1;
        this.lastSelectedBaseVmIndex = -1;
        this.lastSelectedHAPSDcIndex = -1;
        this.lastSelectedHAPSVmIndex = -1;
        this.numberOfDcBase = numberOfDcBase;
        this.numberOfDcHAPS = numberOfDcHAPS;
        this.numberOfVmBase = numberOfVmBase;
        this.numberOfVmHAPS = numberOfVmHAPS;
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>It applies a Round-Robin policy to cyclically select
     * the next Datacenter from the list. However, it just moves
     * to the next Datacenter when the previous one was not able to create
     * all {@link #getVmWaitingList() waiting VMs}.</p>
     *
     * <p>This policy is just used if the selection of the closest Datacenter is not enabled.
     * Otherwise, the {@link #closestDatacenterMapper(Datacenter, Vm)} is used instead.</p>
     *
     * @param lastDatacenter {@inheritDoc}
     * @param vm {@inheritDoc}
     * @return {@inheritDoc}
     * @see DatacenterBroker#setDatacenterMapper(java.util.function.BiFunction)
     * @see #setSelectClosestDatacenter(boolean)
     */
    @Override
    protected Datacenter defaultDatacenterMapper(final Datacenter lastDatacenter, final Vm vm) {
        if(getDatacenterList().isEmpty()) {
            throw new IllegalStateException("You don't have any Datacenter created.");
        }

        if (vm.getId() >= numberOfDcBase) {
            return getDatacenterList().get(numberOfDcBase + (++lastSelectedHAPSDcIndex));
        } else {
            return getDatacenterList().get(++lastSelectedBaseDcIndex);
        }

        /*If all Datacenter were tried already, return Datacenter.NULL to indicate
         * there isn't a suitable Datacenter to place waiting VMs.
        if(lastSelectedDcIndex == getDatacenterList().size()-1){
            return Datacenter.NULL;
        }*/
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>It applies a Round-Robin policy to cyclically select
     * the next Vm from the {@link #getVmWaitingList() list of waiting VMs}.</p>
     *
     * @param cloudlet {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    protected Vm defaultVmMapper(final Cloudlet cloudlet) {
        double pretectedValue = Math.random();

        if (cloudlet.isBoundToVm()) {
            return cloudlet.getVm();
        }

        if (getVmExecList().isEmpty()) {
            return Vm.NULL;
        }

        if(lastSelectedHAPSVmIndex == numberOfVmHAPS-1) {
            lastSelectedHAPSVmIndex = -1;
        }
        if(lastSelectedBaseVmIndex == numberOfVmBase-1) {
            lastSelectedBaseVmIndex = -1;
        }

        if(pretectedValue <= lambdaValue) {
            lastSelectedBaseVmIndex = ++lastSelectedBaseVmIndex % (numberOfVmBase);
            return getVmFromCreatedList(lastSelectedBaseVmIndex);
        } else {
            int tempLastSelectedHAPSVmIndex =(numberOfVmBase + ++lastSelectedHAPSVmIndex) % (getVmExecList().size());
            return getVmFromCreatedList(tempLastSelectedHAPSVmIndex);
        }
/*
        for(Cloudlet cloudlet1 : getCloudletCreatedList()) {
            System.out.println(cloudlet1.getVm().getId());
            System.out.println(cloudlet1.isFinished());
        }

 */
/*
        for(Vm vm : getVmCreatedList()) {
        }

 */
        /*
        for(Vm vm : getVmCreatedList()) {
            System.out.println(vm.getFreePesNumber());
        }
         */

         /*
        for(Datacenter datacenter : getDatacenterList()) {
        }
        */

        /*
        for(Datacenter datacenter : getDatacenterList()) {
            System.out.println(cloudlet.getBroker().getLocation().getX() + " " + cloudlet.getBroker().getLocation().getY() + " " + cloudlet.getBroker().getLocation().getZ());
            System.out.println(datacenter.getLocation().getX() + " " + datacenter.getLocation().getY() + " " + datacenter.getLocation().getZ());
        }

        for(Datacenter datacenter : datacenterList) {
            System.out.println(cloudlet.getBroker().getLocation().getX() + " " + cloudlet.getBroker().getLocation().getY() + " " + cloudlet.getBroker().getLocation().getZ());
            System.out.println(datacenter.getLocation().getX() + " " + datacenter.getLocation().getY() + " " + datacenter.getLocation().getZ());
        }

         */
        /*If the cloudlet isn't bound to a specific VM or the bound VM was not created,
        cyclically selects the next VM on the list of created VMs.*/


    }
}
