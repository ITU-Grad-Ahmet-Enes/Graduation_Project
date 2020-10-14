package org.cloudbus.cloudsim.datacenters;

import org.cloudbus.cloudsim.vms.Vm;

import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

public interface GeoLocation {

    Location getLocation();

    GeoLocation setLocation(Location location);

    default Location validateLocation(final Location location) {
        if(location.getX() < 0.0 || location.getY() < 0.0 || location.getY() > 500.0 || location.getX() > 500.0) {
            throw new IllegalArgumentException("Invalid location was entered!");
        }

        return location;
    }

    static Datacenter closestDatacenter(final Vm vm, final List<Datacenter> datacenters) {
        if(Objects.requireNonNull(datacenters).isEmpty()){
            throw new IllegalArgumentException("The list of Datacenters is empty.");
        }

        if(datacenters.size() == 1){
            return datacenters.get(0);
        }

        final ListIterator<Datacenter> it = datacenters.listIterator();
        //
        Datacenter currentDc = Datacenter.NULL, closestDc = currentDc;
        while (it.hasNext()) {
            currentDc = it.next();
            if (!currentDc.getOnOff()) {
                continue;
            } else if(currentDc.getCurrentCapacity() == currentDc.getDatacenterCapacity()) {
                continue;
            }

            if(closestDc != Datacenter.NULL ) {
                if(distance(vm.getLocation(), closestDc.getLocation()) > distance(vm.getLocation(), currentDc.getLocation())) {
                    closestDc = currentDc;
                }
            } else {
                closestDc = currentDc;
            }

        }
        if(closestDc != Datacenter.NULL ) {
            vm.setLatecny(((1000*(GeoLocation.distance(vm.getLocation(), closestDc.getLocation()))/ 299792458) * Math.pow(10, 6)));
            closestDc.increaseCurrentCapacity();
        } else {
            vm.setLatecny(-1);
            System.out.println("Vm id" + vm.getId() + " could not placed due to the server issue.");
        }


        return closestDc;

    }

    static Double distance(final Location l1, final Location l2) {
        return Math.sqrt(Math.pow((l1.getX() - l2.getX()), 2) + Math.pow((l1.getY() - l2.getY()), 2));
    }
}
