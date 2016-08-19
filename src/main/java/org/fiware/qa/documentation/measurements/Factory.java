package org.fiware.qa.documentation.measurements;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.fiware.qa.documentation.measurements.ingest.ItemStorage;
import org.fiware.qa.documentation.measurements.models.ScrapedEnablerCataloguePage;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;


public class Factory {
	
	
	
	public static ItemStorage getItems(String fileID)
	{
		
		Gson gson = new Gson();
		ItemStorage items = new ItemStorage();

	try {
	     
		
		BufferedReader resource = new BufferedReader(new FileReader(
		        fileID));

		
		JsonArray jsonArray = new JsonParser().parse(resource).getAsJsonArray();
	    for (int i = 0; i < jsonArray.size(); i++) {
	        JsonElement str = jsonArray.get(i);
	        ScrapedEnablerCataloguePage obj = gson.fromJson(str, ScrapedEnablerCataloguePage.class);

	        String url = obj.url;
	        if (!urlOk (url))
	        	continue;
	        
	        String enablerName = obj.enabler.trim();
	        
	        
	        //System.out.println("processing: " + enablerName);
	        items.store(enablerName, obj);
	        
	        
	    }
	 } catch (IOException e) {
	    e.printStackTrace();
	 }
	
	return items;
		
	}
	
	private static boolean urlOk(String url)
	{
		String match ="http://catalogue.fiware.org/enablers";
		
		if (url.endsWith("bundle-deployment")) return false;
		
		return url.startsWith(match);
		
		
		
	}

}
