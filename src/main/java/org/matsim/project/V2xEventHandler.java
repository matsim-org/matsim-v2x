package org.matsim.project;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.events.*;
import org.matsim.api.core.v01.events.handler.LinkEnterEventHandler;
import org.matsim.api.core.v01.events.handler.LinkLeaveEventHandler;
import org.matsim.api.core.v01.events.handler.VehicleEntersTrafficEventHandler;
import org.matsim.api.core.v01.events.handler.VehicleLeavesTrafficEventHandler;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.events.handler.BasicEventHandler;
import org.matsim.core.gbl.Gbl;
import org.matsim.vehicles.Vehicle;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

class V2xEventHandler implements LinkEnterEventHandler, LinkLeaveEventHandler, VehicleEntersTrafficEventHandler, VehicleLeavesTrafficEventHandler, BasicEventHandler{
	private static final AtomicInteger counter = new AtomicInteger(0);

	private final Map<Id<Vehicle>,V2xVehicle> allVehicles = new LinkedHashMap<>();
	private final Map<Id<Link>, List<V2xVehicle>> vehiclesOnLink = new LinkedHashMap<>();
	private final Map<Id<Link>, List<Link>> opposingLinks = new LinkedHashMap<>();

	private final Network network;
	private final EventsManager events;
	private double lastTime = Double.NEGATIVE_INFINITY;

	public V2xEventHandler( Scenario scenario, EventsManager events ){
		this.network = scenario.getNetwork();
		this.events = events;

		for (Link link : network.getLinks().values()) {

			opposingLinks.put(link.getId(), new ArrayList<>());

			for( Link outgoingLink : link.getToNode().getOutLinks().values() ){
				if ( outgoingLink.getToNode().equals( link.getFromNode() ) ) {
					opposingLinks.get(link.getId()).add( outgoingLink );
				}
			}
		}
	}

	// vehicles can either enter/leave the link via intersections, or they can enter/leave from a parking lot or similar:
	@Override public void handleEvent( LinkEnterEvent event ){
		final Id<Vehicle> vehicleId = event.getVehicleId();
		final Id<Link> linkId = event.getLinkId();
		handleVehicleEnteringLink( event.getTime(), vehicleId, linkId );
	}
	@Override public void handleEvent( LinkLeaveEvent event ){
		handleVehicleLeavingLink( event.getVehicleId(), event.getLinkId() );
	}
	@Override public void handleEvent( VehicleEntersTrafficEvent event ){
		final Id<Vehicle> vehicleId = event.getVehicleId();
		final Id<Link> linkId = event.getLinkId();
		handleVehicleEnteringLink( event.getTime(), vehicleId, linkId );

	}
	@Override public void handleEvent( VehicleLeavesTrafficEvent event ){
		handleVehicleLeavingLink( event.getVehicleId(), event.getLinkId() );
	}

	private void handleVehicleEnteringLink( double now, Id<Vehicle> vehicleId, Id<Link> linkId ){
		V2xVehicle vehicle = allVehicles.computeIfAbsent( vehicleId , V2xVehicle::new );

		// give some vehicles some messages to initialize the process:
//		if ( !vehicleId.toString().contains( "tr_" )  && vehicle.getMessages().isEmpty() ){
//			int ii = counter.incrementAndGet();
//			if ( ii<100 ){
//				vehicle.addMessage( new V2xMessage( Integer.toString( ii ) ) );
//			}
//		}

		// Initialize one message for chosen vehicles
		if ( vehicleId.toString().equals( "149341201" ) || vehicleId.toString().equals("360641201") || vehicleId.toString().equals("400032201") ) {
			if (vehicle.getMessages().isEmpty()) {
				vehicle.addMessage( new V2xMessage( "msg-" + vehicleId ) );
				this.events.processEvent( new PersonEntersVehicleEvent( now, Id.createPersonId( "dummy" ), vehicle.getId() ) );
				// (we add a dummy passenger every time this happens since we can color-code according to this)
			}
		}

		// when a vehicle enters a link, we first add it to the data structure ...
		{
			Collection<V2xVehicle> vehicles = vehiclesOnLink.computeIfAbsent( linkId, dummy -> new LinkedList<>() );
			vehicles.add( vehicle  );
		}

		// ... and then compute interaction with vehicles on opposing links:
		List<Link> opposingLinks = this.opposingLinks.get(linkId);
		for( Link opposingLink : opposingLinks ){
			List<V2xVehicle> vehicles = this.vehiclesOnLink.get( opposingLink.getId() );
			if ( vehicles != null ){
				// can be null if never initialized

				for(  V2xVehicle opposingVehicle : vehicles ){
					interact( now, vehicle, allVehicles.get(opposingVehicle.getId() ) );
				}
			}
		}

	}
	@Override public void handleEvent( Event event ){
		if ( event.getTime() > lastTime ) {
//			for(  Map.Entry<Id<Link>, List<V2xVehicle>> entry : vehiclesOnLink.entrySet() ){
//				StringBuilder strb = new StringBuilder().append("time=").append(event.getTime()).append( "; Link=" ).append( entry.getKey() ).append( " -- " );
//				boolean toPrint = false ;
//				for( V2xVehicle vehicle : entry.getValue() ) {
//					if ( !vehicle.getMessages().isEmpty() ){
//						toPrint = true;
//						strb.append( "|vehicleId=" ).append(vehicle.getId() ).append( "|" );
//						for( V2xMessage message : vehicle.getMessages() ){
//							strb.append("|").append( message.getMessage() ).append( "|" );
//						}
//					}
//				}
//				if ( toPrint ){
//					System.out.println( strb );
//				}
//			}

			lastTime = event.getTime();
		}
	}
	private void interact( double now, V2xVehicle vehicle, V2xVehicle opposingVehicle ){
		if ( vehicle.getId().equals( opposingVehicle.getId() ) ) {
			return;
			// (there are sometimes so-called loop links where toNode==fromNode. In consequence, the link is also identified as opposing link, and thus the vehicle interacts with itself)
		}

		if ( !vehicle.getMessages().isEmpty() || !opposingVehicle.getMessages().isEmpty() ){
//			System.out.println( "Interaction between " + vehicle.getId() + " and " + opposingVehicle.getId() );
		}

		// here we just all messages that a vehicle has to all opposing vehicles:
		for (V2xMessage message : opposingVehicle.getMessages()) {
			if (vehicle.addMessage(message)) {
				this.events.processEvent(new PersonEntersVehicleEvent(now, Id.createPersonId("dummy"), vehicle.getId()));
			}
			// (we add a dummy passenger every time this happens since we can color-code according to this)
		}
		for( V2xMessage message : vehicle.getMessages() ){

			if (opposingVehicle.addMessage(message)) {
				this.events.processEvent(new PersonEntersVehicleEvent(now, Id.createPersonId("dummy"), opposingVehicle.getId()));
			}

		}

	}

	private void handleVehicleLeavingLink( Id<Vehicle> vehicleId, Id<Link> linkId ) {
		// when a vehicle leaves a link, we just remove it from the data structure:
		Collection<V2xVehicle> vehicles = vehiclesOnLink.get( linkId );
		Gbl.assertNotNull(vehicles); // should not happen, but who knows
		vehicles.remove( allVehicles.get( vehicleId ) );
	}

}
