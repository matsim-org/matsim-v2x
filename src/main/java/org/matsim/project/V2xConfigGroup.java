package org.matsim.project;

import org.apache.log4j.Logger;
import org.matsim.core.config.ReflectiveConfigGroup;

public class V2xConfigGroup extends ReflectiveConfigGroup {
		private static final Logger log = Logger.getLogger( V2xConfigGroup.class );
		private static final String GROUPNAME = "episim";
		public V2xConfigGroup( ){
			super( GROUPNAME );
		}
		// ---
		private String inputEventsFile = null;
		@StringGetter("inputEventsFile")
		public String getInputEventsFile(){
			return this.inputEventsFile;
		}
		@StringSetter("inputEventsFile")
		public void setInputEventsFile( String inputEventsFile ){
			this.inputEventsFile = inputEventsFile;
		}
}
