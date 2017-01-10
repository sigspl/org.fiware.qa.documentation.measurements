package org.fiware.qa.documentation.measurements;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import org.fiware.qa.documentation.measurements.models.EnablerDescription;
//import com.joestelmach.natty.*;
import org.joda.time.DateTime;
import org.joda.time.Days;

/**
 * 
 * @author pmuryshkin Qualify Enabler descriptions available from the Catalogue
 *         for compliance with the Guide. Guide:
 *         https://forge.fiware.org/plugins/mediawiki/wiki/fiware/index.php/Working_with_the_FIWARE_catalogue#Guidelines_on_what_to_write
 */
// TODO: add scan of page links as URL array or similar for additional layer of
// evaluation = will add more precision and provide data to DEPLOYMENT
// evaluation layer
public class CatalogueComplianceMeasurement {

	public int maximumPoints = 0;
	private EnablerDescription enabler;
	public HashMap<String, Integer> attributes = new HashMap<String, Integer>();
	private PlainMetricProtocol protocol;


	final static Logger logger = Logger
			.getLogger(CatalogueComplianceMeasurement.class);

	public void setEnabler(EnablerDescription e) {
		enabler = e;
	}

	public double measureCompliance() {

		double score = 0;
		score = measureMeta() + measureOverview()
				+ measureCreatingInstances() + measureDownloads()
				+ measureDocumentation() + measureInstances()
				+ measureTermsConditions();

		calculateMaximumPoints(); // !requires all methods to register points!
		printAttributes();
		return score / maximumPoints;

	}

	private int measureOverview() {
		int localScore = 0;

		
		if (enabler.overview.length()<10)
			logger.warn("unassigned text for " + enabler.name);
		
		// attributable quality: overview contains string "What you get"
		attributes.put("catalogue.overview.what", 10);
		// System.out.println(enabler.overview);
		if (enabler.overview.contains("What you get")) {
			localScore += attributes.get("catalogue.overview.what");
			protocol.storeEntry(enabler.name,
					"10/10 points for mentioning 'What you get'");
		} else
			protocol.storeEntry(enabler.name,
					"0/10 points for missing 'What you get'");

		// attributable quality: overview contains string "Why to get it"
		attributes.put("catalogue.overview.why", 10);
		if (enabler.overview.contains("Why to get it")) {
			localScore += attributes.get("catalogue.overview.why");
			protocol.storeEntry(enabler.name,
					"10/10 points for mentioning 'Why to get it'");
		} else
			protocol.storeEntry(enabler.name,
					"0/10 points for missing 'Why to get  it'");

		// attributable quality: text length should be in certain range;
		// too short text is non-informative, or indicates that something might
		// be
		// missing; too long reduces readability and in this context marketing
		// effect.
		// as of August 2016, measured values lay between (789,20541)
		// characters,
		// with a median value 1500 (the closest Enablers is "3D-UI-XML3D".
		// we could assume 1500 characters to be a good average length to
		// present an enabler,
		// and give 10 points for this. Deviation from this
		// ideal value means subtraction of points following some adequate
		// formula.

		// formula: 10-10*(ABS(1-(ABS(1-(ABS(LENGTH-MEDIAN)/MEDIAN)))))

		float median = 1500;
		float L = enabler.overview.length();
		float p = 10;
		int x = Math.round(p - p * (Math.abs(
				1 - (Math.abs(1 - (Math.abs(L - median) / median))))));
		attributes.put("catalogue.overview.optimal_length", 10);
		protocol.storeEntry(enabler.name,
				x+"/10 points for matching recommended median-based text length 1500 chars: length="+(int)L);

		localScore += x; // could be 10 points for a text close to median, or
							// 2-3 points for too long or too short text

		// System.out.println("local score: " + localScore);

		return localScore;
	}

	private int measureCreatingInstances() {
		int localScore = 0;

		// score sections following the template
		String m, e = "";
		int instancesScore =0;
		
		m = "Deploying a dedicated GE instance based on an image";
		e = "There are no images created for this GE implementation yet.";
		attributes.put("catalogue.creating_instances.section1", 10);
		if (matchRegex(enabler.creating_instances, m))
		{
			instancesScore += 10;
			
		}
		else if (matchRegex(enabler.creating_instances,e))
			instancesScore += 2;

		m = "Deploying a dedicated GE instance in your own virtual infrastructure";
		e = "There are no recipe created for this GE implementation yet.";
		attributes.put("catalogue.creating_instances.section2", 10);
		if (matchRegex (enabler.creating_instances,m))
			instancesScore += 10;
		else if (matchRegex (enabler.creating_instances, e))
			instancesScore += 2;

		m = "Deploying a dedicated GE instance based on blueprint templates for this GE";
		attributes.put("catalogue.creating_instances.section3", 10);
		if (matchRegex (enabler.creating_instances, m))
			instancesScore += 10;

		// Docker references. Additionally, this section must include references
		// to the Docker containers and the recipes available (if any).
		// give 1 point for mentioning "Docker", 4 for mentioning
		// "DockerHub"/"Docker Hub" and 5 for "Dockerfile"
		attributes.put("catalogue.creating_instances.docker", 10);
		if (matchRegex (enabler.creating_instances, "\\sDocker\\s"))
			instancesScore += 1;
		if (matchRegex(enabler.creating_instances,"\\sDocker Hub\\s")
				||matchRegex(enabler.creating_instances,"\\sDockerHub\\s"))
			instancesScore += 4;
		if (matchRegex(enabler.creating_instances, "\\sDockerfile\\s"))
			instancesScore += 5;

		
		protocol.storeEntry(enabler.name, + instancesScore + "/40 points for page creating_instances");
		localScore = localScore + instancesScore;
		//if (localScore==0)
		//	logger.warn("0 (zero) points for creating instances tab: \n" );
		return localScore;
	}

