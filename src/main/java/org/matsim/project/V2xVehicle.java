package org.matsim.project;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Identifiable;
import org.matsim.vehicles.Vehicle;

import java.util.*;

class V2xVehicle implements Identifiable<Vehicle> {
	private final Id<Vehicle> vehicleId;
	private final Set<V2xMessage> messages = new HashSet<>();
	public V2xVehicle( Id<Vehicle> vehicleId ){
		this.vehicleId = vehicleId;
	}
	public Id<Vehicle> getId(){
		return vehicleId;
	}

	/**
	 * Add message to the vehicle.
	 * @return true whether the message was not already known
	 */
	public boolean addMessage( V2xMessage message ) {
		return messages.add( message );
	}
	public Collection<V2xMessage> getMessages() {
		return Collections.unmodifiableCollection( messages );
	}
}
