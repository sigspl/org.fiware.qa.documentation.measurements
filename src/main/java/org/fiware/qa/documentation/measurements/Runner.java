package org.fiware.qa.documentation.measurements;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Iterator;
import java.util.List;

import org.fiware.qa.documentation.measurements.ingest.*;
import org.fiware.qa.documentation.measurements.models.*;




public class Runner {

public static void main(String[] args) {

	
	// information retrieval
	
	
	String fileID=Configuration.INPUT_CATALOGUE_DATA;
	
	ItemStorage items = Factory.getItems(fileID);
	EnablerStorage enablers = new EnablerStorage();
	EnablerPartsAssembler assembler = new EnablerPartsAssembler();
	
	for (Iterator iterator = items.map.keySet().iterator(); iterator.hasNext();) {
		String enablerName = (String) iterator.next();
		List<ScrapedEnablerCataloguePage> pages = items.map.get(enablerName);
		enablers.store(enablerName, assembler.assemble(pages) );
		
	}
	
	enablers.listEnablers();
	System.out.println("number of ingested Enabler objects: " +  enablers.assets.size());

	//EnablerDescription d = enablers.assets.get((String)enablers.assets.keySet().toArray()[0]);
	//System.out.println("report for a random Enabler");
	//d.report();
	
	
	// analysis
	MeasurementExperiment m1 = new MeasurementExperiment();
	m1.setStorage(enablers);
	/*
	m1.execute("Complex Event Processing (CEP) - Proactive Technology Online");
	m1.execute("Stream-oriented - Kurento");
	m1.execute("Identity Management - KeyRock");
	*/
	m1.executeAll();
	
}

public static void printClasspath()
{
	 //Get the System Classloader
    ClassLoader sysClassLoader = ClassLoader.getSystemClassLoader();

    //Get the URLs
    URL[] urls = ((URLClassLoader)sysClassLoader).getURLs();

    for(int i=0; i< urls.length; i++)
    {
        System.out.println(urls[i].getFile());
    }       
	
}


}