	private int measureDocumentation() // TODO: next incremental level of
										// precision would be to count/score
										// links
	{
		int localScore = 0;
		String s[] = { "User and Programmer guides",
				"User's and Programmer's guides",
				"Installation and Administration guides", "Tutorials" };

		attributes.put("catalogue.documentation", 9);

		for (int i = 0; i < s.length; i++) {
			if (matchRegex(enabler.documentation, (s[i])))
				localScore += 3;
		}
		
		protocol.storeEntry(enabler.name, + localScore + "/9 points for page documentation");
		

		return localScore;
	}

	private int measureDownloads() {
		int localScore = 0;
		// simplest measurable value is here to check the must have GitHub
		// reference. (with more precision, for links to GitHub)
		attributes.put("catalogue.downloads.github_mentioned", 10);
		if (matchRegex(enabler.downloads.toLowerCase(),"github"))
			localScore += 10;
		
		protocol.storeEntry(enabler.name, + localScore + "/10 points for page downloads");
		return localScore;
	}

	private int measureInstances() {
		
		// This Catalogue entry intentionally does not list any instance.
		
		return 0;
	}

	private int measureTermsConditions() {
		return 0;
	}

	private void calculateMaximumPoints() {
		int sum = 0;
		for (int f : attributes.values()) {
			sum += f;
		}
		// System.out.println("registered max points for Catalogue: " + sum);
		maximumPoints = sum;

	}

	public void printAttributes() {
		String out = "";
		for (Iterator<String> iterator = attributes.keySet()
				.iterator(); iterator.hasNext();) {
			String n = (String) iterator.next();
			Integer v = attributes.get(n);

			
			String entry = n + Configuration.CSV_SEPARATOR+ v;
			out = out + entry + "\n";

		}

		// write a file; TODO: make a configuration option or implement further
		// data flow
		try {
			FileUtils.writeStringToFile(
					new File(Configuration.QA_DOCS_METRICS_FILENAME), out);
		} catch (IOException e) {
			// TODO add log entry
			e.printStackTrace();
		}
	}

	public void setLogCollector(PlainMetricProtocol protocol) {
		this.protocol = protocol;

	}

	public int measureMeta() {
		int localScore = 0;

		
		attributes.put("catalogue.meta.chapter_mentioned", 5);
		attributes.put("catalogue.meta.version_mentioned", 5);
		attributes.put("catalogue.meta.valid_date", 10); // "recently updated
															// e.g. not later
															// than one year
															// before from today
		attributes.put("catalogue.meta.valid_contact", 5); // contact person is
															// mentioned

		attributes.put("catalogue.meta.valid_email", 10); // there is a valid
															// email address

		
		
		
		//System.out.println(enabler.meta);
		
		/* check whether valid recent date is provided */
		if (measureMeta_checkDate(enabler.meta))
			localScore += 10;

		
		int valid_emails = 0;

		Matcher m = Pattern
				.compile("[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+")
				.matcher(enabler.meta);
		while (m.find()) {
			// System.out.println(m.group());
			valid_emails++;
		}

		if (valid_emails > 0) {
			protocol.storeEntry(enabler.name,
					"10/10 points for a valid contact email");
			localScore += 10;

		} else
			protocol.storeEntry(enabler.name,
					"0/10 points for providing a valid contact email");

		return localScore;
	}

	private boolean measureMeta_checkDate(String meta) {

		boolean match = false;
		Matcher m1 = Pattern.compile("Updated:\\s+\\d{4}-\\d{2}-\\d{2}")
				.matcher(meta);
		while (m1.find()) {
			match = true;

		}

		if (!match) {

			protocol.storeEntry(enabler.name,
					"0/10 points for providing a valid recent date. "
							+ "(no 'Updated: YYYY-MM-DD' string provided/identified) "
							+ "\n" + enabler.meta);
			return false;
		}

		Matcher m2 = Pattern.compile("\\d{4}-\\d{2}-\\d{2}").matcher(meta);
		DateTime today = new DateTime();
		int dateCounter = 0;
		while (m2.find()) {

			dateCounter++;
			if (dateCounter>1) logger.warn("");

			DateTime enablerDate = new DateTime(m2.group());
			int diff = Days.daysBetween(enablerDate.toLocalDate(),
					today.toLocalDate()).getDays();

			if (diff > 0 && diff <= 365) {
				protocol.storeEntry(enabler.name,
						"10/10 points for providing a valid recent date: "
								+ enablerDate.toString() + " - " + diff
								+ " days since last update.");

			} else {
				String msg = "enabler not updated since more than 1 year: " + diff
						+ " days since last update (" + enablerDate.toString()+")";
				
				protocol.storeEntry(enabler.name,
						"0/10 points for providing a valid recent date. "
								+ msg);
				return false;
			}

		}

		return false;

	}
	
	
	public static boolean matchRegex (String subject, String regex)
	{
		
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(subject);
		return m.find();
		
	}
	


}
