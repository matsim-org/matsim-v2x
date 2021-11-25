package org.matsim.project;

import org.matsim.api.core.v01.Scenario;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.ControlerUtils;
import org.matsim.core.controler.OutputDirectoryLogging;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.algorithms.EventWriterXML;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.scenario.ScenarioUtils;

import java.io.IOException;
import java.util.Arrays;

class RunV2xExample{

	public static void main( String[] args ) throws IOException{

		OutputDirectoryLogging.catchLogEntries();

		Config config = ConfigUtils.createConfig( new V2xConfigGroup() );

		config.network().setInputFile( "https://svn.vsp.tu-berlin.de/repos/public-svn/matsim/scenarios/countries/de/berlin/berlin-v5.4-10pct/input/berlin-v5-network.xml.gz" );

		V2xConfigGroup v2xConfig = ConfigUtils.addOrGetModule( config, V2xConfigGroup.class );
		v2xConfig.setInputEventsFile( "https://svn.vsp.tu-berlin.de/repos/public-svn/matsim/scenarios/countries/de/berlin/berlin-v5.4-10pct/output-berlin-v5.4-10pct/berlin-v5.4-10pct.output_events.until_06am.wo_tr.xml.gz" );
//		v2xConfig.setInputEventsFile( "https://svn.vsp.tu-berlin.de/repos/public-svn/matsim/scenarios/countries/de/berlin/berlin-v5.4-10pct/output-berlin-v5.4-10pct/berlin-v5.4-10pct.output_events.xml.gz" );
//		v2xConfig.setInputEventsFile( "https://svn.vsp.tu-berlin.de/repos/public-svn/matsim/scenarios/countries/de/berlin/berlin-v5.4-1pct/output-berlin-v5.4-1pct/berlin-v5.4-1pct.output_events.xml.gz" );
//		v2xConfig.setInputEventsFile( "https://svn.vsp.tu-berlin.de/repos/public-svn/matsim/scenarios/countries/de/berlin/berlin-v5.4-0.1pct/output-berlin-v5.4-0.1pct/berlin-v5.4-0.1pct.output_events_wo_tr.xml.gz" );

		// apply command line arguments, if available, to modify config:
		ConfigUtils.applyCommandline( config, Arrays.copyOfRange( args, 0, args.length ) ) ;

		OutputDirectoryLogging.initLoggingWithOutputDirectory( config.controler().getOutputDirectory() );

		// ---

		Scenario scenario = ScenarioUtils.loadScenario( config );

		NetworkUtils.writeNetwork( scenario.getNetwork(), config.controler().getOutputDirectory() + "/output_network.xml.gz" );
		// (write early so we can analyze based on incomplete output)

		// ---

		EventsManager events = EventsUtils.createEventsManager();

		events.addHandler( new V2xEventHandler( scenario, events ) );

		EventWriterXML writerXML = new EventWriterXML(config.controler().getOutputDirectory() + "/output_events.xml.gz");

		events.addHandler(writerXML);

		ControlerUtils.checkConfigConsistencyAndWriteToLog(config, "Just before starting simulation");

		events.initProcessing();

		EventsUtils.readEvents( events, v2xConfig.getInputEventsFile() );

		events.finishProcessing();

		writerXML.closeFile();

		OutputDirectoryLogging.closeOutputDirLogging();


	}

}
