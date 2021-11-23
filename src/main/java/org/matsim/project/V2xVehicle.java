package org.matsim.project;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Identifiable;
import org.matsim.vehicles.Vehicle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

class V2xVehicle implements Identifiable<Vehicle> {
	private final Id<Vehicle> vehicleId;
	private final List<V2xMessage> messages = new ArrayList<>();
	public V2xVehicle( Id<Vehicle> vehicleId ){
		this.vehicleId = vehicleId;
	}
	public Id<Vehicle> getId(){
		return vehicleId;
	}
	public void addMessage( V2xMessage message ) {
		messages.add( message );
	}
	public void addMessages( Collection<V2xMessage> messages ) {
		this.messages.addAll( messages );
	}
	public List<V2xMessage> getMessages() {
		return Collections.unmodifiableList( messages );
	}
}
