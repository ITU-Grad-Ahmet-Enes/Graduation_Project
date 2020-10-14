package org.cloudsimplus.builders.tables;

import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.core.Identifiable;
import org.cloudbus.cloudsim.vms.Vm;

import java.util.List;

public class VmsTableBuilder extends TableBuilderAbstract<Vm> {
    private static final String TIME_FORMAT = "%.0f";
    private static final String SECONDS = "Seconds";
    private static final String CPU_CORES = "CPU cores";

    public VmsTableBuilder(List<? extends Vm> list) {
        super(list);
    }

    public VmsTableBuilder(List<? extends Vm> list, Table table) {
        super(list, table);
    }

    @Override
    protected void createTableColumns() {
        final String ID = "ID";
        addColumnDataFunction(getTable().addColumn("    Vm", ID), Identifiable::getId);
        addColumnDataFunction(getTable().addColumn("    VM PEs", CPU_CORES), vm -> vm.getNumberOfPes());
        addColumnDataFunction(getTable().addColumn("    DC", ID), vm -> vm.getHost().getDatacenter().getId());
        addColumnDataFunction(getTable().addColumn("    Host", ID), vm -> vm.getHost().getId());
        addColumnDataFunction(getTable().addColumn("    Host PEs", CPU_CORES), vm -> vm.getHost().getWorkingPesNumber());

    }

    /**
     * Rounds a given time so that decimal places are ignored.
     * Sometimes a Cloudlet start at time 0.1 and finish at time 10.1.
     * Previously, in such a situation, the finish time was rounded to 11 (Math.ceil),
     * giving the wrong idea that the Cloudlet took 11 seconds to finish.
     * This method makes some little adjustments to avoid such a precision issue.
     *
     * @param cloudlet the Cloudlet being printed
     * @param time the time to round
     * @return
     */
    private double roundTime(final Cloudlet cloudlet, final double time) {

        /*If the given time minus the start time is less than 1,
         * it means the execution time was less than 1 second.
         * This way, it can't be round.*/
        if(time - cloudlet.getExecStartTime() < 1){
            return time;
        }

        final double startFraction = cloudlet.getExecStartTime() - (int) cloudlet.getExecStartTime();
        return Math.round(time - startFraction);
    }
}